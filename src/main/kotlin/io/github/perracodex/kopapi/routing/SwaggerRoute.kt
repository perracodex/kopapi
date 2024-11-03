/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.routing

import io.github.perracodex.kopapi.dsl.plugin.elements.ApiDocs
import io.github.perracodex.kopapi.plugin.Swagger
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Defines the necessary routing to expose Swagger UI to the users of the API.
 *
 * @param apiDocs The [ApiDocs] instance containing the API documentation URLs.
 */
internal fun Routing.swaggerRoute(apiDocs: ApiDocs) {
    /**
     * Redirect to the main Swagger UI HTML page.
     */
    get(apiDocs.swaggerUrl) {
        call.respondRedirect(url = "${apiDocs.swaggerUrl}/index.html")
    }

    /**
     * Handle optional file names to serve various Swagger UI assets.
     */
    get("${apiDocs.swaggerUrl}/{fileName?}") {
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
    get("${apiDocs.swaggerUrl}/swagger-initializer.js") {
        val response: String = Swagger.getSwaggerInitializer(
            openapiYamlUrl = apiDocs.openapiYamlUrl,
            withCredentials = apiDocs.withCredentials,
            syntaxTheme = apiDocs.syntaxTheme
        )
        call.respondText(text = response, contentType = ContentType.Application.JavaScript)
    }
}
