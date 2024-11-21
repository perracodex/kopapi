/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.composer.operation

import io.github.perracodex.kopapi.composer.annotation.ComposerApi
import io.github.perracodex.kopapi.composer.parameter.ParameterComposer
import io.github.perracodex.kopapi.composer.parameter.ParameterObject
import io.github.perracodex.kopapi.composer.request.RequestBodyComposer
import io.github.perracodex.kopapi.composer.request.RequestBodyObject
import io.github.perracodex.kopapi.composer.response.ResponseComposer
import io.github.perracodex.kopapi.composer.response.ResponseObject
import io.github.perracodex.kopapi.composer.security.SecurityObject
import io.github.perracodex.kopapi.composer.security.SecurityRequirement
import io.github.perracodex.kopapi.dsl.operation.element.ApiOperation
import io.github.perracodex.kopapi.dsl.path.element.ApiPath
import io.github.perracodex.kopapi.dsl.plugin.element.ApiServerConfig
import io.github.perracodex.kopapi.schema.OpenApiSchema
import io.github.perracodex.kopapi.system.Tracer
import io.github.perracodex.kopapi.util.orNull
import io.github.perracodex.kopapi.util.trimOrNull

/**
 * Responsible for composing the `paths` section of the OpenAPI schema.
 *
 * The `paths` section maps each API endpoint to its corresponding HTTP methods and associated
 * configurations, including parameters, request bodies, responses, and security requirements.
 *
 * @see [OpenApiSchema.PathItemObject]
 */
@ComposerApi
internal object OperationComposer {
    private val tracer: Tracer = Tracer<OperationComposer>()

    /**
     * Generates the `paths` section of the OpenAPI schema by iterating over each API operation
     * and organizing them based on their HTTP method and path.
     *
     * It ensures that each API operation is correctly placed under its respective path
     * and method, with all relevant configurations and security requirements applied.
     *
     * @param apiPaths Set of [ApiPath] objects representing each API path's metadata.
     * @param apiOperations Set of [ApiOperation] objects representing each API endpoint's metadata.
     * @param securityObject List of [SecurityObject] objects detailing the security configurations for each API operation.
     * @return A map where each key is an API Operation path and the value is an [OpenApiSchema.PathItemObject]
     * object containing the HTTP methods and their configurations for that path.
     */
    fun compose(
        apiPaths: Set<ApiPath>,
        apiOperations: Set<ApiOperation>,
        securityObject: List<SecurityObject>?
    ): Map<String, OpenApiSchema.PathItemObject> {
        tracer.info("Composing the 'paths' section of the OpenAPI schema.")

        val pathItems: MutableMap<String, OpenApiSchema.PathItemObject> = mutableMapOf()

        apiOperations.forEach { operation ->
            tracer.debug("Composing operation: [${operation.method}] → ${operation.path}")

            // Find the most specific ApiPath for the operation's path.
            val apiPath: ApiPath? = findMostSpecificApiPath(
                operationPath = operation.path,
                apiPaths = apiPaths
            )

            // Retrieve or create the PathItemObject for the operation's path.
            var pathItemObject: OpenApiSchema.PathItemObject = pathItems.getOrPut(operation.path) {
                OpenApiSchema.PathItemObject()
            }

            // Apply the path-level metadata to the PathItemObject.
            apiPath?.let { path ->
                pathItemObject = applyPathLevelMetadata(
                    pathItemObject = pathItemObject,
                    apiPath = path
                )
                // Update the pathItems map with the updated pathItemObject.
                pathItems[operation.path] = pathItemObject
            }

            // Locate the corresponding security configuration for the operation, if any.
            val securityConfig: List<SecurityRequirement>? = securityObject
                ?.find { operationSecurity ->
                    operationSecurity.method.equals(operation.method.value, ignoreCase = true) &&
                            operationSecurity.path.equals(operation.path, ignoreCase = true)
                }?.security
            val securityMaps: List<Map<String, List<String>>>? = securityConfig?.map { it.toOpenApiSpec() }

            // Transform the ApiOperation.
            val operationObject: OperationObject = fromApiOperation(
                apiOperation = operation,
                security = securityMaps
            )
            pathItemObject.addOperation(
                method = operation.method,
                operationObject = operationObject
            )
        }

        tracer.debug("Composed ${pathItems.size} path items.")

        return pathItems.toSortedMap()
    }

    /**
     * Applies path-level metadata from an [ApiPath] to a [OpenApiSchema.PathItemObject].
     *
     * #### Merging Servers
     * - **When Both Are Null or Empty**: The result is `null`.
     * - **When One Is Null or Empty**: The result is the non-null/non-empty one.
     * - **When Both Have Servers**: Merge them into a single set, removing duplicates based on `url`.
     *
     * @param pathItemObject The original [OpenApiSchema.PathItemObject] to which metadata will be applied.
     * @param apiPath The [ApiPath] containing path-level metadata to apply.
     * @return A new [OpenApiSchema.PathItemObject] with the merged metadata.
     */
    private fun applyPathLevelMetadata(
        pathItemObject: OpenApiSchema.PathItemObject,
        apiPath: ApiPath
    ): OpenApiSchema.PathItemObject {
        // Retrieve the path-level parameters.
        val parameters: Set<ParameterObject>? = apiPath.parameters?.let {
            ParameterComposer.compose(apiParameters = apiPath.parameters)
        }

        // Merge the servers.
        val mergedServers: Set<ApiServerConfig>? = when {
            pathItemObject.servers.isNullOrEmpty() && apiPath.servers.isNullOrEmpty() -> null
            pathItemObject.servers.isNullOrEmpty() -> apiPath.servers
            apiPath.servers.isNullOrEmpty() -> pathItemObject.servers
            else -> (pathItemObject.servers + apiPath.servers)
                .distinctBy { it.url }
                .toSet()
        }

        // Merge the parameters.
        val mergedParameters: Set<ParameterObject>? = when {
            pathItemObject.parameters.isNullOrEmpty() && parameters.isNullOrEmpty() -> null
            pathItemObject.parameters.isNullOrEmpty() -> parameters
            parameters.isNullOrEmpty() -> pathItemObject.parameters
            else -> (pathItemObject.parameters + parameters)
                .distinctBy { it.name }
                .toSet()
        }

        // Create a new PathItemObject with merged properties.
        return pathItemObject.copy(
            summary = apiPath.summary.trimOrNull() ?: pathItemObject.summary,
            description = apiPath.description.trimOrNull() ?: pathItemObject.description,
            servers = mergedServers,
            parameters = mergedParameters
        )
    }

    /**
     * Finds the most specific [ApiPath] that applies to the given operation's path,
     * by normalizing both the operation's path and the [ApiPath] paths by replacing
     * path parameters with a placeholder. It then selects the [ApiPath] whose normalized
     * path is the longest prefix of the normalized operation path.
     *
     * This approach ensures that path-level metadata is correctly associated with operations,
     * respecting the nested structure and specificity of paths in Ktor applications.
     *
     * #### Example
     * ```
     * // Given ApiPaths:
     * /api
     * /api/v1
     * /api/v1/items
     * /api/v1/items/{item_id}
     *
     * // Operation path:
     * /api/v1/items/{item_id}/details
     *
     * // The function will select:
     * /api/v1/items/{item_id}
     * ```
     *
     * @param operationPath The path of the [ApiOperation].
     * @param apiPaths A set of [ApiPath] objects representing path-level metadata.
     * @return The most specific [ApiPath] applicable to the operation, or `null` if none found.
     */
    private fun findMostSpecificApiPath(operationPath: String, apiPaths: Set<ApiPath>): ApiPath? {
        // Normalize the operation's path by replacing parameters with '{}'.
        val normalizedOperationPath: String = normalizePath(path = operationPath)

        // Filter ApiPaths whose normalized path is a prefix of the normalized operation path.
        return apiPaths.filter { apiPath ->
            normalizedOperationPath.startsWith(
                prefix = normalizePath(path = apiPath.path),
                ignoreCase = true
            )
        }.maxByOrNull { apiPath ->
            // Select the ApiPath with the longest normalized path (most specific).
            normalizePath(path = apiPath.path).length
        }
    }

    /**
     * Normalizes a path by replacing path parameters with a placeholder.
     *
     * It replaces all path parameters (e.g., `{id}`, `{userId}`) with `{}` to standardize
     * variable segments. This normalization allows for accurate comparison of paths
     * by focusing on their structural components rather than the specific parameter names.
     *
     * #### Example
     * ```
     * // Will produce: "/api/v1/items/{}"
     * normalizePath("/api/v1/items/{item_id}")
     *
     * // Will produce: "/api/{}/users/{}"
     * normalizePath("/api/{version}/users/{userId}")
     * ```
     *
     * #### Rationale
     * Path normalization is needed for correctly associating path-level metadata with operations
     * in nested routes, especially when path parameters are involved. By standardizing variable
     * segments, we can compare paths based on their fixed structure.
     *
     * @param path The original path string to normalize.
     * @return The normalized path with parameters replaced by `{}`.
     */
    private fun normalizePath(path: String): String {
        return path.replace(regex = Regex(pattern = "\\{[^}]+\\}"), replacement = "{}")
    }

    /**
     * Creates an [OperationObject] instance from an [ApiOperation] object.
     *
     * This factory method facilitates the transformation of an [ApiOperation] into
     * an [OperationObject] suitable for inclusion in the OpenAPI schema.
     *
     * @param apiOperation The [ApiOperation] object containing the operation's metadata.
     * @param security A list of [SecurityRequirement] objects representing the security configurations.
     *                 An empty list (`security: []`) indicates that the operation does not require security.
     * @return An [OperationObject] instance populated with data from [apiOperation].
     */
    private fun fromApiOperation(
        apiOperation: ApiOperation,
        security: List<Map<String, List<String>>>?
    ): OperationObject {
        val parameters: Set<ParameterObject>? = apiOperation.parameters?.let {
            ParameterComposer.compose(apiParameters = apiOperation.parameters)
        }
        val requestBody: RequestBodyObject? = apiOperation.requestBody?.let {
            RequestBodyComposer.compose(requestBody = apiOperation.requestBody)
        }
        val responses: Map<String, ResponseObject>? = apiOperation.responses?.let {
            ResponseComposer.compose(responses = apiOperation.responses)
        }
        return OperationObject(
            tags = apiOperation.tags?.orNull(),
            summary = apiOperation.summary,
            description = apiOperation.description,
            operationId = apiOperation.operationId,
            parameters = parameters?.orNull(),
            requestBody = requestBody,
            responses = responses?.orNull(),
            security = security,
            servers = apiOperation.servers?.orNull()
        )
    }
}
