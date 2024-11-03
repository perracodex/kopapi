/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.plugin

import io.github.perracodex.kopapi.dsl.common.SecuritySchemeConfigurable
import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.plugin.builders.InfoBuilder
import io.github.perracodex.kopapi.dsl.plugin.builders.TagBuilder
import io.github.perracodex.kopapi.dsl.plugin.builders.server.ServerBuilder
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiConfiguration
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiInfo
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiServerConfig
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiTag
import io.github.perracodex.kopapi.utils.trimOrDefault

/**
 * Configuration for the [Kopapi] plugin.
 */
@KopapiDsl
public class KopapiConfig : SecuritySchemeConfigurable() {
    /**
     * Whether the plugin should be enabled (Default is `true`).
     *
     * - When disabled, the plugin will not register any of the configuration URLs,
     *  and all the Routes API schema definitions will be discarded.
     *
     * - This is useful when you want to disable the plugin in certain environments, such as production.
     */
    public var enabled: Boolean = true

    /**
     * The URL to provide the OpenAPI schema in `JSON` format.
     *
     * - Relative to the server root URL.
     * - Default: `/openapi.json`.
     */
    public var openapiJsonUrl: String = DEFAULT_OPENAPI_JSON_URL

    /**
     * The URL to provide the OpenAPI schema in `YAML` format.
     *
     * - Relative to the server root URL.
     * - Default: `/openapi.yaml`.
     */
    public var openapiYamlUrl: String = DEFAULT_OPENAPI_YAML_URL

    /**
     * The URL to provide the OpenAPI `Redoc` documentation.
     *
     * - Relative to the server root URL.
     * - Default: `/redoc`.
     */
    public var redocUrl: String = DEFAULT_REDOC_URL

    /**
     * The URL to provide the `Swagger UI`.
     *
     * - Relative to the server root URL.
     * - Default: `/swagger-ui`.
     */
    public var swaggerUrl: String = DEFAULT_SWAGGER_URL

    /**
     * The URL to provide the raw pre-processed API Operations metadata, for debugging purposes.
     *
     * - Relative to the server root URL.
     * - Default: `/openapi/debug`.
     */
    public var debugUrl: String = DEFAULT_DEBUG_URL

    /**
     * Whether to enable internal logging for the plugin. Default: `false`.
     *
     * When enabled the plugin will include additional logging information
     * such as when traversing types, resolving schemas, and building the OpenAPI schema.
     *
     * #### Attention
     * Enabling this option may produce quite verbose logs
     * and is recommended for debugging purposes only.
     */
    public var enableLogging: Boolean = false

    /**
     * The [ApiInfo] metadata for the OpenAPI schema.
     */
    private var apiInfo: ApiInfo? = null

    /**
     * The list of servers to include in the OpenAPI schema.
     */
    private val servers: MutableSet<ApiServerConfig> = mutableSetOf()

    /**
     * The list of tags to include in the OpenAPI schema.
     */
    private val tags: MutableSet<ApiTag> = mutableSetOf()

    /**
     * Sets up the OpenAPI metadata.
     *
     * #### Sample Usage
     * ```
     *  info {
     *      title = "API Title"
     *      description = "API Description"
     *      version = "1.0.0"
     *      termsOfService = "https://example.com/terms"
     *      contact {
     *          name = "API Support"
     *          url = "https://example.com/support"
     *          email = "example@email.com"
     *      }
     *      license {
     *          name = "MIT"
     *          url = "https://opensource.org/licenses/MIT"
     *      }
     *  }
     * ```
     *
     * @see [InfoBuilder]
     */
    public fun info(init: InfoBuilder.() -> Unit) {
        apiInfo = InfoBuilder().apply(init).build()
    }

    /**
     * Sets up servers, with optional support for variables.
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
     * @see [ServerBuilder]
     */
    public fun servers(init: ServerBuilder.() -> Unit) {
        val builder: ServerBuilder = ServerBuilder().apply(init)
        servers.addAll(builder.build())
    }

    /**
     * Sets up tags for the API.
     *
     * #### Sample Usage
     * ```
     * tags {
     *     add(name = "Items", description = "Operations related to items.")
     *     add(name = "Users", description = "Operations related to users.")
     * }
     * ```
     *
     * @see [TagBuilder]
     */
    public fun tags(init: TagBuilder.() -> Unit) {
        val builder: TagBuilder = TagBuilder().apply(init)
        tags.addAll(builder.build())
    }

    /**
     * Builds the final immutable [ApiConfiguration] instance.
     */
    internal fun build(): ApiConfiguration {
        return ApiConfiguration(
            isEnabled = enabled,
            debugUrl = normalizeUrl(url = debugUrl, defaultValue = DEFAULT_DEBUG_URL),
            redocUrl = normalizeUrl(url = redocUrl, defaultValue = DEFAULT_REDOC_URL),
            openapiJsonUrl = normalizeUrl(url = openapiJsonUrl, defaultValue = DEFAULT_OPENAPI_JSON_URL),
            openapiYamlUrl = normalizeUrl(url = openapiYamlUrl, defaultValue = DEFAULT_OPENAPI_YAML_URL),
            swaggerUrl = normalizeUrl(url = swaggerUrl, defaultValue = DEFAULT_SWAGGER_URL),
            apiInfo = apiInfo,
            apiServers = servers.takeIf { it.isNotEmpty() } ?: setOf(ServerBuilder.defaultServer()),
            apiTags = tags.takeIf { it.isNotEmpty() },
            apiSecuritySchemes = _securityConfig.securitySchemes.takeIf { it.isNotEmpty() }
        )
    }

    /**
     * Normalizes the URL by ensuring it starts with a `/`.
     *
     * @param url The URL to normalize.
     * @param defaultValue The default value to use if the URL is empty.
     */
    private fun normalizeUrl(url: String?, defaultValue: String): String {
        val cleanUrl: String = url.trimOrDefault(defaultValue = defaultValue)
        return when (cleanUrl.startsWith(prefix = "/")) {
            true -> cleanUrl
            false -> "/$cleanUrl"
        }
    }

    internal companion object {
        const val DEFAULT_OPENAPI_JSON_URL: String = "/openapi.json"
        const val DEFAULT_OPENAPI_YAML_URL: String = "/openapi.yaml"
        const val DEFAULT_REDOC_URL: String = "/redoc"
        const val DEFAULT_SWAGGER_URL: String = "/swagger-ui"
        const val DEFAULT_DEBUG_URL: String = "/openapi/debug"
    }
}
