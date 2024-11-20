/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.dsl.header.delegate

import io.github.perracodex.kopapi.dsl.header.builder.HeadersBuilder
import io.github.perracodex.kopapi.dsl.marker.KopapiDsl

/**
 * Handles the registration of headers.
 */
@KopapiDsl
public interface IHeaderConfigurable {
    /**
     * Adds a collection of headers defined within a `headers { ... }` block.
     *
     * The `headers` block serves only as organizational syntactic sugar.
     * Headers can be defined directly without needing to use the `headers` block.
     *
     * #### Usage
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
     * @receiver [HeadersBuilder] The builder used to configure the headers.
     */
    public fun headers(builder: HeadersBuilder.() -> Unit)
}
