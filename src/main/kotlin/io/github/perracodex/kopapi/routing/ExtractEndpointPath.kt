/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.routing

import io.ktor.server.routing.*

/**
 * Constructs the full path of a route by aggregating path segments from the current route up to the root,
 * by traversing the parent chain of the current route and collects segments defined by various
 * types of [RouteSelector] types.
 *
 * It builds a full path by piecing together these segments in the order from the root to the current route.
 * The method is designed to ignore segments that do not directly contribute to the path structure,
 * such as HTTP method selectors and trailing slashes.
 *
 * @return A string representing the full path from the root to the current route, starting with a `/`.
 * If the current route is at the root or no path segments are defined, the function returns just `/`.
 */
internal fun Route.extractEndpointPath(): String {
    val segments: MutableList<String> = mutableListOf()
    var currentRoute: Route? = this

    // Traverse the parent chain of the current route and collect path segments.
    while (currentRoute != null && currentRoute.selector !is RootRouteSelector) {
        val segment: String = when (val selector: RouteSelector = currentRoute.selector) {
            is PathSegmentConstantRouteSelector -> selector.value
            is PathSegmentParameterRouteSelector -> "{${selector.name}}"
            is PathSegmentOptionalParameterRouteSelector -> "{${selector.name}?}"
            is PathSegmentWildcardRouteSelector -> "*"
            is TrailingSlashRouteSelector -> ""
            is HttpMethodRouteSelector -> "" // Skip HTTP method selectors
            else -> ""
        }
        if (segment.isNotEmpty()) {
            segments.add(segment)
        }
        currentRoute = currentRoute.parent
    }

    return segments.reversed().joinToString(separator = "/", prefix = "/")
}
