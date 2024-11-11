/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

// IHeaderConfigurable.kt
package io.github.perracodex.kopapi.dsl.common.header.configurable

import io.github.perracodex.kopapi.dsl.common.header.HeaderBuilder
import io.github.perracodex.kopapi.dsl.common.header.HeadersBuilder
import io.github.perracodex.kopapi.dsl.markers.KopapiDsl

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
     * @receiver [HeaderBuilder] The builder used to configure the headers.
     */
    public fun headers(builder: HeadersBuilder.() -> Unit)
}
