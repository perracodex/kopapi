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
import io.ktor.http.*
import kotlin.reflect.typeOf

/**
 * Handles the registration of parameters.
 */
@KopapiDsl
public open class ParametersBuilder internal constructor(private val endpoint: String) {
    @Suppress("PropertyName")
    @PublishedApi
    internal val _parametersConfig: Config = Config(endpoint = endpoint)

    /**
     * Registers a `path` parameter.
     *
     * #### Usage
     * ```
     * pathParameter<Uuid>(name = "id") {
     *     description = "The unique identifier of the item."
     *     style = ParameterStyle.SIMPLE
     * }
     * ```
     * ```
     * pathParameter<String>(name = "productCode") {
     *     description = "The code representing the product."
     *     style = ParameterStyle.SIMPLE
     *     schema {
     *         pattern = "^[A-Z0-9]{5,10}$"
     *         minLength = 5
     *         maxLength = 10
     *     }
     * }
     * ```
     *
     * @receiver [PathParameterBuilder] The builder used to configure the path parameter.
     *
     * @param T The type of the parameter.
     * @param name The name of the parameter as it appears in the URL path.
     *
     * @see [cookieParameter]
     * @see [headerParameter]
     * @see [queryParameter]
     */
    public inline fun <reified T : Any> pathParameter(
        name: String,
        noinline builder: PathParameterBuilder.() -> Unit = {}
    ) {
        val parameterBuilder: PathParameterBuilder = PathParameterBuilder().apply(builder)
        val parameter: ApiParameter = parameterBuilder.build(name = name, type = typeOf<T>())
        _parametersConfig.addApiParameter(apiParameter = parameter)
    }

    /**
     * Registers a `query` parameter.
     *
     * #### Usage
     * ```
     * queryParameter<Int>(name = "page") {
     *     description = "The page number to retrieve."
     *     defaultValue = DefaultValue.ofInt(1)
     * }
     * ```
     * ```
     * queryParameter<String>(name = "search") {
     *     description = "A search term to filter the results."
     *     required = false
     *     allowReserved = false
     *     defaultValue = DefaultValue.ofString("default")
     *     style = ParameterStyle.FORM
     *     explode = false
     *     schema {
     *         pattern = "^[a-zA-Z0-9]{3,20}$"
     *         minLength = 3
     *         maxLength = 20
     *     }
     * }
     * ```
     *
     * @receiver [QueryParameterBuilder] The builder used to configure the query parameter.
     *
     * @param T The type of the parameter.
     * @param name The name of the parameter as it appears in the URL path.
     *
     * @see [cookieParameter]
     * @see [headerParameter]
     * @see [pathParameter]
     */
    public inline fun <reified T : Any> queryParameter(
        name: String,
        noinline builder: QueryParameterBuilder.() -> Unit = {}
    ) {
        val parameterBuilder: QueryParameterBuilder = QueryParameterBuilder().apply(builder)
        val parameter: ApiParameter = parameterBuilder.build(name = name, type = typeOf<T>())
        _parametersConfig.addApiParameter(apiParameter = parameter)
    }

    /**
     * Registers a `header` parameter.
     *
     * #### Usage
     * ```
     * headerParameter<String>(name = "X-Custom-Header") {
     *     description = "A custom header for special purposes."
     *     required = false
     *     defaultValue = DefaultValue.ofString("default")
     * }
     * ```
     * ```
     * headerParameter<String>(name = "X-Client-Version") {
     *     description = "The client's version number."
     *     defaultValue = DefaultValue.ofString("1.0.0")
     *     style = ParameterStyle.SIMPLE
     *     schema {
     *         pattern = "^[0-9]+\\.[0-9]+\\.[0-9]+$"
     *     }
     * }
     * ```
     *
     * @receiver [HeaderParameterBuilder] The builder used to configure the header parameter.
     *
     * @param T The type of the parameter.
     * @param name The header of the parameter as it appears in the URL path.
     *
     * @see [cookieParameter]
     * @see [pathParameter]
     * @see [queryParameter]
     */
    public inline fun <reified T : Any> headerParameter(
        name: String,
        noinline builder: HeaderParameterBuilder.() -> Unit = {}
    ) {
        val parameterBuilder: HeaderParameterBuilder = HeaderParameterBuilder().apply(builder)
        val parameter: ApiParameter = parameterBuilder.build(name = name, type = typeOf<T>())
        _parametersConfig.addApiParameter(apiParameter = parameter)
    }

    /**
     * Registers a `cookie` parameter.
     *
     * #### Usage
     * ```
     * cookieParameter<String>(name = "custom") {
     *     description = "Some custom cookie for the user."
     *     defaultValue = DefaultValue.ofString("default")
     * }
     * ```
     * ```
     * cookieParameter<String>(name = "session") {
     *     description = "The session ID for user authentication."
     *     required = true
     *     defaultValue = DefaultValue.ofString("defaultSessionId")
     *     style = ParameterStyle.FORM
     *     explode = false
     *     schema {
     *         pattern = "^[A-Za-z0-9_-]{20,50}$"
     *         minLength = 20
     *         maxLength = 50
     *     }
     * }
     * ```
     *
     * @receiver [CookieParameterBuilder] The builder used to configure the cookie parameter.
     *
     * @param T The type of the parameter.
     * @param name The name of the parameter as it appears in the URL path.
     *
     * @see [headerParameter]
     * @see [pathParameter]
     * @see [queryParameter]
     */
    public inline fun <reified T : Any> cookieParameter(
        name: String,
        noinline builder: CookieParameterBuilder.() -> Unit = {}
    ) {
        val parameterBuilder: CookieParameterBuilder = CookieParameterBuilder().apply(builder)
        val parameter: ApiParameter = parameterBuilder.build(name = name, type = typeOf<T>())
        _parametersConfig.addApiParameter(apiParameter = parameter)
    }

    /**
     * Adds a collection of parameters defined within a `parameters { ... }` block.
     *
     * The `parameters` block serves only as organizational syntactic sugar.
     * Parameters can be defined directly without needing to use the `parameters` block.
     *
     * #### Usage
     * ```
     * parameters {
     *     pathParameter<Uuid>("id_1") { description = "The data Id." }
     *     queryParameter<String>("id_2") { description = "Optional item Id." }
     * }
     * ```
     *
     * @receiver [ParametersBuilder] The builder used to configure the parameters.
     */
    public fun parameters(builder: ParametersBuilder.() -> Unit) {
        val parameterBuilder: ParametersBuilder = ParametersBuilder(endpoint = endpoint).apply(builder)
        _parametersConfig.parameters.addAll(parameterBuilder._parametersConfig.parameters)
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
