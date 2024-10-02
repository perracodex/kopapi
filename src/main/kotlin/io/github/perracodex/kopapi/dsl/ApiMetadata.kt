/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl

import io.ktor.http.*
import java.util.*
import kotlin.reflect.typeOf

/**
 * Provides structured metadata for defining and documenting API endpoints.
 * This class is designed to be used in conjunction with Ktor routes,
 * enabling detailed descriptions of endpoint behaviors, parameters, responses, and operational characteristics.
 *
 * Usage involves defining a Ktor route and attaching API metadata using the `Route.api` infix
 * function to enrich the route with operational details and documentation specifications.
 *
 * @property path The URL path for the endpoint, derived from the Ktor route.
 * @property method Specifies the [HttpMethod] (GET, POST, PUT, etc.) for the endpoint, derived from the Ktor route.
 * @property summary Optional short description of the endpoint's purpose.
 * @property description Optional detailed explanation of the endpoint and its functionality.
 * @property tags Optional set of descriptive tags for categorizing the endpoint in API documentation.
 * @property parameters Optional set of parameters detailing type, necessity, and location in the request.
 * @property requestBody Optional structure and type of the request body.
 * @property responses Optional set of possible responses, outlining expected status codes and content types.
 * @property securities Optional set of security schemes detailing the authentication requirements for the endpoint.
 */
@Suppress("MemberVisibilityCanBePrivate")
@ConsistentCopyVisibility
public data class ApiMetadata internal constructor(
    @PublishedApi internal val path: String,
    @PublishedApi internal val method: HttpMethod,
    @PublishedApi internal var summary: String? = null,
    @PublishedApi internal var description: String? = null,
    @PublishedApi internal var tags: Set<String>? = null,
    @PublishedApi internal var parameters: LinkedHashSet<ApiParameter>? = null,
    @PublishedApi internal var requestBody: ApiRequestBody? = null,
    @PublishedApi internal var responses: LinkedHashSet<ApiResponse>? = null,
    @PublishedApi internal var securities: LinkedHashSet<ApiSecurity>? = null
) {
    init {
        require(path.isNotBlank()) { "Path must not be empty." }
    }

    /**
     * Sets a short description of the endpoint's purpose.
     *
     * @param text The description to set.
     */
    public fun summary(text: String) {
        this.summary = text.trim().takeIf { it.isNotBlank() }
    }

    /**
     * Sets a short description of the endpoint's purpose.
     *
     * @param text The description to set.
     */
    public fun description(text: String) {
        this.description = text.trim().takeIf { it.isNotBlank() }
    }

    /**
     * Adds tags to the API endpoint's metadata.
     *
     * @param tags A list of tags to categorize the endpoint.
     */
    public fun tags(vararg tags: String) {
        // Handle tags to ensure they are unique regardless of case variations,
        // while retaining the original casing of the first occurrence of each tag.
        // If other tags were already added, the new ones will be appended to the set.
        val caseInsensitiveSet: TreeSet<String> = TreeSet(String.CASE_INSENSITIVE_ORDER)
        this.tags?.let { caseInsensitiveSet.addAll(it) }
        caseInsensitiveSet.addAll(tags.map { it.trim() }.filter { it.isNotBlank() })
        this.tags = LinkedHashSet(caseInsensitiveSet)
    }

    /** Internal helper function to add a parameter to the API endpoint's metadata. */
    @PublishedApi
    internal fun addParameter(parameter: ApiParameter) {
        val parameters: LinkedHashSet<ApiParameter> = parameters
            ?: linkedSetOf<ApiParameter>().also { parameters = it }
        parameters.add(parameter)
    }

    /** Internal helper function to add a security scheme to the API endpoint's metadata. */
    @PublishedApi
    internal fun addSecurity(security: ApiSecurity) {
        val securities: LinkedHashSet<ApiSecurity> = securities
            ?: linkedSetOf<ApiSecurity>().also { securities = it }
        securities.add(security)
    }

    /**
     * Adds a path parameter to the API endpoint's metadata.
     *
     * @param name The name of the parameter as it appears in the URL path.
     * @param description A description of the parameter's purpose and usage.
     * @param required Indicates whether the parameter is mandatory for the API call.
     * @param defaultValue The default value for the parameter if one is not provided.
     * @param style The style in which the parameter is serialized in the URL.
     * @param deprecated Indicates if the header is deprecated and should be avoided.
     */
    public inline fun <reified T : Any> pathParameter(
        name: String,
        description: String? = null,
        required: Boolean = true,
        defaultValue: Any? = null,
        style: ApiParameter.Style = ApiParameter.Style.SIMPLE,
        deprecated: Boolean = false
    ) {
        addParameter(
            ApiParameter(
                type = typeOf<T>(),
                location = ApiParameter.Location.PATH,
                name = name.trim(),
                description = description.trimNullable(),
                required = required,
                defaultValue = defaultValue,
                style = style,
                deprecated = deprecated,
            )
        )
    }

    /**
     * Adds a query parameter to the API endpoint's metadata.
     *
     * @param name The name of the parameter as it appears in the query string.
     * @param description Explains the role and expected values for the parameter.
     * @param required Indicates whether the parameter is necessary for the API call.
     * @param defaultValue The default value if none is provided.
     * @param explode Whether to send arrays and objects as separate parameters.
     * @param style The serialization style of the query string.
     * @param deprecated Indicates if the header is deprecated and should be avoided.
     */
    public inline fun <reified T : Any> queryParameter(
        name: String,
        description: String? = null,
        required: Boolean = true,
        defaultValue: Any? = null,
        explode: Boolean = false,
        style: ApiParameter.Style = ApiParameter.Style.FORM,
        deprecated: Boolean = false
    ) {
        addParameter(
            ApiParameter(
                type = typeOf<T>(),
                location = ApiParameter.Location.QUERY,
                name = name.trim(),
                description = description.trimNullable(),
                required = required,
                defaultValue = defaultValue,
                explode = explode,
                style = style,
                deprecated = deprecated
            )
        )
    }

    /**
     * Adds a header parameter to the API endpoint's metadata.
     *
     * @param name The name of the header parameter.
     * @param description Details the usage and significance of the header.
     * @param required Specifies if the header must be present.
     * @param defaultValue A fallback value for the header if not explicitly provided.
     * @param explode Controls how arrays and objects are serialized in the header.
     * @param style The serialization style, typically simple for headers.
     * @param deprecated Indicates if the header is deprecated and should be avoided.
     */
    public inline fun <reified T : Any> headerParameter(
        name: String,
        description: String? = null,
        required: Boolean = true,
        defaultValue: Any? = null,
        explode: Boolean = false,
        style: ApiParameter.Style = ApiParameter.Style.SIMPLE,
        deprecated: Boolean = false
    ) {
        addParameter(
            ApiParameter(
                type = typeOf<T>(),
                location = ApiParameter.Location.HEADER,
                name = name.trim(),
                description = description.trimNullable(),
                required = required,
                defaultValue = defaultValue,
                explode = explode,
                style = style,
                deprecated = deprecated
            )
        )
    }

    /**
     * Adds a cookie parameter to the API endpoint's metadata.
     *
     * @param name The name of the cookie.
     * @param description A brief on the cookie's purpose and its values.
     * @param required Indicates if the cookie is essential for the request.
     * @param defaultValue Default cookie value if none is provided.
     * @param explode Whether individual cookies should be sent for each value of an array or object.
     * @param style The serialization style, typically 'form' for cookies.
     * @param deprecated Indicates if the header is deprecated and should be avoided.
     */
    public inline fun <reified T : Any> cookieParameter(
        name: String,
        description: String? = null,
        required: Boolean = true,
        defaultValue: Any? = null,
        explode: Boolean = false,
        style: ApiParameter.Style = ApiParameter.Style.FORM,
        deprecated: Boolean = false
    ) {
        addParameter(
            ApiParameter(
                type = typeOf<T>(),
                location = ApiParameter.Location.COOKIE,
                name = name.trim(),
                description = description.trimNullable(),
                required = required,
                defaultValue = defaultValue,
                explode = explode,
                style = style,
                deprecated = deprecated,
            )
        )
    }

    /**
     * Defines and registers a request body for the API endpoint.
     *
     * @param description Details the type of data expected in the request body.
     * @param required Specifies whether the request body is required for the API operation.
     * @param contentType The [ContentType] of the data being sent.
     * @param deprecated Indicates if the header is deprecated and should be avoided.
     */
    public inline fun <reified T : Any> requestBody(
        description: String? = null,
        required: Boolean = true,
        contentType: ContentType = ContentType.Application.Json,
        deprecated: Boolean = false
    ) {
        require(value = this.requestBody == null) {
            "Only one request body is allowed per API endpoint. " +
                    "Found '${this.requestBody}' already defined in '${this.path}' / ${this.method}"
        }
        requestBody = ApiRequestBody(
            type = typeOf<T>(),
            description = description.trimNullable(),
            required = required,
            contentType = contentType,
            deprecated = deprecated
        )
    }

    /**
     * Adds a response configuration to the API endpoint's metadata,
     *
     * @param status The [HttpStatusCode] code associated with this response.
     * @param description A description of the response content and what it represents.
     * @param contentType The [ContentType] of the response data, such as JSON or XML.
     * @param header A list of [ApiHeader] objects representing the headers that may be included in the response.
     * @param links A list of [ApiLink] objects representing hypermedia links associated with the response.
     */
    @JvmName(name = "responseWithType")
    public inline fun <reified T : Any> response(
        status: HttpStatusCode = HttpStatusCode.OK,
        description: String? = null,
        contentType: ContentType = ContentType.Application.Json,
        header: List<ApiHeader>? = null,
        links: List<ApiLink>? = null
    ) {
        val responses: LinkedHashSet<ApiResponse> = responses
            ?: linkedSetOf<ApiResponse>().also { responses = it }

        responses.add(
            ApiResponse(
                type = typeOf<T>(),
                status = status,
                description = description.trimNullable(),
                contentType = contentType,
                headers = header.takeIf { it?.isNotEmpty() == true },
                links = links.takeIf { it?.isNotEmpty() == true }
            )
        )
    }

    /**
     * Adds a response configuration to the API endpoint's metadata,
     * assuming there is no response type.
     *
     * @param status The [HttpStatusCode] code associated with this response.
     * @param description A description of the response content and what it represents.
     * @param contentType The [ContentType] of the response data, such as JSON or XML.
     * @param header A list of [ApiHeader] objects representing the headers that may be included in the response.
     * @param links A list of [ApiLink] objects representing hypermedia links associated with the response.
     */
    @JvmName(name = "responseWithoutType")
    public fun response(
        status: HttpStatusCode = HttpStatusCode.OK,
        description: String? = null,
        contentType: ContentType = ContentType.Application.Json,
        header: List<ApiHeader>? = null,
        links: List<ApiLink>? = null
    ) {
        response<Unit>(
            status = status,
            description = description.trimNullable(),
            contentType = contentType,
            header = header,
            links = links
        )
    }

    /**
     * Adds an HTTP security scheme to the API metadata (e.g., Basic, Bearer).
     *
     * @param name The name of the security scheme.
     * @param description A description of the security scheme.
     * @param httpType The [ApiSecurity.HttpType] of the security scheme.
     */
    public fun httpSecurity(
        name: String,
        description: String? = null,
        httpType: ApiSecurity.HttpType
    ) {
        addSecurity(
            ApiSecurity(
                name = name.trim(),
                description = description.trimNullable(),
                scheme = ApiSecurity.Scheme.HTTP,
                httpType = httpType
            )
        )
    }

    /**
     * Adds an API key security scheme to the API metadata.
     *
     * @param name The name of the security scheme.
     * @param description A description of the security scheme.
     * @param location The [ApiSecurity.Location] where the API key is passed.
     */
    public fun apiKeySecurity(
        name: String,
        description: String? = null,
        location: ApiSecurity.Location
    ) {
        addSecurity(
            ApiSecurity(
                name = name.trim(),
                description = description.trimNullable(),
                scheme = ApiSecurity.Scheme.API_KEY,
                location = location
            )
        )
    }

    /**
     * Adds an OAuth2 security scheme to the API metadata.
     *
     * @param name The name of the security scheme.
     * @param description A description of the security scheme.
     */
    public fun oauth2Security(
        name: String,
        description: String? = null
    ) {
        addSecurity(
            ApiSecurity(
                name = name.trim(),
                description = description.trimNullable(),
                scheme = ApiSecurity.Scheme.OAUTH2
            )
        )
    }

    /**
     * Adds an OpenID Connect security scheme to the API metadata.
     *
     * @param name The name of the security scheme.
     * @param description A description of the security scheme.
     * @param openIdConnectUrl The URL for the OpenID Connect configuration.
     */
    public fun openIdConnectSecurity(
        name: String,
        description: String? = null,
        openIdConnectUrl: Url
    ) {
        addSecurity(
            ApiSecurity(
                name = name.trim(),
                description = description.trimNullable(),
                scheme = ApiSecurity.Scheme.OPENID_CONNECT,
                openIdConnectUrl = openIdConnectUrl
            )
        )
    }
}


/**
 * Trim a nullable string, returning null if the trimmed result is empty.
 */
@PublishedApi
internal fun String?.trimNullable(): String? {
    return this?.trim().takeIf { it?.isNotBlank() == true }
}
