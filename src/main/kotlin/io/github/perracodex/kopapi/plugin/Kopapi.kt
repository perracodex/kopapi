/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.plugin

import io.github.perracodex.kopapi.core.composer.SchemaComposer
import io.github.perracodex.kopapi.routing.debugRoute
import io.github.perracodex.kopapi.routing.openApiRoutes
import io.github.perracodex.kopapi.routing.swaggerRoute
import io.ktor.server.application.*
import io.ktor.server.routing.*

/**
 * Kopapi plugin that provides OpenAPI functionality.
 */
public val Kopapi: ApplicationPlugin<KopapiConfig> = createApplicationPlugin(
    name = "Kopapi",
    createConfiguration = ::KopapiConfig
) {
    // Register the configuration with the schema provider.
    val configuration: Configuration = this.pluginConfig.build()
    SchemaComposer.registerConfiguration(configuration = configuration)

    // Exit early if the plugin is disabled.
    if (!configuration.isEnabled) {
        // `Routes.api` definitions can happen before or after the plugin is installed,
        // so we need to also clear the schema provider when the application starts
        // to ensure all unused resources are freed.
        this.application.monitor.subscribe(ApplicationStarted) {
            SchemaComposer.clear()
        }

        return@createApplicationPlugin
    }

    // Configure the plugin endpoints using the extracted function.
    application.routing {
        debugRoute(debugUrl = configuration.debugUrl)
        openApiRoutes(
            openapiJsonUrl = configuration.openapiJsonUrl,
            openapiYamlUrl = configuration.openapiYamlUrl
        )
        swaggerRoute(swaggerUrl = configuration.swaggerUrl)
    }
}
