/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.utils

import io.ktor.server.routing.*

/**
 * Constructs the full endpoint path of a [Route] by traversing its parent chain and aggregating
 * path segments. Segments are collected from the current route up to the root,
 * omitting segments that don't contribute to the path structure like HTTP method selectors
 * and trailing slashes.
 *
 * @return A string representing the full path, starting with a `/`. If the current route is
 * at the root or has no defined path segments, returns `/`.
 */
internal fun Route.extractRoutePath(): String {
    val segments: MutableList<String> = mutableListOf()
    var currentRoute: Route? = this

    while (currentRoute is RoutingNode) {
        val selector: RouteSelector = currentRoute.selector

        when (selector) {
            is PathSegmentConstantRouteSelector -> selector.value
            is PathSegmentParameterRouteSelector -> "{${selector.name}}"
            is PathSegmentWildcardRouteSelector -> "*"
            is TrailingSlashRouteSelector, is HttpMethodRouteSelector -> "" // Ignored for path construction.
            is PathSegmentOptionalParameterRouteSelector -> "" // Query parameters are not included in the path.
            else -> ""
        }.let { segment ->
            if (segment.isNotEmpty()) {
                segments.add(segment)
            }
        }

        currentRoute = currentRoute.parent
    }

    return segments.asReversed()
        .joinToString(separator = "/", prefix = "/")
        .trimEnd('/')
}
