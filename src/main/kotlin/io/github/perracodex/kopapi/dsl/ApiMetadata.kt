/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl

import io.ktor.http.*
import kotlin.reflect.typeOf

/**
 * Represents the metadata of an API endpoint.
 *
 * @property summary A brief summary of what the API endpoint does.
 * @property description A detailed description of the API endpoint.
 * @property tags A list of tags to categorize and organize the endpoint within the API documentation.
 * @property path The URL path of the API endpoint.
 * @property method The [HttpMethod] (e.g., GET, POST, PUT) applicable to the endpoint.
 * @property parameters A list of parameters used by the endpoint, detailing where each is located (e.g., path, query).
 * @property requestBody Information about the request body expected by the endpoint, if applicable.
 * @property responses A list of possible responses from the endpoint, including status codes and data types.
 * @property security A list of security schemes required to access the endpoint.
 */
public data class ApiMetadata(
    var summary: String? = null,
    var description: String? = null,
    var tags: List<String> = emptyList(),
    @PublishedApi internal var path: String,
    @PublishedApi internal var method: HttpMethod,
    @PublishedApi internal val parameters: MutableList<ApiParameter> = mutableListOf(),
    @PublishedApi internal var requestBody: ApiRequestBody? = null,
    @PublishedApi internal val responses: MutableList<ApiResponse> = mutableListOf(),
    @PublishedApi internal val security: MutableList<ApiSecurity> = mutableListOf()
) {
    init {
        require(path.isNotEmpty()) {
            "Path must not be empty."
        }

        summary = summary?.trim()
        description = description?.trim()
        tags = tags.map { it.trim() }
        path = path.trim()
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
        parameters.add(
            ApiParameter(
                type = typeOf<T>(),
                location = ApiParameter.Location.PATH,
                name = name.trim(),
                description = description?.trim(),
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
        parameters.add(
            ApiParameter(
                type = typeOf<T>(),
                location = ApiParameter.Location.QUERY,
                name = name.trim(),
                description = description?.trim(),
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
        parameters.add(
            ApiParameter(
                type = typeOf<T>(),
                location = ApiParameter.Location.HEADER,
                name = name.trim(),
                description = description?.trim(),
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
        parameters.add(
            ApiParameter(
                type = typeOf<T>(),
                location = ApiParameter.Location.COOKIE,
                name = name.trim(),
                description = description?.trim(),
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
        requestBody = ApiRequestBody(
            type = typeOf<T>(),
            description = description?.trim(),
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
    public inline fun <reified T : Any> response(
        status: HttpStatusCode = HttpStatusCode.OK,
        description: String? = null,
        contentType: ContentType = ContentType.Application.Json,
        header: List<ApiHeader>? = null,
        links: List<ApiLink>? = null
    ) {
        responses.add(
            ApiResponse(
                type = typeOf<T>(),
                status = status,
                description = description?.trim(),
                contentType = contentType,
                headers = header.takeIf { it?.isNotEmpty() == true },
                links = links.takeIf { it?.isNotEmpty() == true }
            )
        )
    }

    /**
     * Adds a security scheme to the API endpoint's metadata.
     *
     * @property name The name of the security scheme.
     * @property description A description of the security scheme.
     * @property scheme The type of security scheme (e.g., HTTP, API Key, OAuth2, etc.).
     * @property httpType The HTTP type of security scheme (only applicable when the scheme is HTTP).
     * @property location The location where the API key is passed (only applicable for API Key schemes).
     */
    public fun security(
        name: String,
        description: String? = null,
        scheme: ApiSecurity.Scheme,
        httpType: ApiSecurity.HttpType? = null,// Only applicable for HTTP scheme.
        location: ApiSecurity.Location? = null // Only required for API_KEY scheme
    ) {
        security.add(
            ApiSecurity(
                name = name.trim(),
                description = description?.trim(),
                scheme = scheme,
                location = location,
                httpType = httpType
            )
        )
    }
}
