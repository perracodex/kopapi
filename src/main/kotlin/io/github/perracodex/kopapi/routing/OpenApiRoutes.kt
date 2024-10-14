/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.routing

import io.github.perracodex.kopapi.core.composer.SchemaComposer
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Define the OpenAPI endpoints exposed by the plugin.
 *
 * @param openapiJsonUrl The URL to access the OpenAPI schema in JSON format.
 * @param openapiYamlUrl The URL to access the OpenAPI schema in YAML format.
 */
internal fun Routing.openApiRoutes(
    openapiJsonUrl: String,
    openapiYamlUrl: String
) {
    get(openapiJsonUrl) {
        val openapiJson = ""
        call.respondText(text = openapiJson, contentType = ContentType.Application.Json)
    }

    get(openapiYamlUrl) {
        val openapiYaml: String = SchemaComposer.getOpenApiSchema(format = SchemaComposer.Format.YAML)
        call.respondText(text = openapiYaml, contentType = ContentType.Application.Json)
    }
}
