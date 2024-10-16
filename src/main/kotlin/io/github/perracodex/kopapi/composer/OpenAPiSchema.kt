/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer

import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.github.perracodex.kopapi.composer.path.PathOperation
import io.github.perracodex.kopapi.composer.security.SecurityRequirement
import io.github.perracodex.kopapi.dsl.operation.elements.ApiOperation
import io.github.perracodex.kopapi.dsl.operation.elements.ApiSecurityScheme
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiInfo
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiServerConfig
import io.github.perracodex.kopapi.system.KopapiException
import io.ktor.http.*

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
@ComposerAPI
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
     */
    data class PathItem(
        var get: PathOperation? = null,
        var put: PathOperation? = null,
        var post: PathOperation? = null,
        var delete: PathOperation? = null,
        var options: PathOperation? = null,
        var head: PathOperation? = null,
        var patch: PathOperation? = null,
    ) {
        /**
         * Adds an API Operation to the [PathItem] based on the specified HTTP method,
         * ensuring that each operation is correctly associated with its corresponding
         * HTTP method within the path item.
         *
         * @param method The HTTP method of the operation (e.g., "GET", "POST").
         * @param apiOperation The [ApiOperation] object containing the operation's metadata and configurations.
         */
        fun addOperation(method: HttpMethod, apiOperation: ApiOperation) {
            val pathOperation: PathOperation = PathOperation.fromApiOperation(apiOperation = apiOperation)
            when (method) {
                HttpMethod.Get -> get = pathOperation
                HttpMethod.Put -> put = pathOperation
                HttpMethod.Post -> post = pathOperation
                HttpMethod.Delete -> delete = pathOperation
                HttpMethod.Options -> options = pathOperation
                HttpMethod.Head -> head = pathOperation
                HttpMethod.Patch -> patch = pathOperation
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
        fun setSecurity(method: HttpMethod, security: List<SecurityRequirement>?) {
            val securityMaps: List<Map<String, List<String>>>? = security?.map { it.toOpenAPISpec() }
            when (method) {
                HttpMethod.Get -> get?.security = securityMaps
                HttpMethod.Put -> put?.security = securityMaps
                HttpMethod.Post -> post?.security = securityMaps
                HttpMethod.Delete -> delete?.security = securityMaps
                HttpMethod.Options -> options?.security = securityMaps
                HttpMethod.Head -> head?.security = securityMaps
                HttpMethod.Patch -> patch?.security = securityMaps
                else -> throw KopapiException("Unsupported HTTP method: $method")
            }
        }
    }
}
