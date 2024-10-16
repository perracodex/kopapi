/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.plugin

import io.github.perracodex.kopapi.core.SchemaRegistry
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
    SchemaRegistry.registerConfiguration(configuration = configuration)

    // Subscribe to the application started event to clear
    // the schema composer when the plugin is disabled.
    if (!configuration.isEnabled) {
        lateinit var applicationStartedHandler: (Application) -> Unit
        applicationStartedHandler = {
            SchemaRegistry.clear()
            application.monitor.unsubscribe(definition = ApplicationStarting, handler = applicationStartedHandler)
        }
        application.monitor.subscribe(definition = ApplicationStarting, handler = applicationStartedHandler)
    }

    // Exit early if the plugin is disabled.
    if (!configuration.isEnabled) {
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
