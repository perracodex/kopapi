/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.plugin

import io.github.perracodex.kopapi.dsl.marker.KopapiDsl
import io.github.perracodex.kopapi.dsl.plugin.builder.ApiDocsBuilder
import io.github.perracodex.kopapi.dsl.plugin.builder.InfoBuilder
import io.github.perracodex.kopapi.dsl.plugin.builder.TagBuilder
import io.github.perracodex.kopapi.dsl.plugin.element.ApiConfiguration
import io.github.perracodex.kopapi.dsl.plugin.element.ApiDocs
import io.github.perracodex.kopapi.dsl.plugin.element.ApiInfo
import io.github.perracodex.kopapi.dsl.plugin.element.ApiTag
import io.github.perracodex.kopapi.dsl.security.delegate.ISecurityConfigurable
import io.github.perracodex.kopapi.dsl.security.delegate.SecurityDelegate
import io.github.perracodex.kopapi.dsl.server.builder.ServerBuilder
import io.github.perracodex.kopapi.dsl.server.delegate.IServerConfigurable
import io.github.perracodex.kopapi.dsl.server.delegate.ServerDelegate
import io.github.perracodex.kopapi.util.NetworkUtils
import io.github.perracodex.kopapi.util.orNull
import io.github.perracodex.kopapi.util.trimOrNull

/**
 * Configuration for the [Kopapi] plugin.
 */
@KopapiDsl
public class KopapiConfig internal constructor(
    private val serverDelegate: ServerDelegate = ServerDelegate(),
    private val securityDelegate: SecurityDelegate = SecurityDelegate()
) : IServerConfigurable by serverDelegate,
    ISecurityConfigurable by securityDelegate {

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
     * The base URL of the application.
     *
     * Typically, this setting is not required because the plugin automatically
     * resolves the base URL based on incoming requests.
     * However, in containerized environments like Docker, where the application binds
     * to `0.0.0.0`, the resolved URLs in output logs may not reflect the actual accessible address.
     * In such cases, it is recommended to explicitly set the host property to the application's
     * public IP address or domain name.
     * This ensures that the full URLs in the logs are accurate.
     *
     * #### Example
     * ```
     * http://localhost:8080
     * https://api.example.com
     * ```
     *
     * - Default: `null`.
     */
    public var host: String? = null

    /**
     * The URL to provide the raw pre-processed API Operations metadata, for debugging purposes.
     *
     * - Relative to the server root URL.
     * - Default: `/openapi/debug`.
     */
    public var debugUrl: String = DEFAULT_DEBUG_URL

    /**
     * Whether to enable on-demand API schema generation.
     *
     * When enabled (the default), the plugin will only generate and cache the OpenAPI schema
     * in a synchronized manner the first time it is requested.
     * Otherwise, it will be asynchronously generated and cached right away when the plugin
     * is initialized so it is ready to be served on the first request.
     *
     * As the schema can take a while to generate, depending on the number
     * of routes and the complexity of the types, this setting can be used
     * to defer the generation until it is needed, or to generate and cache it
     * right away to avoid any delays on the first request.
     *
     * - Default: `true`.
     */
    public var onDemand: Boolean = true

    /**
     * When enabled (the default), the plugin will log into the console the
     * routes setup to access the OpenAPI schema, Swagger UI, and ReDoc.
     *
     * Useful to quickly identify how to access the URLs for the API documentation.
     *
     * - Default: `true`.
     */
    public var logPluginRoutes: Boolean = true

    /**
     * Whether to enable internal logging for the plugin. Default: `false`.
     *
     * When enabled the plugin will include additional logging information
     * such as when traversing types, resolving schemas, and building the OpenAPI schema.
     *
     * #### Caution
     * Enabling this option may produce quite verbose logs.
     * It is recommended only for debugging purposes.
     */
    public var enableLogging: Boolean = false

    /**
     * The [ApiDocs] configuration settings.
     */
    private var apiDocs: ApiDocs? = null

    /**
     * The [ApiInfo] metadata for the OpenAPI schema.
     */
    private var apiInfo: ApiInfo? = null

    /**
     * The list of tags to include in the OpenAPI schema.
     */
    private val tags: MutableSet<ApiTag> = mutableSetOf()

    /**
     * Constructs the information for the API documentation.
     *
     * #### Usage
     * ```
     * apiDocs {
     *      openApiUrl = "/openapi.yaml"
     *      openApiFormat = OpenApiFormat.YAML
     *      redocUrl = "/api/redoc"
     *
     *      swagger {
     *          url = "/swagger-ui/"
     *          persistAuthorization = true
     *          withCredentials = true
     *          docExpansion = SwaggerDocExpansion.LIST
     *          displayRequestDuration = true
     *          displayOperationId = true
     *          operationsSorter = SwaggerOperationsSorter.METHOD
     *          uiTheme = SwaggerUITheme.DARK
     *          syntaxTheme = SwaggerSyntaxTheme.NORD
     *          includeErrors = true
     *      }
     * }
     * ```
     *
     * @receiver [ApiDocsBuilder] The builder used to configure the API documentation section.
     */
    public fun apiDocs(builder: ApiDocsBuilder.() -> Unit) {
        apiDocs = ApiDocsBuilder().apply(builder).build()
    }

    /**
     * Sets up the OpenAPI metadata.
     *
     * #### Usage
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
     * @receiver [InfoBuilder] The builder used to configure the API info section.
     */
    public fun info(builder: InfoBuilder.() -> Unit) {
        apiInfo = InfoBuilder().apply(builder).build()
    }

    /**
     * Sets up tags for the API.
     *
     * #### Usage
     * ```
     * tags {
     *     add(name = "Items", description = "Operations related to items.")
     *     add(name = "Users", description = "Operations related to users.")
     * }
     * ```
     *
     * @receiver [TagBuilder] The builder used to configure the API tags section.
     */
    public fun tags(builder: TagBuilder.() -> Unit) {
        val tagsBuilder: TagBuilder = TagBuilder().apply(builder)
        tagsBuilder.build()?.let { tags.addAll(it) }
    }

    /**
     * Builds the final immutable [ApiConfiguration] instance.
     */
    internal fun build(): ApiConfiguration {
        return ApiConfiguration(
            isEnabled = enabled,
            host = host.trimOrNull(),
            onDemand = onDemand,
            logPluginRoutes = logPluginRoutes,
            enableLogging = enableLogging,
            apiDocs = apiDocs ?: ApiDocsBuilder().build(),
            debugUrl = NetworkUtils.normalizeUrl(url = debugUrl, defaultValue = DEFAULT_DEBUG_URL),
            apiInfo = apiInfo,
            apiServers = serverDelegate.build() ?: setOf(ServerBuilder.defaultServer()),
            apiTags = tags.orNull(),
            apiSecuritySchemes = securityDelegate.build()
        )
    }

    private companion object {
        const val DEFAULT_DEBUG_URL: String = "/openapi/debug"
    }
}
