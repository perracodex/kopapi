/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.operation

import io.github.perracodex.kopapi.dsl.common.SecuritySchemeConfigurable
import io.github.perracodex.kopapi.dsl.markers.OperationDsl
import io.github.perracodex.kopapi.dsl.operation.builders.attributes.HeaderBuilder
import io.github.perracodex.kopapi.dsl.operation.builders.attributes.LinkBuilder
import io.github.perracodex.kopapi.dsl.operation.builders.parameter.CookieParameterBuilder
import io.github.perracodex.kopapi.dsl.operation.builders.parameter.HeaderParameterBuilder
import io.github.perracodex.kopapi.dsl.operation.builders.parameter.PathParameterBuilder
import io.github.perracodex.kopapi.dsl.operation.builders.parameter.QueryParameterBuilder
import io.github.perracodex.kopapi.dsl.operation.builders.request.RequestBodyBuilder
import io.github.perracodex.kopapi.dsl.operation.builders.response.ResponseBuilder
import io.github.perracodex.kopapi.dsl.operation.elements.ApiOperation
import io.github.perracodex.kopapi.dsl.operation.elements.ApiParameter
import io.github.perracodex.kopapi.dsl.operation.elements.ApiRequestBody
import io.github.perracodex.kopapi.dsl.operation.elements.ApiResponse
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.types.PathType
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.string.SpacedString
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.http.*
import io.ktor.utils.io.*
import java.util.*
import kotlin.reflect.typeOf

/**
 * Builder for constructing API Operations metadata for a route endpoint.
 *
 * Usage involves defining a Ktor route and attaching API metadata using the `Route.api` infix
 * function to enrich the route with operational details and documentation specifications.
 *
 * #### Information
 * - [summary]: Optional short description of the endpoint's purpose.
 * - [description]: Optional detailed explanation of the endpoint and its functionality.
 * - [tags]: Optional set of descriptive tags for categorizing the endpoint in API documentation.
 *
 * #### Parameters
 * - [pathParameter]: Adds a path parameter to the API endpoint's metadata.
 * - [queryParameter]: Adds a query parameter to the API endpoint's metadata.
 * - [headerParameter]: Adds a header parameter to the API endpoint's metadata.
 * - [cookieParameter]: Adds a cookie parameter to the API endpoint's metadata.
 *
 * #### Request Body
 * - [requestBody]: Adds a request body to the API endpoint's metadata.
 *
 * #### Responses
 * - [response]: Add a response to the endpoint. Can be used with or without a response type.
 *  With no response type, the response is assumed to be only a [HttpStatusCode] with no associated type.
 *
 * #### Security Schemes
 * - [apiKeySecurity]: Adds an API key security scheme to the API metadata.
 * - [httpSecurity]: Adds an HTTP security scheme to the API metadata.
 * - [mutualTLSSecurity]: Adds a mutual TLS security scheme to the API metadata.
 * - [oauth2Security]: Adds an OAuth 2.0 security scheme to the API metadata.
 * - [openIdConnectSecurity]: Adds an OpenID Connect security scheme to the API metadata.
 *
 * #### Sample Usage
 * ```
 * get("/items/{data_id}/{item_id?}") {
 *     // Handle GET request
 * } api {
 *     tags = setOf("Items", "Data")
 *     summary = "Retrieve data items."
 *     description = "Fetches all items for a data set."
 *     pathParameter("data_id", PathType.Uuid) { description = "The data Id." }
 *     queryParameter<String>("item_id") { description = "Optional item Id." }
 *     response<List<Item>>(HttpStatusCode.OK) { description = "Successful." }
 *     response(HttpStatusCode.NotFound) { description = "Data not found." }
 * }
 * ```
 *
 * @see [ApiOperation]
 */
@KtorDsl
@OperationDsl
public class ApiOperationBuilder internal constructor(
    @PublishedApi internal val endpoint: String
) : SecuritySchemeConfigurable() {

    @Suppress("PropertyName")
    @PublishedApi
    internal val _config: Config = Config(endpoint = endpoint)

    /**
     * Optional short description of the endpoint's purpose.
     *
     * Declaring the `summary` multiple times will concatenate all the summaries
     * delimited by a `space` character between each one.
     *
     * #### Sample Usage
     * ```
     * summary = "Retrieve data items."
     * ```
     *
     * @see [description]
     * @see [tags]
     */
    public var summary: String by SpacedString()

    /**
     * Optional detailed explanation of the endpoint and its functionality.
     *
     * Declaring the `description` multiple times will concatenate all the descriptions
     * delimited by a `newline` character between each one.
     *
     * #### Sample Usage
     * ```
     * description = "Fetches all items for a group."
     * description = "In addition, it can fetch a specific item."
     * ```
     *
     * @see [summary]
     * @see [tags]
     */
    public var description: String by MultilineString()

    /**
     * Optional set of descriptive tags for categorizing the endpoint in API documentation.
     *
     * Declaring multiple `tags` will append all the of them to the existing list.
     * Repeated tags are discarded in a case-insensitive manner.
     *
     * #### Sample Usage
     * ```
     * tags = setOf("Items", "Data")
     * tags = setOf("NewTag1", "NewTag2")
     * ```
     *
     * @see [summary]
     * @see [description]
     */
    public var tags: Set<String>
        get() = _config.tags.toSet()
        set(value) {
            _config.tags.addAll(value.map { it.trim() }.filter { it.isNotBlank() })
        }

    /**
     * The identifier for the API operation.
     * Must be unique across all API operations.
     */
    public var operationId: String? = null

    /**
     * Disables the security schemes for the API operation.
     * Both top-level global and local security schemes will not be applied to this API Operation.
     *
     * @see [apiKeySecurity]
     * @see [httpSecurity]
     * @see [mutualTLSSecurity]
     * @see [oauth2Security]
     * @see [openIdConnectSecurity]
     */
    public fun skipSecurity() {
        _securityConfig.securitySchemes.clear()
        _securityConfig.skipSecurity = true
    }

    /**
     * Registers a path parameter.
     *
     * #### Sample Usage
     * ```
     * pathParameter(name = "id", type = PathType.String) {
     *     description = "The unique identifier of the item."
     * }
     * ```
     *
     * #### Attention:
     * Contrary to `query`, `cookie`, and `header` parameters, `path` parameters
     * are more constrained in the types they can represent, so the class `PathType`
     * must be used instead of defining generic type.
     *
     * @param name The name of the parameter as it appears in the URL path.
     * @param type The [PathType] for the parameter.
     * @param configure A lambda receiver for configuring the [PathParameterBuilder].
     *
     * @see [PathParameterBuilder]
     * @see [cookieParameter]
     * @see [headerParameter]
     * @see [queryParameter]
     * @see [requestBody]
     */
    public inline fun ApiOperationBuilder.pathParameter(
        type: PathType,
        name: String,
        configure: PathParameterBuilder.() -> Unit = {}
    ) {
        val builder: PathParameterBuilder = PathParameterBuilder().apply(configure)
        val parameter: ApiParameter = builder.build(name = name, pathType = type)
        _config.addApiParameter(apiParameter = parameter)
    }

    /**
     * Registers a query parameter.
     *
     * #### Sample Usage
     * ```
     * queryParameter<Int>(name = "page") {
     *     description = "The page number to retrieve."
     * }
     * queryParameter<Int>(name = "size") {
     *     description = "The number of items per page."
     *     required = false
     *     defaultValue = 1
     * }
     * ```
     *
     * @param T The type of the parameter.
     * @param name The name of the parameter as it appears in the URL path.
     * @param configure A lambda receiver for configuring the [QueryParameterBuilder].
     *
     * @see [QueryParameterBuilder]
     * @see [cookieParameter]
     * @see [headerParameter]
     * @see [pathParameter]
     * @see [requestBody]
     */
    public inline fun <reified T : Any> ApiOperationBuilder.queryParameter(
        name: String,
        configure: QueryParameterBuilder.() -> Unit = {}
    ) {
        val builder: QueryParameterBuilder = QueryParameterBuilder().apply(configure)
        val parameter: ApiParameter = builder.build(name = name, type = typeOf<T>())
        _config.addApiParameter(apiParameter = parameter)
    }

    /**
     * Registers a header parameter.
     *
     * #### Sample Usage
     * ```
     * headerParameter<String>(name = "X-Custom-Header") {
     *     description = "A custom header for special purposes."
     *     required = true
     * }
     * ```
     *
     * @param T The type of the parameter.
     * @param name The header of the parameter as it appears in the URL path.
     * @param configure A lambda receiver for configuring the [HeaderParameterBuilder].
     *
     * @see [HeaderParameterBuilder]
     * @see [cookieParameter]
     * @see [pathParameter]
     * @see [queryParameter]
     * @see [requestBody]
     */
    public inline fun <reified T : Any> ApiOperationBuilder.headerParameter(
        name: String,
        configure: HeaderParameterBuilder.() -> Unit = {}
    ) {
        val builder: HeaderParameterBuilder = HeaderParameterBuilder().apply(configure)
        val parameter: ApiParameter = builder.build(name = name, type = typeOf<T>())
        _config.addApiParameter(apiParameter = parameter)
    }

    /**
     * Registers a cookie parameter.
     *
     * #### Sample Usage
     * ```
     * cookieParameter<String>(name = "session") {
     *     description = "The session ID for authentication."
     * }
     * ```
     *
     * @param T The type of the parameter.
     * @param name The name of the parameter as it appears in the URL path.
     * @param configure A lambda receiver for configuring the [CookieParameterBuilder].
     *
     * @see [CookieParameterBuilder]
     * @see [headerParameter]
     * @see [pathParameter]
     * @see [queryParameter]
     * @see [requestBody]
     */
    public inline fun <reified T : Any> ApiOperationBuilder.cookieParameter(
        name: String,
        configure: CookieParameterBuilder.() -> Unit = {}
    ) {
        val builder: CookieParameterBuilder = CookieParameterBuilder().apply(configure)
        val parameter: ApiParameter = builder.build(name = name, type = typeOf<T>())
        _config.addApiParameter(apiParameter = parameter)
    }

    /**
     * Registers a request body.
     *
     * #### Sample Typed Request Body
     * ```
     * // Standard request body.
     * requestBody<MyRequestBodyType> {
     *      description = "The data required to create a new item."
     *      required = true
     *
     *      // Optional composition.
     *      // Only meaningful if multiple types are provided.
     *      // If omitted, defaults to `AnyOf`.
     *      composition = Composition.AnyOf
     *
     *      // Register an additional type.
     *      addType<AnotherType>()
     *
     *      // Register another type to the PDF ContentType
     *      // instead of the default.
     *      addType<YetAnotherType>(
     *          setOf(ContentType.Application.Pdf)
     *      )
     * }
     * ```
     *
     * #### Sample Multipart
     * ```
     * requestBody<Unit> {
     *      // Implicit ContentType.MultiPart.FormData (default)
     *      multipart {
     *          part<PartData.FileItem>("file") {
     *              description = "The file to upload."
     *          }
     *          part<PartData.FormItem>("metadata") {
     *              description = "Metadata about the file, provided as JSON."
     *          }
     *      }
     *
     *      // Explicit ContentType.MultiPart.Encrypted
     *      multipart(contentType = ContentType.MultiPart.Encrypted) {
     *          part<PartData.FileItem>("secureFile") {
     *              description = "A securely uploaded file."
     *          }
     *          part<PartData.FormItem>("metadata") {
     *              description = "Additional metadata about the file."
     *          }
     *      }
     * }
     * ```
     *
     * @param T The body primary type of the request.
     * @param contentType Optional set of [ContentType]s to associate with the type. Default: `JSON`.
     * @param configure A lambda receiver for configuring the [RequestBodyBuilder].
     *
     * @see [RequestBodyBuilder]
     * @see [cookieParameter]
     * @see [headerParameter]
     * @see [pathParameter]
     * @see [queryParameter]
     * @see [response]
     */
    public inline fun <reified T : Any> ApiOperationBuilder.requestBody(
        contentType: Set<ContentType>? = null,
        configure: RequestBodyBuilder.() -> Unit = {}
    ) {
        if (_config.requestBody == null) {
            val builder: RequestBodyBuilder = RequestBodyBuilder().apply {
                // Associate the primary type T with the builder's contentTypes.
                addType<T>(contentType = contentType)
                // Apply additional configurations, which can include more types.
                apply(configure)
            }

            _config.requestBody = builder.build()

        } else {
            throw KopapiException(
                "Only one RequestBody can be defined per API endpoint. " +
                        "Attempted to define multiple RequestBodies in '${this.endpoint}'"
            )
        }
    }

    /**
     * Registers a response.
     *
     * Responses can be registered with or without a response body type:
     * ```
     * response<ResponseType>(HttpStatusCode) { ... }
     * response(HttpStatusCode) { ... }
     * ```
     *
     * #### Sample Usage
     *```
     * response<String>(status = HttpStatusCode.OK) {
     *      description = "Successfully retrieved the item."
     *
     *      header(name = "X-Rate-Limit") {
     *          description = "Number of allowed requests per period."
     *          required = true
     *      }
     *      link(operationId = "getNextItem") {
     *          description = "Link to the next item."
     *      }
     *
     *      // Only meaningful if multiple types are provided.
     *      // If omitted, defaults to `AnyOf`.
     *      composition = Composition.AnyOf
     *
     *      // Register additional type,
     *      // and also set it to the PDF ContentType.
     *      addType<YetAnotherType>(
     *          setOf(ContentType.Application.Pdf)
     *      )
     * }
     *```
     *
     * @param status The [HttpStatusCode] code associated with this response.
     * @param configure A lambda receiver for configuring the [ResponseBuilder].
     *
     * @see [ResponseBuilder]
     * @see [HeaderBuilder]
     * @see [LinkBuilder]
     */
    @JvmName(name = "responseWithoutType")
    public fun ApiOperationBuilder.response(
        status: HttpStatusCode,
        configure: ResponseBuilder.() -> Unit = {}
    ) {
        response<Unit>(status = status, configure = configure)
    }

    /**
     * Registers a response.
     *
     * Responses can be registered with or without a response body type:
     * ```
     * response<ResponseType>(HttpStatusCode) { ... }
     * response(HttpStatusCode) { ... }
     * ```
     *
     * #### Sample Usage
     *```
     * response<String>(status = HttpStatusCode.OK) {
     *      description = "Successfully retrieved the item."
     *
     *      header(name = "X-Rate-Limit") {
     *          description = "Number of allowed requests per period."
     *          required = true
     *      }
     *      link(operationId = "getNextItem") {
     *          description = "Link to the next item."
     *      }
     *
     *      // Only meaningful if multiple types are provided.
     *      // If omitted, defaults to `AnyOf`.
     *      composition = Composition.AnyOf
     *
     *      // Register additional type,
     *      // and also set it to the PDF ContentType.
     *      addType<YetAnotherType>(
     *          setOf(ContentType.Application.Pdf)
     *      )
     * }
     *```
     *
     * @param T The body primary type of the response.
     * @param status The [HttpStatusCode] code associated with this response.
     * @param contentType One or more [ContentType]s to associate with the response. Default: `JSON`.
     * @param configure A lambda receiver for configuring the [ResponseBuilder].
     *
     * @see [ResponseBuilder]
     * @see [HeaderBuilder]
     * @see [LinkBuilder]
     */
    @JvmName(name = "responseWithSingleContentType")
    public inline fun <reified T : Any> ApiOperationBuilder.response(
        status: HttpStatusCode,
        contentType: ContentType,
        configure: ResponseBuilder.() -> Unit = {}
    ) {
        response<T>(
            status = status,
            contentType = setOf(contentType),
            configure = configure
        )
    }

    /**
     * Registers a response.
     *
     * Responses can be registered with or without a response body type:
     * ```
     * response<ResponseType>(HttpStatusCode) { ... }
     * response(HttpStatusCode) { ... }
     * ```
     *
     * #### Sample Usage
     *```
     * response<String>(status = HttpStatusCode.OK) {
     *      description = "Successfully retrieved the item."
     *
     *      header(name = "X-Rate-Limit") {
     *          description = "Number of allowed requests per period."
     *          required = true
     *      }
     *      link(operationId = "getNextItem") {
     *          description = "Link to the next item."
     *      }
     *
     *      // Only meaningful if multiple types are provided.
     *      // If omitted, defaults to `AnyOf`.
     *      composition = Composition.AnyOf
     *
     *      // Register additional type,
     *      // and also set it to the PDF ContentType.
     *      addType<YetAnotherType>(
     *          setOf(ContentType.Application.Pdf)
     *      )
     * }
     *```
     *
     * @param T The body primary type of the response.
     * @param status The [HttpStatusCode] code associated with this response.
     * @param contentType One or more [ContentType]s to associate with the response. Default: `JSON`.
     * @param configure A lambda receiver for configuring the [ResponseBuilder].
     *
     * @see [ResponseBuilder]
     * @see [HeaderBuilder]
     * @see [LinkBuilder]
     */
    @JvmName(name = "responseWithType")
    public inline fun <reified T : Any> ApiOperationBuilder.response(
        status: HttpStatusCode,
        contentType: Set<ContentType>? = null,
        configure: ResponseBuilder.() -> Unit = {}
    ) {
        val builder: ResponseBuilder = ResponseBuilder().apply {
            // Associate the primary type and ContentType.
            addType<T>(contentType = contentType)
            // Apply additional configurations, which can include more types.
            apply(configure)
        }

        val apiResponse: ApiResponse = builder.build(status = status)

        _config.responses.merge(
            status,
            apiResponse
        ) { existing, new ->
            existing.mergeWith(other = new)
        }
    }

    /**
     * Constructs the API operation metadata for the route endpoint.
     *
     * @param method The [HttpMethod] associated with the route.
     * @param endpointPath The URL path for the route.
     * @return The constructed [ApiOperation] instance.
     */
    internal fun build(method: HttpMethod, endpointPath: String): ApiOperation {
        // If no responses are defined, add a default NoContent response,
        // otherwise sort the responses by status code.
        val responses: LinkedHashMap<HttpStatusCode, ApiResponse> = if (_config.responses.isEmpty()) {
            linkedMapOf(HttpStatusCode.NoContent to ApiResponse.buildWithNoContent())
        } else {
            _config.responses.entries
                .sortedBy { it.key.value }
                .associateTo(LinkedHashMap()) { it.toPair() }
        }

        return ApiOperation(
            path = endpointPath,
            method = method,
            tags = _config.tags.takeIf { it.isNotEmpty() },
            summary = summary.trimOrNull(),
            description = description.trimOrNull(),
            operationId = operationId.trimOrNull(),
            parameters = _config.parameters.takeIf { it.isNotEmpty() },
            requestBody = _config.requestBody,
            responses = responses,
            securitySchemes = _securityConfig.securitySchemes.takeIf { it.isNotEmpty() },
            skipSecurity = _securityConfig.skipSecurity
        )
    }

    @PublishedApi
    internal class Config(private val endpoint: String) {
        /** Optional set of descriptive tags for categorizing the endpoint in API documentation. */
        val tags: TreeSet<String> = TreeSet(String.CASE_INSENSITIVE_ORDER)

        /**  Optional set of parameters detailing type, necessity, and location in the request. */
        var parameters: LinkedHashSet<ApiParameter> = linkedSetOf()

        /** Optional structure and type of the request body. */
        var requestBody: ApiRequestBody? = null

        /** Optional set of possible responses, outlining expected status codes and content types. */
        var responses: MutableMap<HttpStatusCode, ApiResponse> = mutableMapOf()

        /**
         * Adds a new path parameter to the API operation,
         * ensuring that the parameter name is unique.
         *
         * @param apiParameter The [ApiParameter] instance to add to the cache.
         * @throws KopapiException If an [ApiParameter] with the same name already exists.
         */
        fun addApiParameter(apiParameter: ApiParameter) {
            if (parameters.any { it.name.equals(other = apiParameter.name, ignoreCase = true) }) {
                throw KopapiException(
                    "Attempting to register parameter with name '${apiParameter.name}' more than once." +
                            "with the same API Operation: '$endpoint'."
                )
            }

            parameters.add(apiParameter)
        }
    }
}
