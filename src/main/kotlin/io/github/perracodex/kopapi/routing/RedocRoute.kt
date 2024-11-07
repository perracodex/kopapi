/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.routing

import io.github.perracodex.kopapi.dsl.plugin.elements.ApiDocs
import io.github.perracodex.kopapi.providers.RedocProvider
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Define the Redoc OpenAPI endpoint exposed by the plugin.
 *
 * @param apiDocs The [ApiDocs] instance containing the API documentation URLs.
 */
internal fun Routing.redocRoute(apiDocs: ApiDocs) {
    get(apiDocs.redocUrl) {
        val response: String = RedocProvider.getRedocHtml(apiDocs = apiDocs)
        call.respondText(text = response, contentType = ContentType.Text.Html)
    }
}
