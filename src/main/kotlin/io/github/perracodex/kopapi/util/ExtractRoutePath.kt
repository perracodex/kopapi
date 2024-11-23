/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.util

import io.ktor.http.*
import io.ktor.server.routing.*

/**
 * Constructs the full endpoint path of a [Route] by traversing its parent chain and aggregating
 * path segments. Segments are collected from the current route up to the root,
 * omitting segments that don't contribute to the path structure like HTTP method selectors
 * and trailing slashes.
 *
 * **Note:** Optional path parameters are not included in the endpoint path construct.
 * This is because OpenAPI does not support optional path parameters directly in the path template.
 *
 * @return A [RoutePathDetails] object containing the full path of the route and whether it contains optional path parameters.
 */
internal fun Route.extractRoutePath(): RoutePathDetails {
    val segments: MutableList<String> = mutableListOf()
    var currentRoute: Route? = this
    val optionalParameters: MutableSet<String> = mutableSetOf()

    while (currentRoute is RoutingNode) {
        val selector: RouteSelector = currentRoute.selector

        when (selector) {
            is PathSegmentConstantRouteSelector -> selector.value
            is PathSegmentParameterRouteSelector -> "{${selector.name}}"
            is PathSegmentWildcardRouteSelector -> "*"

            // Ignored for path construction.
            is TrailingSlashRouteSelector -> ""
            is HttpMethodRouteSelector -> ""

            // Track optional path parameters for error reporting, but exclude them from the final path.
            is PathSegmentOptionalParameterRouteSelector -> {
                optionalParameters.add(selector.name)
                ""
            }

            else -> ""
        }.let { segment ->
            if (segment.isNotBlank()) {
                segments.add(segment.trim())
            }
        }

        currentRoute = currentRoute.parent
    }

    // Construct the final path by reversing the segments and joining them.
    val path: String = segments.asReversed().joinToString(separator = "/", prefix = "/")
    val endpoint: String = if (path == "/") {
        path // Root path.
    } else {
        path.trimEnd('/')
    }

    // Generate error messages with the full path context.
    val method: HttpMethod? = ((this as? RoutingNode)?.selector as? HttpMethodRouteSelector)?.method
    val errors: Set<String> = optionalParameters.map { name ->
        val endpointMethod: String = method?.let { "${it.value} → " } ?: ""
        "Discarded '$name?' in $endpointMethod$endpoint. " +
                "OpenAPI does not support 'optional' path parameters."
    }.toSet()

    return RoutePathDetails(
        path = endpoint,
        errors = errors.takeIf { it.isNotEmpty() }
    )
}

/**
 * Represents the path of a route and whether it contains optional path parameters.
 *
 * @property path The full path of the route, starting with a `/`.
 *                If the current route is at the root or has no defined path segments, returns `/`.
 * @property errors Set of error messages encountered during path extraction.
 */
internal data class RoutePathDetails(val path: String, val errors: Set<String>?)