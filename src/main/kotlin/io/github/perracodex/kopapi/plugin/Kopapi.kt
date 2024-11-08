/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.plugin

import io.github.perracodex.kopapi.dsl.plugin.elements.ApiConfiguration
import io.github.perracodex.kopapi.routing.debugRoute
import io.github.perracodex.kopapi.routing.openApiRoutes
import io.github.perracodex.kopapi.routing.redocRoute
import io.github.perracodex.kopapi.routing.swaggerRoute
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.github.perracodex.kopapi.system.Tracer
import io.github.perracodex.kopapi.utils.NetworkUtils
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
    val apiConfiguration: ApiConfiguration = this.pluginConfig.build()
    SchemaRegistry.registerApiConfiguration(apiConfiguration = apiConfiguration)

    // Subscribe to the application started event to clear
    // the schema composer when the plugin is disabled.
    if (!apiConfiguration.isEnabled) {
        lateinit var applicationStartedHandler: (Application) -> Unit
        applicationStartedHandler = {
            SchemaRegistry.clear()
            application.monitor.unsubscribe(definition = ApplicationStarting, handler = applicationStartedHandler)
        }
        application.monitor.subscribe(definition = ApplicationStarting, handler = applicationStartedHandler)
    }

    // Exit early if the plugin is disabled.
    if (!apiConfiguration.isEnabled) {
        return@createApplicationPlugin
    }

    // Configure the plugin endpoints using the extracted function.
    application.routing {
        debugRoute(debugUrl = apiConfiguration.debugUrl)
        openApiRoutes(apiDocs = apiConfiguration.apiDocs)
        redocRoute(apiDocs = apiConfiguration.apiDocs)
        swaggerRoute(apiDocs = apiConfiguration.apiDocs)
    }

    // Dump the configured endpoints to the log.
    val server: String = NetworkUtils.getServerUrl(environment = application.environment)
    Tracer<KopapiConfig>().info(
        """
        |Kopapi plugin enabled.
        |  Debug: $server${apiConfiguration.debugUrl}
        |  OpenAPI YAML: $server${apiConfiguration.apiDocs.openapiYamlUrl}
        |  OpenAPI JSON: $server${apiConfiguration.apiDocs.openapiJsonUrl}
        |  Swagger UI: $server${apiConfiguration.apiDocs.swagger.url}
        |  ReDoc: $server${apiConfiguration.apiDocs.redocUrl}
        """.trimMargin()
    )

    // Enable logging if the plugin is configured to do so.
    // Done as last step to allow logging of the plugin setup.
    Tracer.enabled = this.pluginConfig.enableLogging
}
