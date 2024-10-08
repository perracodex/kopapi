/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.builders.response

import io.github.perracodex.kopapi.dsl.ApiHeader
import io.github.perracodex.kopapi.dsl.ApiLink
import io.github.perracodex.kopapi.dsl.ApiMetadata
import io.github.perracodex.kopapi.dsl.ApiResponse
import io.github.perracodex.kopapi.utils.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.http.*
import kotlin.reflect.KType

/**
 * A builder for constructing a response in an API endpoint's metadata.
 *
 * @property description A description of the response content and what it represents.
 * @property contentType The [ContentType] of the response data, such as JSON or XML.
 *
 * @see [ApiMetadata.response]
 */
public data class ResponseBuilder(
    var contentType: ContentType = ContentType.Application.Json
) {
    var description: String by MultilineString()

    private val headersSet: MutableSet<ApiHeader> = mutableSetOf()
    private val linksSet: MutableSet<ApiLink> = mutableSetOf()

    /**
     * Builds an [ApiResponse] instance from the current builder state.
     *
     * @param status The [HttpStatusCode] code associated with this response.
     * @param type The [KType] of the parameter.
     * @return The constructed [ApiResponse] instance.
     */
    @PublishedApi
    internal fun build(status: HttpStatusCode, type: KType): ApiResponse {
        return ApiResponse(
            type = type,
            status = status,
            description = description.trimOrNull(),
            contentType = contentType,
            headers = headersSet.takeIf { it.isNotEmpty() },
            links = linksSet.takeIf { it.isNotEmpty() }
        )
    }

    /**
     * Adds a header to the response.
     *
     * #### Sample Usage
     * ```
     * header("X-Rate-Limit") {
     *     description = "Number of allowed requests per period."
     *     required = true
     * }
     * ```
     *
     * @param name The name of the header.
     * @param configure A lambda receiver for configuring the [ApiHeader].
     */
    public fun header(name: String, configure: ApiHeader.() -> Unit) {
        val header: ApiHeader = ApiHeader(name = name).apply(configure)
        headersSet.add(header)
    }

    /**
     * Adds a link to the response.
     *
     * #### Sample Usage
     * ```
     * link("getNextItem") {
     *     description = "Link to the next item."
     * }
     * ```
     *
     * @param operationId The name of an existing, resolvable OAS operation.
     * @param configure A lambda receiver for configuring the [ApiLink].
     */
    public fun link(operationId: String, configure: ApiLink.() -> Unit) {
        val link: ApiLink = ApiLink(operationId = operationId).apply(configure)
        linksSet.add(link)
    }
}
