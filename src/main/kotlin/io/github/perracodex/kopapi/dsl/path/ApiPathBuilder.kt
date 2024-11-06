/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.path

import io.github.perracodex.kopapi.dsl.common.parameter.CookieParameterBuilder
import io.github.perracodex.kopapi.dsl.common.parameter.HeaderParameterBuilder
import io.github.perracodex.kopapi.dsl.common.parameter.PathParameterBuilder
import io.github.perracodex.kopapi.dsl.common.parameter.QueryParameterBuilder
import io.github.perracodex.kopapi.dsl.common.server.ServerBuilder
import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.elements.ApiParameter
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiServerConfig
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.string.SpacedString
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.server.routing.*
import kotlin.reflect.typeOf

/**
 * Builder for constructing API path-level metadata.
 *
 * #### Information
 * - [summary]: Optional short description of the endpoint's purpose.
 * - [description]: Optional detailed explanation of the endpoint and its functionality.
 * - [servers]: Optional list of server configurations specific to this path.
 *
 * #### Parameters
 * - [pathParameter]: Adds a path parameter applicable to all operations within this path.
 * - [queryParameter]: Adds a query parameter applicable to all operations within this path.
 * - [headerParameter]: Adds a header parameter applicable to all operations within this path.
 * - [cookieParameter]: Adds a cookie parameter applicable to all operations within this path.
 *
 * #### Sample Usage
 * - Define for a `routing` block:
 * ```
 * routing {
 *     // Implement routes as usual
 * } apiPath {
 *     summary = "Some summary"
 *     description = "Some description"
 *     servers {
 *         add(urlString = "https://api.example.com") {
 *             description = "Some server description"
 *         }
 *     }
 *     pathParameter<Uuid>(name = "id") {
 *         description = "The unique identifier of the item."
 *    }
 * }
 * ```
 * - Define for a `route` block:
 * ```
 * routing {
 *     route("some-endpoint") {
 *         // Implement operations as usual (e.g., get, put, post, etc)
 *     } apiPath {
 *         summary = "Some summary"
 *         description = "Some description"
 *         servers {
 *             add(urlString = "https://api.example.com") {
 *                 description = "Some server description"
 *             }
 *         }
 *         pathParameter<Uuid>(name = "id") {
 *             description = "The unique identifier of the item."
 *         }
 *     }
 * }
 * ```
 *
 * @see [Route.apiPath]
 */
@KopapiDsl
public class ApiPathBuilder internal constructor(
    @PublishedApi internal val endpoint: String
) {

    @Suppress("PropertyName")
    @PublishedApi
    internal val _config: Config = Config(endpoint = endpoint)

    /**
     * Optional short description of the path's purpose.
     *
     * Declaring the `summary` multiple times will concatenate all the summaries
     * delimited by a `space` character between each one.
     *
     * #### Sample Usage
     * ```
     * summary = "Retrieve data items."
     * ```
     *
     * @see [description]
     */
    public var summary: String by SpacedString()

    /**
     * Optional detailed explanation of the path.
     *
     * Declaring the `description` multiple times will concatenate all the descriptions
     * delimited by a `newline` character between each one.
     *
     * #### Sample Usage
     * ```
     * description = "Fetches all items for a group."
     * description = "In addition, it can fetch a specific item."
     * ```
     *
     * @see [summary]
     */
    public var description: String by MultilineString()

    /**
     * Sets up servers for the path, with optional support for variables.
     *
     * #### Sample Usage
     * ```
     * servers {
     *      // Simple example with no variables.
     *      add(urlString = "http://localhost:8080") {
     *         description = "Local server for development."
     *      }
     *
     *      // Example with variable placeholders.
     *      add(urlString = "{protocol}://{environment}.example.com:{port}") {
     *          description = "The server with environment variable."
     *
     *          // Environment.
     *          variable(name = "environment", defaultValue = "production") {
     *              choices = setOf("production", "staging", "development")
     *              description = "Specifies the environment (production, etc)"
     *          }
     *
     *          // Port.
     *          variable(name = "port", defaultValue = "8080") {
     *              choices = setOf("8080", "8443")
     *              description = "The port for the server."
     *          }
     *
     *          // Protocol.
     *          variable(name = "protocol", defaultValue = "http") {
     *              choices = setOf("http", "https")
     *          }
     *      }
     * }
     * ```
     *
     * @param configure A lambda receiver for configuring the [ServerBuilder].
     *
     * @see [ServerBuilder]
     */
    public fun servers(configure: ServerBuilder.() -> Unit) {
        val builder: ServerBuilder = ServerBuilder().apply(configure)
        _config.servers.addAll(builder.build())
    }

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
        configure: PathParameterBuilder.() -> Unit = {}
    ) {
        val builder: PathParameterBuilder = PathParameterBuilder().apply(configure)
        val parameter: ApiParameter = builder.build(name = name, type = typeOf<T>())
        _config.addApiParameter(apiParameter = parameter)
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
        configure: QueryParameterBuilder.() -> Unit = {}
    ) {
        val builder: QueryParameterBuilder = QueryParameterBuilder().apply(configure)
        val parameter: ApiParameter = builder.build(name = name, type = typeOf<T>())
        _config.addApiParameter(apiParameter = parameter)
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
        configure: HeaderParameterBuilder.() -> Unit = {}
    ) {
        val builder: HeaderParameterBuilder = HeaderParameterBuilder().apply(configure)
        val parameter: ApiParameter = builder.build(name = name, type = typeOf<T>())
        _config.addApiParameter(apiParameter = parameter)
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
        configure: CookieParameterBuilder.() -> Unit = {}
    ) {
        val builder: CookieParameterBuilder = CookieParameterBuilder().apply(configure)
        val parameter: ApiParameter = builder.build(name = name, type = typeOf<T>())
        _config.addApiParameter(apiParameter = parameter)
    }

    /**
     * Builds the [ApiPath] instance with the configured properties.
     *
     * @return The constructed [ApiPath] instance.
     */
    internal fun build(): ApiPath {
        return ApiPath(
            path = endpoint,
            summary = summary.trimOrNull(),
            description = description.trimOrNull(),
            servers = _config.servers.takeIf { it.isNotEmpty() },
            parameters = _config.parameters.takeIf { it.isNotEmpty() }
        )
    }

    @PublishedApi
    internal class Config(private val endpoint: String) {
        /** Set of parameters applicable to all operations within this path. */
        var parameters: LinkedHashSet<ApiParameter> = linkedSetOf()

        /** The list of servers at path level. */
        val servers: MutableSet<ApiServerConfig> = mutableSetOf()

        /**
         * Cache a new [ApiParameter] instance for the path.
         *
         * @param apiParameter The [ApiParameter] instance to add to the cache.
         * @throws KopapiException If an [ApiParameter] with the same name already exists.
         */
        fun addApiParameter(apiParameter: ApiParameter) {
            if (parameters.any { it.name.equals(other = apiParameter.name, ignoreCase = true) }) {
                val message: String = """
                    |Attempting to register more than once parameter with name '${apiParameter.name}' within the same Path:
                    |   '$endpoint'.
                    |The OpenAPI specification requires parameter names to be unique with the same path unless overridden by the operation.
                    """.trimMargin()
                throw KopapiException(message)
            }

            parameters.add(apiParameter)
        }
    }
}
