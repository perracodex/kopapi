/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.routing

import io.github.perracodex.kopapi.dsl.plugin.elements.ApiDocs
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Define Schema endpoints exposed by the plugin.
 *
 * @param apiDocs The [ApiDocs] instance containing the API documentation URLs.
 */
internal fun Routing.openApiRoutes(apiDocs: ApiDocs) {
    get(apiDocs.openapiYamlUrl) {
        val openapiYaml: String = SchemaRegistry.getOpenApiSchema(format = SchemaRegistry.Format.YAML)
        call.respondText(text = openapiYaml, contentType = ContentType.Application.Json)
    }

    get(apiDocs.openapiJsonUrl) {
        val openapiJson: String = SchemaRegistry.getOpenApiSchema(format = SchemaRegistry.Format.JSON)
        call.respondText(text = openapiJson, contentType = ContentType.Application.Json)
    }
}
