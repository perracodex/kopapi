/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl

import io.github.perracodex.kopapi.dsl.builders.parameter.CookieParameterBuilder
import io.github.perracodex.kopapi.dsl.builders.parameter.HeaderParameterBuilder
import io.github.perracodex.kopapi.dsl.builders.parameter.PathParameterBuilder
import io.github.perracodex.kopapi.dsl.builders.parameter.QueryParameterBuilder
import io.github.perracodex.kopapi.dsl.builders.request.RequestBodyBuilder
import io.github.perracodex.kopapi.dsl.builders.response.ResponseBuilder
import io.github.perracodex.kopapi.dsl.builders.security.*
import io.github.perracodex.kopapi.dsl.types.AuthenticationMethod
import io.github.perracodex.kopapi.dsl.types.SecurityLocation
import io.github.perracodex.kopapi.utils.MultilineString
import io.github.perracodex.kopapi.utils.SpacedString
import io.ktor.http.*
import kotlin.reflect.typeOf

/**
 * Provides structured metadata for defining and documenting API endpoints.
 * This class is designed to be used in conjunction with Ktor routes,
 * enabling detailed descriptions of endpoint behaviors, parameters, responses, and operational characteristics.
 *
 * Usage involves defining a Ktor route and attaching API metadata using the `Route.api` infix
 * function to enrich the route with operational details and documentation specifications.
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
 * - [pathParameter]: Adds a path parameter to the API endpoint's metadata.
 * - [queryParameter]: Adds a query parameter to the API endpoint's metadata.
 * - [headerParameter]: Adds a header parameter to the API endpoint's metadata.
 * - [cookieParameter]: Adds a cookie parameter to the API endpoint's metadata.
 *
 * #### Sample Usage
 * ```
 * get("/items/{group_id}/{item_id?}") {
 *     // Handle GET request
 * } api {
 *     summary = "Retrieve data items."
 *     description = "Fetches all items for a group."
 *     tags = Tags("Items", "Data")
 *     pathParameter<Uuid>("group_id") { description = "The Id of the group." }
 *     queryParameter<String>("item_id") { description = "Optional item Id." }
 *     response<List<Item>>(HttpStatusCode.OK) { description = "Successful" }
 *     response(HttpStatusCode.NotFound) { description = "Data not found" }
 * }
 * ```
 *
 * @property path The URL path for the endpoint. Automatically derived from the Ktor route.
 * @property method The endpoint [HttpMethod] (GET, POST, etc.). Automatically derived from the Ktor route.
 * @property summary Optional short description of the endpoint's purpose.
 * @property description Optional detailed explanation of the endpoint and its functionality.
 * @property tags Optional set of descriptive [Tags] for categorizing the endpoint in API documentation.
 */
public data class ApiMetadata internal constructor(
    @PublishedApi internal val path: String,
    @PublishedApi internal val method: HttpMethod,
) {
    init {
        require(path.isNotBlank()) { "Endpoint path must not be empty." }
    }

    public var summary: String by SpacedString()
    public var description: String by MultilineString()
    public var tags: Tags by TagsDelegate()

    /**
     * Optional set of parameters detailing type, necessity, and location in the request.
     */
    internal var parameters: LinkedHashSet<ApiParameter>? = null

    /**
     * Optional structure and type of the request body.
     */
    @PublishedApi
    internal var requestBody: ApiRequestBody? = null

    /**
     * Optional set of possible responses, outlining expected status codes and content types.
     */
    @PublishedApi
    internal var responses: LinkedHashSet<ApiResponse>? = null

    /**
     * Optional set of security schemes detailing the authentication requirements for the endpoint.
     */
    internal var security: LinkedHashSet<ApiSecurity>? = null

    /** Internal helper function to add a parameter to the API endpoint's metadata. */
    @PublishedApi
    internal fun addParameter(parameter: ApiParameter) {
        val parameters: LinkedHashSet<ApiParameter> = parameters
            ?: linkedSetOf<ApiParameter>().also { parameters = it }
        parameters.add(parameter)
    }

    /** Internal helper function to add a security scheme to the API endpoint's metadata. */
    @PublishedApi
    internal fun addSecurity(scheme: ApiSecurity) {
        val securities: LinkedHashSet<ApiSecurity> = security
            ?: linkedSetOf<ApiSecurity>().also { security = it }
        securities.add(scheme)
    }

    /**
     * Adds a path parameter to the API endpoint's metadata.
     *
     * #### Sample Usage
     * ```
     * pathParameter<String>("id") {
     *     description = "The unique identifier of the item."
     * }
     * ```
     *
     * @param T The type of the parameter.
     * @param name The name of the parameter as it appears in the URL path.
     * @param configure A lambda receiver for configuring the [PathParameterBuilder].
     *
     * @see [PathParameterBuilder]
     */
    public inline fun <reified T : Any> ApiMetadata.pathParameter(name: String, configure: PathParameterBuilder.() -> Unit = {}) {
        val builder: PathParameterBuilder = PathParameterBuilder().apply(configure)
        addParameter(parameter = builder.build(name = name, type = typeOf<T>()))
    }

    /**
     * Adds a query parameter to the API endpoint's metadata.
     *
     * #### Sample Usage
     * ```
     * queryParameter<Int>("page") {
     *     description = "The page number to retrieve."
     * }
     * queryParameter<Int>("size") {
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
     */
    public inline fun <reified T : Any> ApiMetadata.queryParameter(name: String, configure: QueryParameterBuilder.() -> Unit = {}) {
        val builder: QueryParameterBuilder = QueryParameterBuilder().apply(configure)
        addParameter(parameter = builder.build(name = name, type = typeOf<T>()))
    }

    /**
     * Adds a header parameter to the API endpoint's metadata.
     *
     * #### Sample Usage
     * ```
     * headerParameter<String>("X-Custom-Header") {
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
     */
    public inline fun <reified T : Any> ApiMetadata.headerParameter(name: String, configure: HeaderParameterBuilder.() -> Unit = {}) {
        val builder: HeaderParameterBuilder = HeaderParameterBuilder().apply(configure)
        addParameter(parameter = builder.build(name = name, type = typeOf<T>()))
    }

    /**
     * Adds a cookie parameter to the API endpoint's metadata.
     *
     * #### Sample Usage
     * ```
     * cookieParameter<String>("session") {
     *     description = "The session ID for authentication."
     * }
     * ```
     *
     * @param T The type of the parameter.
     * @param name The name of the parameter as it appears in the URL path.
     * @param configure A lambda receiver for configuring the [CookieParameterBuilder].
     *
     * @see [CookieParameterBuilder]
     */
    public inline fun <reified T : Any> ApiMetadata.cookieParameter(name: String, configure: CookieParameterBuilder.() -> Unit = {}) {
        val builder: CookieParameterBuilder = CookieParameterBuilder().apply(configure)
        addParameter(parameter = builder.build(name = name, type = typeOf<T>()))
    }

    /**
     * Adds a request body to the API endpoint's metadata.
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
     */
    public inline fun <reified T : Any> ApiMetadata.requestBody(configure: RequestBodyBuilder.() -> Unit = {}) {
        require(value = (requestBody == null)) {
            "Only one request body is allowed per API endpoint. " +
                    "Found '$requestBody' already defined in '${this.path}' / ${this.method}"
        }
        val builder: RequestBodyBuilder = RequestBodyBuilder().apply(configure)
        requestBody = builder.build(typeOf<T>())
    }

    /**
     * Adds a response, with a body, to the API endpoint's metadata.
     *
     * #### Sample Usage
     *```
     * response<ResponseType>(HttpStatusCode.OK) {
     *     description = "Successfully retrieved the item."
     *     contentType = ContentType.Application.Json
     * }
     * ```
     *
     * #### Adding Headers and Links
     * ```
     * response<ResponseType>(HttpStatusCode.OK) {
     *     description = "Successfully retrieved the item."
     *     header("X-Rate-Limit") {
     *         description = "Number of allowed requests per period."
     *         required = true
     *     }
     *     link("getNextItem") {
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
     */
    @JvmName(name = "responseWithType")
    public inline fun <reified T : Any> ApiMetadata.response(status: HttpStatusCode, configure: ResponseBuilder.() -> Unit = {}) {
        val responses: LinkedHashSet<ApiResponse> = responses
            ?: linkedSetOf<ApiResponse>().also { responses = it }
        val builder: ResponseBuilder = ResponseBuilder().apply(configure)
        responses.add(builder.build(status = status, type = typeOf<T>()))
    }

    /**
     * Adds a response, with no response body, to the API endpoint's metadata.
     * Assuming there is only a [HttpStatusCode] with no associated type.
     *
     * #### Sample Usage
     *```
     * response(HttpStatusCode.NotFound) {
     *     description = "The item was not found."
     * }
     * ```
     *
     * #### Adding Headers and Links
     * ```
     * response(HttpStatusCode.OK) {
     *     description = "Successfully retrieved the item."
     *     header("X-Rate-Limit") {
     *         description = "Number of allowed requests per period."
     *         required = true
     *     }
     *     link("getNextItem") {
     *         description = "Link to the next item."
     *     }
     * }
     * ```
     *
     * @param status The [HttpStatusCode] code associated with this response.
     * @param configure A lambda receiver for configuring the [ResponseBuilder].
     *
     * @see [ResponseBuilder]
     */
    @JvmName(name = "responseWithoutType")
    public fun ApiMetadata.response(status: HttpStatusCode, configure: ResponseBuilder.() -> Unit = {}) {
        response<Unit>(status = status, configure = configure)
    }

    /**
     * Adds an HTTP security scheme to the API metadata (e.g., Basic, Bearer).
     *
     * #### Sample Usage
     * ```
     * httpSecurity("BasicAuth", AuthenticationMethod.BASIC) {
     *     description = "Basic Authentication"
     * }
     * ```
     *
     * @param name The name of the security scheme.
     * @param method The [AuthenticationMethod] of the security scheme.
     * @param configure A lambda receiver for configuring the [HttpSecurityBuilder].
     *
     * @see [HttpSecurityBuilder]
     */
    public fun ApiMetadata.httpSecurity(
        name: String,
        method: AuthenticationMethod,
        configure: HttpSecurityBuilder.() -> Unit = {}
    ) {
        val builder: HttpSecurityBuilder = HttpSecurityBuilder().apply(configure)
        addSecurity(scheme = builder.build(name = name, method = method))
    }

    /**
     * Adds an API key security scheme to the API metadata.
     *
     * #### Sample Usage
     * ```
     * apiKeySecurity("API Key", APIKeyLocation.HEADER) {
     *     description = "API Key Authentication via header."
     * }
     * ```
     *
     * @param name The name of the security scheme.
     * @param location The [SecurityLocation] where the API key is passed.
     * @param configure A lambda receiver for configuring the [ApiKeySecurityBuilder].
     *
     * @see [ApiKeySecurityBuilder]
     */
    public fun ApiMetadata.apiKeySecurity(
        name: String,
        location: SecurityLocation,
        configure: ApiKeySecurityBuilder.() -> Unit = {}
    ) {
        val builder: ApiKeySecurityBuilder = ApiKeySecurityBuilder().apply(configure)
        addSecurity(scheme = builder.build(name = name, location = location))
    }

    /**
     * Adds an OAuth2 security scheme to the API metadata.
     *
     * #### Sample Usage
     * ```
     * oauth2Security("OAuth2") {
     *     description = "OAuth2 Authentication."
     * }
     * ```
     *
     * @param name The name of the security scheme.
     * @param configure A lambda receiver for configuring the [OAuth2SecurityBuilder].
     *
     * @see [OAuth2SecurityBuilder]
     */
    public fun ApiMetadata.oauth2Security(
        name: String,
        configure: OAuth2SecurityBuilder.() -> Unit = {}
    ) {
        val builder: OAuth2SecurityBuilder = OAuth2SecurityBuilder().apply(configure)
        addSecurity(scheme = builder.build(name = name))
    }

    /**
     * Adds an OpenID Connect security scheme to the API metadata.
     *
     * #### Sample Usage
     * ```
     * openIdConnectSecurity("OpenID") {
     *     description = "OpenID Connect Authentication."
     *     url = Url("https://example.com/.well-known/openid-configuration")
     * }
     * ```
     *
     * @param name The name of the security scheme.
     * @param url The [Url] for the OpenID Connect configuration.
     * @param configure A lambda receiver for configuring the [OpenIdConnectSecurityBuilder].
     *
     * @see [OpenIdConnectSecurityBuilder]
     */
    public fun ApiMetadata.openIdConnectSecurity(
        name: String,
        url: Url,
        configure: OpenIdConnectSecurityBuilder.() -> Unit = {}
    ) {
        val builder: OpenIdConnectSecurityBuilder = OpenIdConnectSecurityBuilder().apply(configure)
        addSecurity(scheme = builder.build(name = name, url = url))
    }

    /**
     * Adds a Mutual TLS security scheme to the API metadata.
     *
     * #### Sample Usage
     * ```
     * mutualTLSSecurity("MutualTLS") {
     *     description = "Mutual TLS Authentication."
     * }
     * ```
     *
     * @param name The name of the security scheme.
     * @param configure A lambda receiver for configuring the [MutualTLSSecurityBuilder].
     *
     * @see [MutualTLSSecurityBuilder]
     */
    public fun ApiMetadata.mutualTLSSecurity(
        name: String,
        configure: MutualTLSSecurityBuilder.() -> Unit = {}
    ) {
        val builder: MutualTLSSecurityBuilder = MutualTLSSecurityBuilder().apply(configure)
        addSecurity(scheme = builder.build(name = name))
    }
}
