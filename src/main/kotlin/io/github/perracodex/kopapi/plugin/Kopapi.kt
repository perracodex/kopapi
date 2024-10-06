/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.plugin

import io.github.perracodex.kopapi.routing.kopapiRoutes
import io.github.perracodex.kopapi.utils.Tracer
import io.ktor.server.application.*
import io.ktor.server.routing.*

/**
 * Kopapi plugin that provides OpenAPI functionality.
 */
public val Kopapi: ApplicationPlugin<KopapiConfig> = createApplicationPlugin(
    name = "Kopapi",
    createConfiguration = ::KopapiConfig
) {
    val tracer = Tracer<KopapiConfig>()

    val openapiJsonUrl: String = this.pluginConfig.openapiJsonUrl.trim()
    val openapiYamlUrl: String = this.pluginConfig.openapiYamlUrl.trim()
    val swaggerUrl: String = this.pluginConfig.swaggerUrl.trim()
    val debugUrl: String = this.pluginConfig.debugUrl.trim()

    // Validate the configuration.
    require(openapiJsonUrl.isNotBlank()) { "The OpenAPI JSON URL must not be empty." }
    require(openapiYamlUrl.isNotBlank()) { "The OpenAPI YAML URL must not be empty." }
    require(swaggerUrl.isNotBlank()) { "The Swagger UI URL must not be empty." }
    require(debugUrl.isNotBlank()) { "The debug URL must not be empty." }
    require(setOf(openapiJsonUrl, openapiYamlUrl, swaggerUrl, debugUrl).size == 4) {
        "Each URL in must be unique. Duplicate URLs detected: " +
                "OpenAPI JSON URL: $openapiJsonUrl, " +
                "OpenAPI YAML URL: $openapiYamlUrl, " +
                "Swagger UI URL: $swaggerUrl, " +
                "Debug URL: $debugUrl"
    }

    // If no servers are provided, add a default one.
    if (this.pluginConfig.servers.isEmpty()) {
        val server: String = this.pluginConfig.servers.addDefault()
        tracer.warning("No servers were provided. Added a default server: $server")
    }

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
