/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer

import io.github.perracodex.kopapi.composer.security.SecurityRequirement
import io.github.perracodex.kopapi.core.ApiOperation
import io.github.perracodex.kopapi.core.KopapiException
import io.github.perracodex.kopapi.dsl.api.elements.ApiParameter
import io.github.perracodex.kopapi.dsl.api.elements.ApiRequestBody
import io.github.perracodex.kopapi.dsl.api.elements.ApiResponse
import io.github.perracodex.kopapi.dsl.api.elements.ApiSecurityScheme
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiInfo
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiServerConfig

/**
 * Represents the entire OpenAPI schema, encapsulating all necessary sections
 * such as information, servers, security, paths, and components.
 *
 * This data class serves as the root structure for serializing the OpenAPI specification
 * into JSON or YAML formats.
 *
 * @property openapi The version of the OpenAPI Specification being used (e.g., "3.1.0").
 * @property info Provides metadata about the API, including title, description, version, etc.
 * @property servers A list of server objects that provide connectivity information to the API.
 * @property security The global security requirements that apply to all operations unless overridden.
 * @property paths An object that holds the relative paths to the individual endpoints and their operations.
 * @property components An object to hold various reusable components such as security schemes, responses, parameters, etc.
 */
internal data class OpenAPiSchema(
    val openapi: String,
    val info: ApiInfo,
    val servers: List<ApiServerConfig>?,
    val security: List<Map<String, List<String>>?>?,
    val paths: Map<String, PathItem>?,
    val components: Components?,
) {
    /**
     * Represents the components section of the OpenAPI schema.
     *
     * The components section holds various reusable objects for different aspects of the API.
     * In this context, it primarily includes security schemes that can be referenced throughout
     * the schema to avoid duplication.
     *
     * @property securitySchemes A map of security scheme names to their respective [ApiSecurityScheme] definitions.
     */
    data class Components(
        val securitySchemes: Map<String, ApiSecurityScheme>?
    ) {
        /**
         * Checks if the components object contains any security schemes.
         *
         * @return `true` if there are one or more security schemes defined; `false` otherwise.
         */
        fun hasContent(): Boolean {
            return !securitySchemes.isNullOrEmpty()
        }
    }

    /**
     * Represents an individual path item in the OpenAPI schema.
     *
     * Each path item corresponds to a specific API endpoint and contains operations (HTTP methods)
     * such as GET, POST, PUT, DELETE, etc., along with their configurations.
     *
     * @property get The GET operation for the path, if defined.
     * @property put The PUT operation for the path, if defined.
     * @property post The POST operation for the path, if defined.
     * @property delete The DELETE operation for the path, if defined.
     * @property options The OPTIONS operation for the path, if defined.
     * @property head The HEAD operation for the path, if defined.
     * @property patch The PATCH operation for the path, if defined.
     * @property trace The TRACE operation for the path, if defined.
     */
    data class PathItem(
        var get: PathOperation? = null,
        var put: PathOperation? = null,
        var post: PathOperation? = null,
        var delete: PathOperation? = null,
        var options: PathOperation? = null,
        var head: PathOperation? = null,
        var patch: PathOperation? = null,
        var trace: PathOperation? = null
    ) {
        /**
         * Adds an API Operation to the [PathItem] based on the specified HTTP method,
         * ensuring that each operation is correctly associated with its corresponding
         * HTTP method within the path item.
         *
         * @param method The HTTP method of the operation (e.g., "GET", "POST").
         * @param apiOperation The [ApiOperation] object containing the operation's metadata and configurations.
         */
        fun addOperation(method: String, apiOperation: ApiOperation) {
            val pathOperation: PathOperation = PathOperation.fromApiOperation(apiOperation = apiOperation)
            when (method.lowercase()) {
                "get" -> get = pathOperation
                "put" -> put = pathOperation
                "post" -> post = pathOperation
                "delete" -> delete = pathOperation
                "options" -> options = pathOperation
                "head" -> head = pathOperation
                "patch" -> patch = pathOperation
                "trace" -> trace = pathOperation
                else -> throw KopapiException("Unsupported HTTP method: $method")
            }
        }

        /**
         * Assigns security configurations to a specific operation within the PathItem based on the HTTP method.
         *
         * It updates the security requirements for the specified HTTP method by mapping
         * each [SecurityRequirement] to its corresponding map structure as defined by the OpenAPI Specification.
         *
         * @param method The HTTP method of the operation (e.g., "GET", "POST").
         * @param security A list of [SecurityRequirement] objects representing the security configurations.
         *                 An empty list (`security: []`) indicates that the operation does not require security.
         */
        fun setSecurity(method: String, security: List<SecurityRequirement>?) {
            val securityMaps: List<Map<String, List<String>>>? = security?.mapNotNull { it.toOpenAPISpec() }
            when (method.lowercase()) {
                "get" -> get?.security = securityMaps
                "put" -> put?.security = securityMaps
                "post" -> post?.security = securityMaps
                "delete" -> delete?.security = securityMaps
                "options" -> options?.security = securityMaps
                "head" -> head?.security = securityMaps
                "patch" -> patch?.security = securityMaps
                "trace" -> trace?.security = securityMaps
                else -> throw KopapiException("Unsupported HTTP method: $method")
            }
        }
    }

    /**
     * Represents an individual API Operation (HTTP method) within a path item.
     *
     * Each operation includes details such as summary, description, tags, parameters,
     * request body, responses, and security requirements.
     *
     * @property summary A brief summary of what the operation does.
     * @property description A detailed description of the operation, which can span multiple lines.
     * @property tags A set of tags for API documentation control. Tags can be used to group operations.
     * @property parameters A set of [ApiParameter] objects defining the parameters for the operation.
     * @property requestBody The [ApiRequestBody] object defining the request body for the operation.
     * @property responses A set of [ApiResponse] objects defining the possible responses for the operation.
     * @property security A list of security requirement maps, each specifying a security scheme and its scopes.
     *                    An empty list (`security: []`) disables security for this operation.
     */
    data class PathOperation(
        val summary: String?,
        val description: String?,
        val tags: Set<String>?,
        val parameters: Set<ApiParameter>?,
        val requestBody: ApiRequestBody?,
        val responses: Set<ApiResponse>?,
        var security: List<Map<String, List<String>>>? = null
    ) {
        companion object {
            /**
             * Creates an [PathOperation] instance from an [ApiOperation] object.
             *
             * This factory method facilitates the transformation of an [ApiOperation] into
             * an [PathOperation] suitable for inclusion in the OpenAPI schema.
             *
             * @param apiOperation The [ApiOperation] object containing the operation's metadata.
             * @return An [PathOperation] instance populated with data from [apiOperation].
             */
            fun fromApiOperation(apiOperation: ApiOperation): PathOperation {
                return PathOperation(
                    summary = apiOperation.summary,
                    description = apiOperation.description,
                    tags = apiOperation.tags,
                    parameters = apiOperation.parameters,
                    requestBody = apiOperation.requestBody,
                    responses = apiOperation.responses
                )
            }
        }
    }
}
