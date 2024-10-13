/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.plugin

import io.github.perracodex.kopapi.api.debugRoute
import io.github.perracodex.kopapi.api.openApiRoutes
import io.github.perracodex.kopapi.api.swaggerRoute
import io.github.perracodex.kopapi.core.SchemaProvider
import io.github.perracodex.kopapi.plugin.KopapiConfig.Companion.DEFAULT_DEBUG_URL
import io.github.perracodex.kopapi.plugin.KopapiConfig.Companion.DEFAULT_OPENAPI_JSON_URL
import io.github.perracodex.kopapi.plugin.KopapiConfig.Companion.DEFAULT_OPENAPI_YAML_URL
import io.github.perracodex.kopapi.plugin.KopapiConfig.Companion.DEFAULT_SWAGGER_URL
import io.github.perracodex.kopapi.utils.trimOrDefault
import io.ktor.server.application.*
import io.ktor.server.routing.*

/**
 * Kopapi plugin that provides OpenAPI functionality.
 */
public val Kopapi: ApplicationPlugin<KopapiConfig> = createApplicationPlugin(
    name = "Kopapi",
    createConfiguration = ::KopapiConfig
) {
    // Set the schema provider to enabled/disabled based on the plugin configuration,
    // so that the Routes API definitions can be collected or discarded.
    SchemaProvider.isEnabled = this.pluginConfig.enabled

    // Exit early if the plugin is disabled.
    if (!this.pluginConfig.enabled) {
        // `Routes.api` definitions can happen before or after the plugin is installed,
        // so we need to also clear the schema provider when the application starts
        // to ensure all unused resources are freed.
        this.application.monitor.subscribe(ApplicationStarted) {
            SchemaProvider.clear()
        }

        return@createApplicationPlugin
    }

    // Set th API documentation info.
    SchemaProvider.apiInfo = this.pluginConfig.apiInfo
    // Add the servers to the schema provider.
    SchemaProvider.servers.addAll(this.pluginConfig.servers.get())

    // Get the URLs from the plugin configuration.
    // If any of the URLs are empty, restore the default values.
    val openapiJsonUrl: String = this.pluginConfig.openapiJsonUrl.trimOrDefault(DEFAULT_OPENAPI_JSON_URL)
    val openapiYamlUrl: String = this.pluginConfig.openapiYamlUrl.trimOrDefault(DEFAULT_OPENAPI_YAML_URL)
    val swaggerUrl: String = this.pluginConfig.swaggerUrl.trimOrDefault(DEFAULT_SWAGGER_URL)
    val debugUrl: String = this.pluginConfig.debugUrl.trimOrDefault(DEFAULT_DEBUG_URL)

    // Configure the plugin endpoints using the extracted function.
    application.routing {
        debugRoute(debugUrl = debugUrl)
        openApiRoutes(
            openapiJsonUrl = openapiJsonUrl,
            openapiYamlUrl = openapiYamlUrl
        )
        swaggerRoute(swaggerUrl = swaggerUrl)
    }
}
