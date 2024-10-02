/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.plugin

import io.github.perracodex.kopapi.routing.kopapiRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

/**
 * Kopapi plugin that provides OpenAPI functionality.
 */
public val Kopapi: ApplicationPlugin<KopapiConfig> = createApplicationPlugin(
    name = "Kopapi",
    createConfiguration = ::KopapiConfig
) {
    val openapiJsonUrl: String = this.pluginConfig.openapiJsonUrl.trim()
    val openapiYamlUrl: String = this.pluginConfig.openapiYamlUrl.trim()
    val swaggerUrl: String = this.pluginConfig.swaggerUrl.trim()
    val debugUrl: String = this.pluginConfig.debugUrl.trim()

    // Validate the configuration.
    require(openapiJsonUrl.isNotBlank()) { "The OpenAPI JSON URL must not be empty." }
    require(openapiYamlUrl.isNotBlank()) { "The OpenAPI YAML URL must not be empty." }
    require(swaggerUrl.isNotBlank()) { "The Swagger UI URL must not be empty." }
    require(debugUrl.isNotBlank()) { "The debug URL must not be empty." }
    check(value = openapiJsonUrl != openapiYamlUrl) { "The OpenAPI JSON and YAML URLs must be different." }

    // Configure the plugin endpoints using the extracted function.
    application.routing {
        kopapiRoutes(
            openapiJsonUrl = openapiJsonUrl,
            openapiYamlUrl = openapiYamlUrl,
            swaggerUrl = swaggerUrl,
            debugUrl = debugUrl
        )
    }
}
