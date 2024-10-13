/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.routing

import io.github.perracodex.kopapi.view.DebugPanelView
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*

/**
 * Define the Debug endpoint exposed by the plugin.
 *
 * @param debugUrl The URL to access the debug panel.
 */
internal fun Routing.debugRoute(
    debugUrl: String
) {
    staticResources(remotePath = "/static-kopapi", basePackage = "debug")

    get(debugUrl) {
        call.respondHtml(status = HttpStatusCode.OK) {
            DebugPanelView().build(html = this)
        }
    }
}
