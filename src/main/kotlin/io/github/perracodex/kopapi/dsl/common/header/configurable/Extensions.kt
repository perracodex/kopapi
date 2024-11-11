/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.common.header.configurable

import io.github.perracodex.kopapi.dsl.common.header.HeaderBuilder

/**
 * Adds a header to the response.
 *
 * #### Usage
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
 * @receiver [HeaderBuilder] The builder used to configure the header.
 *
 * @param T The type of the header.
 * @param name The name of the header.
 */
public inline fun <reified T : Any> IHeaderConfigurable.header(
    name: String,
    noinline builder: HeaderBuilder.() -> Unit = {}
) {
    this.headers {
        add<T>(name = name, builder = builder)
    }
}
