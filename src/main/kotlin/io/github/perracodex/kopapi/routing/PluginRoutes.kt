/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.routing

import io.github.perracodex.kopapi.core.SchemaProvider
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Define the endpoints exposed by the plugin.
 */
internal fun Routing.kopapiRoutes(
    openapiJsonUrl: String,
    openapiYamlUrl: String,
    swaggerUrl: String,
    debugUrl: String
) {
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
    get(debugUrl) {
        val apiMetadataJson: String = SchemaProvider.getApiMetadataJson(application = call.application)
        call.respondText(text = apiMetadataJson, contentType = ContentType.Application.Json)
    }
}
