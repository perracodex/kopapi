/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.headers.builders

import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.elements.ApiHeader
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.utils.orNull
import io.github.perracodex.kopapi.utils.sanitize
import kotlin.reflect.typeOf

/**
 * Builds a collection of response headers for an API endpoint.
 */
@KopapiDsl
public class HeadersBuilder internal constructor() {
    /** Cached headers. */
    @Suppress("PropertyName")
    @PublishedApi
    internal val _headers: MutableMap<String, ApiHeader> = mutableMapOf()

    /**
     * Adds a header to the collection.
     *
     * #### Usage
     * ```
     * headers {
     *     add<Int>("X-Rate-Limit") {
     *         description = "Number of allowed requests per period."
     *     }
     *     add<Uuid>(name = "X-Request-Id") {
     *         description = "A unique identifier for the request."
     *         required = false
     *     }
     * }
     * ```
     *
     * @receiver [HeaderBuilder] The builder used to configure the header.
     *
     * @param T The type of the header. Must not be [Unit], [Nothing], or [Any].
     * @param name The name of the header.
     * @throws KopapiException If a header with the same name already exists.
     */
    public inline fun <reified T : Any> add(
        name: String,
        noinline builder: HeaderBuilder.() -> Unit = {}
    ) {
        if (T::class == Unit::class || T::class == Nothing::class || T::class == Any::class) {
            throw KopapiException("Header type must not be Unit, Nothing, or Any. Specify an explicit type.")
        }

        val headerName: String = name.sanitize()
        if (headerName.isBlank()) {
            throw KopapiException("Header name must not be blank.")
        }

        _headers[headerName] = HeaderBuilder().apply(builder).build(type = typeOf<T>())
    }

    /**
     * Returns the registered headers.
     */
    @PublishedApi
    internal fun build(): MutableMap<String, ApiHeader>? = _headers.orNull()
}
