/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.routing

import io.github.perracodex.kopapi.plugin.Swagger
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Defines the necessary routing to expose Swagger UI to the users of the API.
 *
 * @param openapiYamlUrl The URL to access the OpenAPI schema in YAML format.
 * @param swaggerUrl The base URL to access the Swagger UI interface.
 */
internal fun Routing.swaggerRoute(
    openapiYamlUrl: String,
    swaggerUrl: String
) {
    /**
     * Redirect to the main Swagger UI HTML page.
     */
    get(swaggerUrl) {
        call.respondRedirect(url = "$swaggerUrl/index.html")
    }

    /**
     * Handle optional file names to serve various Swagger UI assets.
     */
    get("$swaggerUrl/{fileName?}") {
        val filename: String = call.parameters["fileName"] ?: "index.html"
        val result: OutgoingContent? = Swagger.getContentFor(
            environment = application.environment,
            filename = filename
        )

        result?.let { call.respond(it) }
            ?: call.respond(HttpStatusCode.NotFound)
    }

    /**
     * Serve the JavaScript code that initializes Swagger UI with the provided OpenAPI URL.
     */
    get("$swaggerUrl/swagger-initializer.js") {
        val response: String = Swagger.getSwaggerInitializer(openapiYamlUrl = openapiYamlUrl)
        call.respondText(text = response, contentType = ContentType.Application.JavaScript)
    }
}
