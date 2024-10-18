/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders

import io.github.perracodex.kopapi.dsl.common.SecuritySchemeConfigurable
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
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.string.SpacedString
import io.ktor.http.*
import java.util.*
import kotlin.reflect.KType
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
 * get("/items/{group_id}/{item_id?}") {
 *     // Handle GET request
 * } api {
 *     summary = "Retrieve data items."
 *     description = "Fetches all items for a group."
 *     tags("Items", "Data")
 *     pathParameter<Uuid>("group_id") { description = "The Id of the group." }
 *     queryParameter<String>("item_id") { description = "Optional item Id." }
 *     response<List<Item>>(HttpStatusCode.OK) { description = "Successful" }
 *     response(HttpStatusCode.NotFound) { description = "Data not found" }
 * }
 * ```
 *
 * @see [ApiOperation]
 */
public class ApiOperationBuilder internal constructor(
    @PublishedApi internal val endpoint: String
) : SecuritySchemeConfigurable() {
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
     */
    internal val tags: TreeSet<String> = TreeSet(String.CASE_INSENSITIVE_ORDER)

    /**
     * Optional set of parameters detailing type, necessity, and location in the request.
     */
    @PublishedApi
    internal var parameters: LinkedHashSet<ApiParameter> = linkedSetOf()

    /**
     * Optional structure and type of the request body.
     */
    @PublishedApi
    internal var requestBody: ApiRequestBody? = null

    /**
     * Optional set of possible responses, outlining expected status codes and content types.
     */
    @PublishedApi
    internal var responses: LinkedHashMap<String, ApiResponse> = linkedMapOf()

    /**
     * Optional set of descriptive tags for categorizing the endpoint in API documentation.
     *
     * Declaring multiple `tags` will append all the of them to the existing list.
     * Repeated tags are discarded in a case-insensitive manner.
     *
     * #### Sample Usage
     * ```
     * tags("Items", "Data")
     * ```
     *
     * @see [summary]
     * @see [description]
     */
    public fun ApiOperationBuilder.tags(vararg tags: String) {
        this.tags.addAll(tags.map { it.trim() }.filter { it.isNotBlank() })
    }

    /**
     * Registers a path parameter.
     *
     * #### Sample Usage
     * ```
     * pathParameter<String>(name = "id") {
     *     description = "The unique identifier of the item."
     * }
     * ```
     *
     * @param T The type of the parameter.
     * @param name The name of the parameter as it appears in the URL path.
     * @param configure A lambda receiver for configuring the [PathParameterBuilder].
     *
     * @see [PathParameterBuilder]
     * @see [cookieParameter]
     * @see [headerParameter]
     * @see [queryParameter]
     * @see [requestBody]
     */
    public inline fun <reified T : Any> ApiOperationBuilder.pathParameter(
        name: String,
        configure: PathParameterBuilder.() -> Unit = {}
    ) {
        val builder: PathParameterBuilder = PathParameterBuilder().apply(configure)
        val parameter: ApiParameter = builder.build(name = name, type = typeOf<T>())
        addApiParameter(apiParameter = parameter)
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
        addApiParameter(apiParameter = parameter)
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
        addApiParameter(apiParameter = parameter)
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
        addApiParameter(apiParameter = parameter)
    }

    /**
     * Adds a new path parameter to the API operation,
     * ensuring that the parameter name is unique.
     *
     * @param apiParameter The [ApiParameter] instance to add to the cache.
     * @throws KopapiException If an [ApiParameter] with the same name already exists.
     */
    @PublishedApi
    internal fun addApiParameter(apiParameter: ApiParameter) {
        if (parameters.any { it.name.equals(other = apiParameter.name, ignoreCase = true) }) {
            throw KopapiException(
                "Attempting to register parameter with name '${apiParameter.name}' more than once " +
                        "in the operation '$endpoint'."
            )
        }

        parameters.add(apiParameter)
    }

    /**
     * Registers a request body.
     *
     * #### Sample Usage
     * ```
     * requestBody<MyRequestBodyType> {
     *     description = "The data required to create a new item."
     *     required = true
     *     contentType = ContentType.Application.Json
     * }
     * ```
     *
     * @param T The type of the request body.
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
        configure: RequestBodyBuilder.() -> Unit = {}
    ) {
        if (requestBody != null) {
            throw KopapiException(
                "Only one RequestBody can be defined per API endpoint. " +
                        "Attempted to define multiple RequestBodies in '${this.endpoint}'"
            )
        }

        val builder: RequestBodyBuilder = RequestBodyBuilder().apply(configure)
        requestBody = builder.build(type = typeOf<T>())
    }

    /**
     * Registers a response with a body.
     *
     * #### Sample Usage
     *```
     * response<ResponseType>(status = HttpStatusCode.OK) {
     *     description = "Successfully retrieved the item."
     *     contentType = setOf(
     *          ContentType.Application.Json,
     *          ContentType.Application.Xml
     *      )
     * }
     * ```
     *
     * #### Adding Headers and Links
     * ```
     * response<ResponseType>(status = HttpStatusCode.OK) {
     *     description = "Successfully retrieved the item."
     *     header(name = "X-Rate-Limit") {
     *         description = "Number of allowed requests per period."
     *         required = true
     *     }
     *     link(operationId = "getNextItem") {
     *         description = "Link to the next item."
     *     }
     * }
     * ```
     *
     * @param T The body type of the response.
     * @param status The [HttpStatusCode] code associated with this response.
     * @param configure A lambda receiver for configuring the [ResponseBuilder].
     *
     * @see [ResponseBuilder]
     * @see [HeaderBuilder]
     * @see [LinkBuilder]
     */
    @JvmName(name = "responseWithType")
    public inline fun <reified T : Any> ApiOperationBuilder.response(
        status: HttpStatusCode,
        configure: ResponseBuilder.() -> Unit = {}
    ) {
        val type: KType? = when (T::class) {
            Unit::class, Nothing::class, Any::class -> null
            else -> typeOf<T>()
        }
        val builder: ResponseBuilder = ResponseBuilder().apply(configure)
        val response: ApiResponse = builder.build(status = status, type = type)

        responses[status.value.toString()] = response
    }

    /**
     * Registers a response with no response body.
     * Assuming there is only a [HttpStatusCode] with no associated type.
     *
     * #### Sample Usage
     *```
     * response(status = HttpStatusCode.NotFound) {
     *     description = "The item was not found."
     * }
     * ```
     *
     * #### Adding Headers and Links
     * ```
     * response(status = HttpStatusCode.OK) {
     *     description = "Successfully retrieved the item."
     *     header(name = "X-Rate-Limit") {
     *         description = "Number of allowed requests per period."
     *         required = true
     *     }
     *     link(operationId = "getNextItem") {
     *         description = "Link to the next item."
     *     }
     * }
     * ```
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
        securitySchemes.clear()
        skipSecurity = true
    }
}
