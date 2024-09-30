/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.core

import io.github.perracodex.kopapi.dsl.ApiMetadata
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.util.*

/**
 * Attaches API metadata to a Ktor route, intended for use with terminal route handlers that define an HTTP method.
 *
 * The metadata includes the endpoint full path, the HTTP method associated with the route, and optionally other
 * concrete details such as a summary, description, tags, parameters, request body, and responses.
 *
 * Usage example:
 * ```
 * get("/items/{id}") {
 *     // Handle GET request
 * }.api {
 *     summary = "Retrieve an item"
 *     description = "Fetches an item by its unique identifier."
 *     tags = listOf("Item Operations")
 *     response<Item>(status = HttpStatusCode.OK, description = "Successful fetch")
 *     response<Unit>(status = HttpStatusCode.NotFound, description = "Item not found")
 * }
 * ```
 *
 * @param configure A lambda receiver for configuring the [ApiMetadata].
 * @return The current [Route] instance with attached metadata.
 * @throws IllegalArgumentException If the route does not have an HTTP method selector.
 */
public infix fun Route.api(configure: ApiMetadata.() -> Unit): Route {
    // Resolve the HTTP method of the route: GET, POST, PUT, DELETE, etc.
    val method: HttpMethod = (this.selector as? HttpMethodRouteSelector)?.method
        ?: throw IllegalArgumentException("Route must have an HTTP method selector directly associated with it.")

    // Create an instance of ApiMetadata and apply the configuration.
    val metadata: ApiMetadata = ApiMetadata(
        path = this.extractFullPath(),
        method = method
    ).apply(configure)

    // Store the metadata in the route's attributes.
    this.attributes.put(key = ApiMetadataKey, value = metadata)
    return this
}

/**
 * Defines the key used to store and retrieve API metadata associated with a specific [Route].
 *
 * This key is used as part of the routing configuration process, where API metadata is attached to routes
 * using the `api` extension function.
 */
public val ApiMetadataKey: AttributeKey<ApiMetadata> = AttributeKey("ApiMetadata")

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
private fun Route.extractFullPath(): String {
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
