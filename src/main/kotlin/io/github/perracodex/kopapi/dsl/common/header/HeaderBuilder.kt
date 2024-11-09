/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.common.header

import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.builders.attributes.HeaderBuilder
import io.github.perracodex.kopapi.dsl.operation.builders.attributes.HeadersBuilder
import io.github.perracodex.kopapi.dsl.operation.elements.ApiHeader
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.utils.sanitize

/**
 * Handles the registration of headers.
 */
@KopapiDsl
public open class HeaderBuilder internal constructor() {
    /** Holds the headers associated with the response. */
    @Suppress("PropertyName")
    @PublishedApi
    internal val _headers: MutableMap<String, ApiHeader> = mutableMapOf()

    /**
     * Adds a header to the response.
     *
     * #### Sample usage
     * ```
     * header<Int>(name = "X-Rate-Limit") {
     *     description = "Number of allowed requests per period."
     * }
     * ```
     * ```
     * header<String>(name = "X-Session-Token") {
     *     description = "The session token for the user."
     *     schema {
     *         pattern = "^[A-Za-z0-9_-]{20,50}$"
     *         minLength = 20
     *         maxLength = 50
     *     }
     * }
     *```
     *
     * @param T The type of the header.
     * @param name The name of the header.
     * @param configure A lambda receiver for configuring the [HeaderBuilder].
     */
    public inline fun <reified T : Any> header(
        name: String,
        noinline configure: HeaderBuilder.() -> Unit
    ) {
        headers { add<T>(name = name, configure = configure) }
    }

    /**
     * Adds a collection of headers defined within a `headers { ... }` block.
     *
     * The `headers { ... }` block serves only as organizational syntactic sugar.
     * Headers can be defined directly without needing to use the `headers { ... }` block.
     *
     * #### Sample Usage
     * ```
     * headers {
     *      add<Int>("X-Rate-Limit") {
     *          description = "Number of allowed requests per period."
     *      }
     *      add<String>(name = "X-Session-Token") {
     *          description = "The session token for the user."
     *          schema {
     *              pattern = "^[A-Za-z0-9_-]{20,50}$"
     *              minLength = 20
     *              maxLength = 50
     *          }
     *      }
     * }
     * ```
     *
     * @param configure A lambda receiver for configuring the [HeadersBuilder].
     */
    public fun headers(configure: HeadersBuilder.() -> Unit) {
        val headersBuilder: HeadersBuilder = HeadersBuilder().apply(configure)
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
        if (_headers.any { it.key.equals(other = headerName, ignoreCase = true) }) {
            throw KopapiException("Header with name '${headerName}' already exists within the same response.")
        }

        _headers[headerName] = header
    }
}
