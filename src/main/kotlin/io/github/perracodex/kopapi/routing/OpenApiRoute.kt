/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.routing

import io.github.perracodex.kopapi.dsl.plugin.element.ApiDocs
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Define the route for the OpenAPI schema.
 *
 * @param apiDocs The [ApiDocs] instance containing the API documentation URLs.
 */
internal fun Routing.openApiRoute(apiDocs: ApiDocs) {
    get(apiDocs.openApiUrl) {
        val openApi: String = SchemaRegistry.getOpenApiSchema(format = apiDocs.openApiFormat, cacheAllFormats = false)
        call.respondText(text = openApi, contentType = ContentType.Application.Json)
    }
}
