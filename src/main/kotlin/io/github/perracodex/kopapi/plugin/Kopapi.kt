/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.plugin

import io.github.perracodex.kopapi.dsl.plugin.element.ApiConfiguration
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.github.perracodex.kopapi.system.Tracer
import io.ktor.server.application.*

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

    val pluginSetup = PluginSetup(application = application, apiConfiguration = apiConfiguration)

    // Handle plugin disabling by clearing schema on application starting.
    if (!apiConfiguration.isEnabled) {
        application.monitor.subscribe(definition = ApplicationStarting, handler = pluginSetup::onPluginDisabled)
        return@createApplicationPlugin
    }

    // Setup plugin routes.
    pluginSetup.configureRoutes()

    // Subscribe to the application started event to perform post-startup operations.
    application.monitor.subscribe(definition = ApplicationStarted, handler = pluginSetup::onPluginStarted)
}
