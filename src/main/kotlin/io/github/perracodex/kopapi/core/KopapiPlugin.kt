/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.core

import io.github.perracodex.kopapi.parser.SchemaProvider
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Kopapi plugin that provides OpenAPI functionality.
 */
public val KopapiPlugin: ApplicationPlugin<KopapiPluginConfig> = createApplicationPlugin(
    name = "KopapiPlugin",
    createConfiguration = ::KopapiPluginConfig
) {
    val openapiJsonUrl: String = this.pluginConfig.openapiJsonUrl
    val openapiYamlUrl: String = this.pluginConfig.openapiYamlUrl
    val swaggerUrl: String = this.pluginConfig.swaggerUrl
    val debugUrl: String = this.pluginConfig.debugUrl

    // Validate the URLs.
    require(openapiJsonUrl.isNotBlank()) { "The OpenAPI JSON URL must not be empty." }
    require(openapiYamlUrl.isNotBlank()) { "The OpenAPI YAML URL must not be empty." }
    require(swaggerUrl.isNotBlank()) { "The Swagger UI URL must not be empty." }
    require(debugUrl.isNotBlank()) { "The debug URL must not be empty." }
    check(value = openapiJsonUrl != openapiYamlUrl) { "The OpenAPI JSON and YAML URLs must be different." }

    // Configure the plugin endpoints.
    application.routing {
        // Provide the OpenAPI schema in JSON format.
        get(openapiJsonUrl) {
            val openapiJson = ""
            call.respondText(text = openapiJson, contentType = ContentType.Application.Json)
        }

        // Provide the OpenAPI schema in YAML format.
        get(openapiYamlUrl) {
            val openapiYaml = ""
            call.respondText(text = openapiYaml, contentType = ContentType.Application.Json)
        }

        // Provide the Swagger UI.
        get(swaggerUrl) {
            val swaggerUi = ""
            call.respondText(text = swaggerUi, contentType = ContentType.Text.Html)
        }

        // Provide the API metadata, for debugging purposes.
        get(this@createApplicationPlugin.pluginConfig.debugUrl) {
            val apiMetadataJson: String = SchemaProvider.getApiMetadataJson(application = call.application)
            call.respondText(text = apiMetadataJson, contentType = ContentType.Application.Json)
        }
    }
}

/**
 * Configuration for the [KopapiPlugin].
 */
public class KopapiPluginConfig {
    /**
     * The URL to provide the OpenAPI schema in JSON format.
     * Relative to the server root URL. Default is `openapi/json`.
     */
    public var openapiJsonUrl: String = "openapi/json"

    /**
     * The URL to provide the OpenAPI schema in YAML format.
     * Relative to the server root URL. Default is `openapi/yaml`.
     */
    public var openapiYamlUrl: String = "openapi/yaml"

    /**
     * The URL to provide the Swagger UI.
     * Relative to the server root URL. Default is `swagger`.
     */
    public var swaggerUrl: String = "swagger"

    /**
     * The URL to provide the API metadata, for debugging purposes.
     * Relative to the server root URL. Default is `openapi/debug`.
     */
    public var debugUrl: String = "openapi/debug"
}
