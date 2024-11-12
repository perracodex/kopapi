/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.plugin

import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.plugin.builders.ApiDocsBuilder
import io.github.perracodex.kopapi.dsl.plugin.builders.InfoBuilder
import io.github.perracodex.kopapi.dsl.plugin.builders.TagBuilder
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiConfiguration
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiDocs
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiInfo
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiTag
import io.github.perracodex.kopapi.dsl.security.delegate.ISecurityConfigurable
import io.github.perracodex.kopapi.dsl.security.delegate.SecurityDelegate
import io.github.perracodex.kopapi.dsl.servers.builders.ServerBuilder
import io.github.perracodex.kopapi.dsl.servers.delegate.IServerConfigurable
import io.github.perracodex.kopapi.dsl.servers.delegate.ServerDelegate
import io.github.perracodex.kopapi.utils.NetworkUtils
import io.github.perracodex.kopapi.utils.orNull

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
