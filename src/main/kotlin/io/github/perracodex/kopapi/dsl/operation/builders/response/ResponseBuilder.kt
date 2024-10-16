/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.response

import io.github.perracodex.kopapi.dsl.operation.builders.ApiOperationBuilder
import io.github.perracodex.kopapi.dsl.operation.builders.attributes.HeaderBuilder
import io.github.perracodex.kopapi.dsl.operation.builders.attributes.LinkBuilder
import io.github.perracodex.kopapi.dsl.operation.elements.ApiHeader
import io.github.perracodex.kopapi.dsl.operation.elements.ApiLink
import io.github.perracodex.kopapi.dsl.operation.elements.ApiResponse
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.http.*
import kotlin.reflect.KType

/**
 * A builder for constructing a response in an API endpoint's metadata.
 *
 * @property contentType The [ContentType] of the response data, such as JSON, XML, etc.
 * @property description A description of the response content and what it represents.
 *
 * @see [ApiOperationBuilder.response]
 */
public class ResponseBuilder {
    public var contentType: ContentType? = null
    public var description: String by MultilineString()

    private val headers: MutableSet<ApiHeader> = mutableSetOf()
    private val links: MutableSet<ApiLink> = mutableSetOf()

    /**
     * Builds an [ApiResponse] instance from the current builder state.
     *
     * @param status The [HttpStatusCode] code associated with this response.
     * @param type The [KType] of the parameter.
     * @return The constructed [ApiResponse] instance.
     */
    @PublishedApi
    internal fun build(status: HttpStatusCode, type: KType?): ApiResponse {
        return ApiResponse(
            type = type,
            status = status,
            description = description.trimOrNull(),
            contentType = contentType,
            headers = headers.takeIf { it.isNotEmpty() },
            links = links.takeIf { it.isNotEmpty() }
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
     * @param configure A lambda receiver for configuring the [HeaderBuilder].
     */
    public fun header(name: String, configure: HeaderBuilder.() -> Unit) {
        val header: ApiHeader = HeaderBuilder(name = name).apply(configure).build()
        addHeader(header = header)
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
     * @param configure A lambda receiver for configuring the [LinkBuilder].
     */
    public fun link(operationId: String, configure: LinkBuilder.() -> Unit) {
        val link: ApiLink = LinkBuilder(operationId = operationId).apply(configure).build()
        links.add(link)
    }

    /**
     * Adds a new [ApiHeader] instance to the cache, ensuring that the header name is unique
     *
     * @param header The [ApiHeader] instance to add to the cache.
     * @throws KopapiException If an [ApiHeader] with the same name already exists.
     */
    private fun addHeader(header: ApiHeader) {
        if (headers.any { it.name == header.name }) {
            throw KopapiException("Header with name '${header.name}' already exists within the same response.")
        }
        headers.add(header)
    }
}
