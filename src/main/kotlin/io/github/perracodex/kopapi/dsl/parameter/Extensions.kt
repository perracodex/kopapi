/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.parameter

import io.github.perracodex.kopapi.dsl.parameter.builder.CookieParameterBuilder
import io.github.perracodex.kopapi.dsl.parameter.builder.HeaderParameterBuilder
import io.github.perracodex.kopapi.dsl.parameter.builder.PathParameterBuilder
import io.github.perracodex.kopapi.dsl.parameter.builder.QueryParameterBuilder
import io.github.perracodex.kopapi.dsl.parameter.delegate.IParameterConfigurable

/**
 * Adds a `path` parameter using the `pathParameter<T>()` function.
 *
 * #### Usage
 * ```
 * pathParameter<Uuid>("id") {
 *     description = "The unique identifier."
 * }
 * ```
 *
 * @param T The type of the parameter.
 * @param name The name of the parameter as it appears in the URL path.
 * @param builder The builder used to configure the path parameter.
 */
public inline fun <reified T : Any> IParameterConfigurable.pathParameter(
    name: String,
    noinline builder: PathParameterBuilder.() -> Unit = {}
) {
    this.parameters {
        pathParameter<T>(name = name, builder = builder)
    }
}

/**
 * Adds a `query` parameter using the `queryParameter<T>()` function.
 *
 * #### Usage
 * ```
 * queryParameter<Int>("page") {
 *     description = "Page number."
 * }
 * ```
 *
 * @param T The type of the parameter.
 * @param name The name of the parameter as it appears in the query string.
 * @param builder The builder used to configure the query parameter.
 */
public inline fun <reified T : Any> IParameterConfigurable.queryParameter(
    name: String,
    noinline builder: QueryParameterBuilder.() -> Unit = {}
) {
    this.parameters {
        queryParameter<T>(name = name, builder = builder)
    }
}

/**
 * Adds a `header` parameter using the `headerParameter<T>()` function.
 *
 * #### Usage
 * ```
 * headerParameter<String>("X-Custom-Header") {
 *     description = "Custom header."
 * }
 * ```
 *
 * @param T The type of the parameter.
 * @param name The name of the header parameter.
 * @param builder The builder used to configure the header parameter.
 */
public inline fun <reified T : Any> IParameterConfigurable.headerParameter(
    name: String,
    noinline builder: HeaderParameterBuilder.() -> Unit = {}
) {
    this.parameters {
        headerParameter<T>(name = name, builder = builder)
    }
}

/**
 * Adds a `cookie` parameter using the `cookieParameter<T>()` function.
 *
 * #### Usage
 * ```
 * cookieParameter<String>("session") {
 *     description = "Session ID."
 * }
 * ```
 *
 * @param T The type of the parameter.
 * @param name The name of the cookie parameter.
 * @param builder The builder used to configure the cookie parameter.
 */
public inline fun <reified T : Any> IParameterConfigurable.cookieParameter(
    name: String,
    noinline builder: CookieParameterBuilder.() -> Unit = {}
) {
    this.parameters {
        cookieParameter<T>(name = name, builder = builder)
    }
}
