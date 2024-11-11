/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.common.header.configurable

import io.github.perracodex.kopapi.dsl.common.header.HeadersBuilder
import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.elements.ApiHeader
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.utils.sanitize

/**
 * Handles the registration of headers.
 */
@PublishedApi
@KopapiDsl
internal class HeaderDelegate internal constructor() : IHeaderConfigurable {
    /** Holds the headers associated with the response. */
    private val headers: MutableMap<String, ApiHeader> = mutableMapOf()

    override fun headers(builder: HeadersBuilder.() -> Unit) {
        val headersBuilder: HeadersBuilder = HeadersBuilder().apply(builder)
        headersBuilder.build().forEach { addHeader(name = it.key, header = it.value) }
    }

    /**
     * Adds a new [ApiHeader] instance to the cache, ensuring that the header name is unique
     *
     * @param name The unique name of the header.
     * @param header The [ApiHeader] instance to add to the cache.
     * @throws KopapiException If an [ApiHeader] with the same name already exists.
     */
    private fun addHeader(name: String, header: ApiHeader) {
        val headerName: String = name.sanitize()

        if (headerName.isBlank()) {
            throw KopapiException("Header name must not be blank.")
        }
        if (headers.any { it.key.equals(other = headerName, ignoreCase = true) }) {
            throw KopapiException("Header with name '${headerName}' already exists within the same response.")
        }

        headers[headerName] = header
    }

    /**
     * Builds the headers.
     *
     * @return The constructed headers.
     */
    fun build(): Map<String, ApiHeader> = headers
}
