/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.schema

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import io.github.perracodex.kopapi.composer.annotation.ComposerApi
import io.github.perracodex.kopapi.composer.operation.OperationObject
import io.github.perracodex.kopapi.dsl.operation.elements.ApiSecurityScheme
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiInfo
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiServerConfig
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiTag
import io.github.perracodex.kopapi.schema.facets.ISchemaFacet
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
 * @property paths An object that holds the relative paths to the individual endpoints and their operations.
 * @property components An object to hold various reusable components such as security schemes, responses, parameters, etc.
 * @property security The top-level security requirements that apply to all operations unless overridden.
 */
@ComposerApi
internal data class OpenApiSchema(
    val openapi: String,
    val info: ApiInfo,
    val servers: List<ApiServerConfig>?,
    val tags: List<ApiTag>?,
    val paths: Map<String, PathItemObject>?,
    val components: Components?,
    val security: List<Map<String, List<String>>?>?,
) {
    /**
     * Represents an individual path item in the OpenAPI schema.
     *
     * Each path item corresponds to a specific API endpoint and contains operations (HTTP methods)
     * such as GET, POST, PUT, DELETE, etc., along with their configurations.
     *
     * @property summary A brief summary of the path item.
     * @property description A detailed description of the path item.
     * @property servers A list of server configurations specific to this path.
     * @property get The GET operation for the path, if defined.
     * @property put The PUT operation for the path, if defined.
     * @property post The POST operation for the path, if defined.
     * @property delete The DELETE operation for the path, if defined.
     * @property options The OPTIONS operation for the path, if defined.
     * @property head The HEAD operation for the path, if defined.
     * @property patch The PATCH operation for the path, if defined.
     * @property trace The TRACE operation for the path, if defined.
     */
    data class PathItemObject(
        val summary: String? = null,
        val description: String? = null,
        val servers: Set<ApiServerConfig>? = null,
        var get: OperationObject? = null,
        var put: OperationObject? = null,
        var post: OperationObject? = null,
        var delete: OperationObject? = null,
        var options: OperationObject? = null,
        var head: OperationObject? = null,
        var patch: OperationObject? = null,
        var trace: OperationObject? = null,
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
                else -> {
                    trace = operationObject.takeIf {
                        method.value.equals(other = "TRACE", ignoreCase = true)
                    } ?: throw KopapiException("Unsupported HTTP method: $method")
                }
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
        @JsonProperty("schemas") val componentSchemas: Map<String, ISchemaFacet>?,
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
     * Represents the [ISchemaFacet] definition for content in an API operation.
     *
     * This class is used to wrap the schema under the `schema` key for any content type
     * (e.g., `application/json`, `application/xml`), ensuring compatibility with the OpenAPI
     * specification.
     *
     * @property schema The [ISchemaFacet] representing the structure of the content.
     */
    data class ContentSchema(
        @JsonProperty("schema") var schema: ISchemaFacet?
    )
}
