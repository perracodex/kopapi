/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.routing

import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Define Schema endpoints exposed by the plugin.
 *
 * @param openapiJsonUrl The URL to access the OpenAPI schema in JSON format.
 * @param openapiYamlUrl The URL to access the OpenAPI schema in YAML format.
 */
internal fun Routing.openApiRoutes(
    openapiJsonUrl: String,
    openapiYamlUrl: String
) {
    get(openapiJsonUrl) {
        val openapiJson: String = SchemaRegistry.getOpenApiSchema(format = SchemaRegistry.Format.JSON)
        call.respondText(text = openapiJson, contentType = ContentType.Application.Json)
    }

    get(openapiYamlUrl) {
        val openapiYaml: String = SchemaRegistry.getOpenApiSchema(format = SchemaRegistry.Format.YAML)
        call.respondText(text = openapiYaml, contentType = ContentType.Application.Json)
    }
}
