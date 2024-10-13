/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.api

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Define the Swagger-UI endpoint exposed by the plugin.
 *
 * @param swaggerUrl The URL to access the Swagger UI.
 */
internal fun Routing.swaggerRoute(
    swaggerUrl: String
) {
    get(swaggerUrl) {
        val swaggerUi = ""
        call.respondText(text = swaggerUi, contentType = ContentType.Text.Html)
    }
}
