/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.plugin

import io.github.perracodex.kopapi.dsl.plugin.element.ApiConfiguration
import io.github.perracodex.kopapi.routing.debugRoute
import io.github.perracodex.kopapi.routing.openApiRoute
import io.github.perracodex.kopapi.routing.redocRoute
import io.github.perracodex.kopapi.routing.swaggerRoute
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.github.perracodex.kopapi.system.Tracer
import io.github.perracodex.kopapi.util.NetworkUtils
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Kopapi plugin that provides OpenAPI functionality.
 */
public val Kopapi: ApplicationPlugin<KopapiConfig> = createApplicationPlugin(
    name = "Kopapi",
    createConfiguration = ::KopapiConfig
) {
    val apiConfiguration: ApiConfiguration = this.pluginConfig.build()

    // Enable logging if the plugin is configured to do so.
    Tracer.enabled = apiConfiguration.enableLogging

    // Register the configuration with the schema provider.
    SchemaRegistry.registerApiConfiguration(apiConfiguration = apiConfiguration)

    // Handle plugin disabling by clearing schema on application starting.
    if (!apiConfiguration.isEnabled) {
        lateinit var eventHandler: (Application) -> Unit
        eventHandler = {
            SchemaRegistry.clear()
            application.monitor.unsubscribe(definition = ApplicationStarting, handler = eventHandler)
        }
        application.monitor.subscribe(definition = ApplicationStarting, handler = eventHandler)

        // Exit early when the plugin is disabled.
        return@createApplicationPlugin
    }

    // Setup plugin routes.
    setupRoutes(application = application, apiConfig = apiConfiguration)

    // Subscribe to the application started event to perform post-startup operations.
    lateinit var eventHandler: (Application) -> Unit
    eventHandler = {
        logConfiguredEndpoints(application = application, apiConfig = apiConfiguration)
        generateOpenApiSchema(application = application, apiConfig = apiConfiguration)
        application.monitor.unsubscribe(definition = ApplicationStarted, handler = eventHandler)
    }
    application.monitor.subscribe(definition = ApplicationStarted, handler = eventHandler)
}

/**
 * Configures the routing for the Kopapi plugin.
 *
 * @param application The application reference.
 * @param apiConfig The API configuration for the plugin.
 */
private fun setupRoutes(application: Application, apiConfig: ApiConfiguration) {
    application.routing {
        debugRoute(debugUrl = apiConfig.debugUrl)
        openApiRoute(apiDocs = apiConfig.apiDocs)
        redocRoute(apiDocs = apiConfig.apiDocs)
        swaggerRoute(apiDocs = apiConfig.apiDocs)
    }
}

/**
 * Logs the configured endpoints if logging is enabled.
 *
 * @param application The application reference.
 * @param apiConfig The API configuration for the plugin.
 */
private fun logConfiguredEndpoints(application: Application, apiConfig: ApiConfiguration) {
    if (apiConfig.logPluginRoutes) {

        // Temporarily enable logging to log the plugin routes.
        Tracer.enabled = true

        var serverUrl: String? = apiConfig.host
        var hostNotes = ""

        if (serverUrl == null) {
            serverUrl = NetworkUtils.getServerUrl(environment = application.environment)
            hostNotes = """
                |   Note: 'host' has not been explicitly set in the Kopapi configuration.
                |   In containerized or remote environments, set 'host' to your public IP/domain for correct URLs.
                |
                """.trimMargin()
        }

        Tracer<KopapiConfig>().info(
            """
            |Kopapi plugin enabled.
            |   Debug: $serverUrl${apiConfig.debugUrl}
            |   OpenAPI: $serverUrl${apiConfig.apiDocs.openApiUrl}
            |   Swagger: $serverUrl${apiConfig.apiDocs.swagger.url}
            |   ReDoc: $serverUrl${apiConfig.apiDocs.redocUrl}
            |$hostNotes
            """.trimMargin()
        )

        // Restore original logging state.
        Tracer.enabled = apiConfig.enableLogging
    }
}

/**
 * Generate the OpenAPI schema if the plugin is configured to do so.
 *
 * When on-demand is disabled, all formats are cached so they are ready for the debug panel too.
 *
 * #### Attention
 * This operation must be done right after the application is fully started,
 * so that all routes are registered and available for the schema generation.
 *
 * @param application The application reference.
 * @param apiConfig The API configuration for the plugin.
 */
private fun generateOpenApiSchema(application: Application, apiConfig: ApiConfiguration) {
    if (!apiConfig.onDemand) {
        application.launch(Dispatchers.IO) {
            SchemaRegistry.getOpenApiSchema(
                format = apiConfig.apiDocs.openApiFormat,
                cacheAllFormats = true
            )
        }
    }
}
