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
 * Provides utility functions for the Kopapi plugin setup.
 */
internal class PluginSetup(
    private val application: Application,
    private val apiConfiguration: ApiConfiguration
) {
    /**
     * Handles the plugin being disabled by releasing the schema
     * and unsubscribing from the ApplicationStarting event.
     *
     * @param application The application instance, to match the handler signature.
     */
    fun onPluginDisabled(application: Application) {
        SchemaRegistry.release()
        application.monitor.unsubscribe(definition = ApplicationStarting, handler = this::onPluginDisabled)
    }

    /**
     * Handles the post-startup operations for the Kopapi plugin.
     *
     * @param application The application instance, to match the handler signature.
     */
    fun onPluginStarted(application: Application) {
        if (apiConfiguration.onDemand) {
            logConfiguredEndpoints()
        } else {
            generateOpenApiSchema()
        }

        application.monitor.unsubscribe(definition = ApplicationStarted, handler = this::onPluginStarted)
    }

    /**
     * Configures the routing for the Kopapi plugin.
     */
    fun configureRoutes() {
        application.routing {
            debugRoute(debugUrl = apiConfiguration.debugUrl)
            openApiRoute(apiDocs = apiConfiguration.apiDocs)
            redocRoute(apiDocs = apiConfiguration.apiDocs)
            swaggerRoute(apiDocs = apiConfiguration.apiDocs)
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
     */
    private fun generateOpenApiSchema() {
        application.launch(Dispatchers.IO) {
            SchemaRegistry.getOpenApiSchema(
                format = apiConfiguration.apiDocs.openApiFormat,
                cacheAllFormats = true
            )

            logConfiguredEndpoints()
        }
    }

    /**
     * Logs the configured endpoints if logging is enabled.
     */
    private fun logConfiguredEndpoints() {
        if (apiConfiguration.logPluginRoutes) {

            // Temporarily enable logging to log the plugin routes.
            Tracer.enabled = true

            val serverUrl: String = apiConfiguration.host
                ?: NetworkUtils.getServerUrl(environment = application.environment)

            Tracer<KopapiConfig>().info(
                """
            |Kopapi plugin enabled.
            |   Debug: $serverUrl${apiConfiguration.debugUrl}
            |   OpenAPI: $serverUrl${apiConfiguration.apiDocs.openApiUrl}
            |   Swagger: $serverUrl${apiConfiguration.apiDocs.swagger.url}
            |   ReDoc: $serverUrl${apiConfiguration.apiDocs.redocUrl}
            """.trimMargin()
            )

            // Restore original logging state.
            Tracer.enabled = apiConfiguration.enableLogging
        }
    }
}
