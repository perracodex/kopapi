/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.attributes

import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.elements.ApiHeader
import io.github.perracodex.kopapi.system.KopapiException

/**
 * Builds a collection of response headers for an API endpoint.
 *
 * #### Sample Usage
 * ```
 * headers {
 *     header("X-Rate-Limit") {
 *         description = "Number of allowed requests per period."
 *         required = true
 *     }
 *
 *     header("X-Another-Header") {
 *         description = "Another header description."
 *         required = false
 *         deprecated = true
 *     }
 * }
 * ```
 *
 * @property headers A mutable set holding the configured [ApiHeader] instances.
 */
@KopapiDsl
public class HeadersBuilder @PublishedApi internal constructor() {
    internal val headers: MutableSet<ApiHeader> = mutableSetOf()

    /**
     * Adds a header to the collection.
     *
     * @param name The name of the header.
     * @param configure A lambda receiver for configuring the [HeaderBuilder].
     *
     * #### Sample Usage
     * ```
     * header("X-Rate-Limit") {
     *     description = "Number of allowed requests per period."
     *     required = true
     * }
     * ```
     *
     * @throws KopapiException If a header with the same name already exists.
     */
    public fun header(name: String, configure: HeaderBuilder.() -> Unit) {
        val header: ApiHeader = HeaderBuilder(name = name).apply(configure).build()
        addHeader(header)
    }

    /**
     * Adds a header to the collection, ensuring uniqueness.
     *
     * @param header The [ApiHeader] instance to add.
     * @throws KopapiException If a header with the same name already exists.
     */
    internal fun addHeader(header: ApiHeader) {
        if (headers.any { it.name.equals(header.name, ignoreCase = true) }) {
            throw KopapiException("Header with name '${header.name}' already exists within the same response.")
        }
        headers.add(header)
    }

    /**
     * Builds the headers collection.
     *
     * @return A set of [ApiHeader] instances.
     */
    internal fun build(): Set<ApiHeader> = headers
}
