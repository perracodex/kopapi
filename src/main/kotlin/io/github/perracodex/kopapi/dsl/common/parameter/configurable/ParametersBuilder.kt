/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.common.parameter.configurable

import io.github.perracodex.kopapi.dsl.common.parameter.CookieParameterBuilder
import io.github.perracodex.kopapi.dsl.common.parameter.HeaderParameterBuilder
import io.github.perracodex.kopapi.dsl.common.parameter.PathParameterBuilder
import io.github.perracodex.kopapi.dsl.common.parameter.QueryParameterBuilder
import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.elements.ApiParameter
import io.github.perracodex.kopapi.system.KopapiException
import kotlin.reflect.typeOf

/**
 * Handles the registration of parameters.
 */
@KopapiDsl
public open class ParametersBuilder internal constructor(endpoint: String) {
    @Suppress("PropertyName")
    @PublishedApi
    internal val _parametersConfig: Config = Config(endpoint = endpoint)

    /**
     * Registers a `path` parameter.
     *
     * #### Sample Usage
     * ```
     * pathParameter<Uuid>(name = "id") {
     *     description = "The unique identifier of the item."
     *     style = ParameterStyle.SIMPLE
     *     deprecated = false
     * }
     * ```
     *
     * @param T The type of the parameter.
     * @param name The name of the parameter as it appears in the URL path.
     * @param configure A lambda receiver for configuring the [PathParameterBuilder].
     *
     * @see [PathParameterBuilder]
     * @see [cookieParameter]
     * @see [headerParameter]
     * @see [queryParameter]
     */
    public inline fun <reified T : Any> pathParameter(
        name: String,
        noinline configure: PathParameterBuilder.() -> Unit = {}
    ) {
        val builder: PathParameterBuilder = PathParameterBuilder().apply(configure)
        val parameter: ApiParameter = builder.build(name = name, type = typeOf<T>())
        _parametersConfig.addApiParameter(apiParameter = parameter)
    }

    /**
     * Registers a `query` parameter.
     *
     * #### Sample Usage
     * ```
     * queryParameter<Int>(name = "page") {
     *     description = "The page number to retrieve."
     *     required = true
     *     allowReserved = false
     *     defaultValue = DefaultValue.ofInt(1)
     *     style = ParameterStyle.FORM
     *     explode = false
     *     deprecated = false
     * }
     * ```
     *
     * @param T The type of the parameter.
     * @param name The name of the parameter as it appears in the URL path.
     * @param configure A lambda receiver for configuring the [QueryParameterBuilder].
     *
     * @see [QueryParameterBuilder]
     * @see [cookieParameter]
     * @see [headerParameter]
     * @see [pathParameter]
     */
    public inline fun <reified T : Any> queryParameter(
        name: String,
        noinline configure: QueryParameterBuilder.() -> Unit = {}
    ) {
        val builder: QueryParameterBuilder = QueryParameterBuilder().apply(configure)
        val parameter: ApiParameter = builder.build(name = name, type = typeOf<T>())
        _parametersConfig.addApiParameter(apiParameter = parameter)
    }

    /**
     * Registers a `header` parameter.
     *
     * #### Sample Usage
     * ```
     * headerParameter<String>(name = "X-Custom-Header") {
     *     description = "A custom header for special purposes."
     *     required = true
     *     defaultValue = DefaultValue.ofString("default")
     *     style = ParameterStyle.SIMPLE
     *     deprecated = false
     * }
     * ```
     *
     * @param T The type of the parameter.
     * @param name The header of the parameter as it appears in the URL path.
     * @param configure A lambda receiver for configuring the [HeaderParameterBuilder].
     *
     * @see [HeaderParameterBuilder]
     * @see [cookieParameter]
     * @see [pathParameter]
     * @see [queryParameter]
     */
    public inline fun <reified T : Any> headerParameter(
        name: String,
        noinline configure: HeaderParameterBuilder.() -> Unit = {}
    ) {
        val builder: HeaderParameterBuilder = HeaderParameterBuilder().apply(configure)
        val parameter: ApiParameter = builder.build(name = name, type = typeOf<T>())
        _parametersConfig.addApiParameter(apiParameter = parameter)
    }

    /**
     * Registers a `cookie` parameter.
     *
     * #### Sample Usage
     * ```
     * cookieParameter<String>(name = "session") {
     *     description = "The session ID for authentication."
     *     required = true
     *     defaultValue = DefaultValue.ofString("default")
     *     style = ParameterStyle.FORM
     *     explode = false
     *     deprecated = false
     * }
     * ```
     *
     * @param T The type of the parameter.
     * @param name The name of the parameter as it appears in the URL path.
     * @param configure A lambda receiver for configuring the [CookieParameterBuilder].
     *
     * @see [CookieParameterBuilder]
     * @see [headerParameter]
     * @see [pathParameter]
     * @see [queryParameter]
     */
    public inline fun <reified T : Any> cookieParameter(
        name: String,
        noinline configure: CookieParameterBuilder.() -> Unit = {}
    ) {
        val builder: CookieParameterBuilder = CookieParameterBuilder().apply(configure)
        val parameter: ApiParameter = builder.build(name = name, type = typeOf<T>())
        _parametersConfig.addApiParameter(apiParameter = parameter)
    }

    @PublishedApi
    internal class Config(private val endpoint: String) {
        /** Internal cache for the parameters. */
        var parameters: LinkedHashSet<ApiParameter> = linkedSetOf()

        /**
         * Cache a new [ApiParameter] instance for the API Operation.
         *
         * @param apiParameter The [ApiParameter] instance to add to the cache.
         * @throws KopapiException If an [ApiParameter] with the same name already exists.
         */
        fun addApiParameter(apiParameter: ApiParameter) {
            if (parameters.any { it.name.equals(other = apiParameter.name, ignoreCase = true) }) {
                val message: String = """
                |Attempting to register more than once parameter with name '${apiParameter.name}' within its own scope context:
                |   '$endpoint'.
                |The OpenAPI specification requires parameter names to be unique within each API scope context.
                """.trimMargin()
                throw KopapiException(message)
            }

            parameters.add(apiParameter)
        }
    }
}