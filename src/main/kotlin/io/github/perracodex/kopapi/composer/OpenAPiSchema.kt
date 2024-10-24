/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.github.perracodex.kopapi.composer.operation.OperationObject
import io.github.perracodex.kopapi.composer.security.SecurityRequirement
import io.github.perracodex.kopapi.dsl.operation.elements.ApiSecurityScheme
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiInfo
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiServerConfig
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiTag
import io.github.perracodex.kopapi.schema.IOpenApiSchema
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
 * @property tags A list of tags used to group API operations together for documentation purposes.
 * @property pathItems An object that holds the relative paths to the individual endpoints and their operations.
 * @property components An object to hold various reusable components such as security schemes, responses, parameters, etc.
 * @property security The global security requirements that apply to all operations unless overridden.
 */
@ComposerAPI
internal data class OpenAPiSchema(
    val openapi: String,
    val info: ApiInfo,
    val servers: List<ApiServerConfig>?,
    val tags: List<ApiTag>?,
    val pathItems: Map<String, PathItemObject>?,
    val components: Components?,
    val security: List<Map<String, List<String>>?>?,
) {
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
    data class PathItemObject(
        var get: OperationObject? = null,
        var put: OperationObject? = null,
        var post: OperationObject? = null,
        var delete: OperationObject? = null,
        var options: OperationObject? = null,
        var head: OperationObject? = null,
        var patch: OperationObject? = null,
    ) {
        /**
         * Adds an API Operation to the [OperationObject] based on the specified HTTP method,
         * ensuring that each operation is correctly associated with its corresponding
         * HTTP method within the path item.
         *
         * @param method The HTTP method of the operation (e.g., "GET", "POST").
         * @param operationObject The [OperationObject] object representing the operation's metadata.
         */
        fun addOperation(method: HttpMethod, operationObject: OperationObject) {
            when (method) {
                HttpMethod.Get -> get = operationObject
                HttpMethod.Put -> put = operationObject
                HttpMethod.Post -> post = operationObject
                HttpMethod.Delete -> delete = operationObject
                HttpMethod.Options -> options = operationObject
                HttpMethod.Head -> head = operationObject
                HttpMethod.Patch -> patch = operationObject
                else -> throw KopapiException("Unsupported HTTP method: $method")
            }
        }

        /**
         * Assigns security configurations to a specific operation within the [PathItemObject] based on the HTTP method.
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

    /**
     * Represents the components section of the OpenAPI schema.
     * The components section holds various reusable objects for different aspects of the API.
     *
     * @property componentSchemas A map of schema names to their respective schema definitions.
     * @property securitySchemes A map of security scheme names to their respective [ApiSecurityScheme] definitions.
     */
    @JsonTypeName("components")
    data class Components(
        @JsonProperty("schemas") val componentSchemas: Map<String, Any?>?,
        @JsonProperty("securitySchemes") val securitySchemes: Map<String, ApiSecurityScheme>?
    ) {
        /**
         * Checks if the components object contains any security schemes.
         *
         * @return `true` if there are one or more security schemes defined; `false` otherwise.
         */
        fun hasContent(): Boolean {
            return !componentSchemas.isNullOrEmpty() || !securitySchemes.isNullOrEmpty()
        }
    }

    /**
     * Represents the [IOpenApiSchema] definition for content in an API operation.
     *
     * This class is used to wrap the schema under the `schema` key for any content type
     * (e.g., `application/json`, `application/xml`), ensuring compatibility with the OpenAPI
     * specification.
     *
     * @property schema The [IOpenApiSchema] representing the structure of the content.
     */
    data class ContentSchema(
        @JsonProperty("schema") var schema: IOpenApiSchema?
    )
}
