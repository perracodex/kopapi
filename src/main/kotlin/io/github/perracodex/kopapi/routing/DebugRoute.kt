/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.routing

import io.github.perracodex.kopapi.view.DebugInfo
import io.github.perracodex.kopapi.view.DebugPanelView
import io.github.perracodex.kopapi.view.DebugViewUtils
import io.github.perracodex.kopapi.view.annotation.DebugViewApi
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Define the Debug endpoint exposed by the plugin.
 *
 * @param debugUrl The URL to access the debug panel.
 */
@OptIn(DebugViewApi::class)
internal fun Routing.debugRoute(debugUrl: String) {
    staticResources(remotePath = "/static-kopapi", basePackage = "debug")

    get(debugUrl) {
        runCatching {
            val debugInfo: DebugInfo = DebugViewUtils().extractSections()
            val debugPanelView = DebugPanelView(debugInfo = debugInfo)

            call.respondHtml(status = HttpStatusCode.OK) {
                debugPanelView.build(html = this)
            }
        }.onFailure { cause ->
            val stackTrace: String = StringWriter().apply {
                cause.printStackTrace(PrintWriter(this))
            }.toString()

            call.respondText(
                text = "Failed to render the debug panel:\n$stackTrace",
                status = HttpStatusCode.InternalServerError,
                contentType = ContentType.Text.Plain
            )
        }
    }
}
