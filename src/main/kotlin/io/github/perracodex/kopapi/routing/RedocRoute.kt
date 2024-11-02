/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.routing

import io.github.perracodex.kopapi.plugin.Swagger
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Define the Redoc OpenAPI endpoint exposed by the plugin.
 *
 * @param openapiYamlUrl The URL to access the OpenAPI schema in YAML format.
 * @param redocUrl The base URL to access the `Redoc` interface.
 */
internal fun Routing.redocRoute(openapiYamlUrl: String, redocUrl: String) {
    get(redocUrl) {
        val response: String = Swagger.getRedocHtml(openapiYamlUrl = openapiYamlUrl)
        call.respondText(text = response, contentType = ContentType.Text.Html)
    }
}
