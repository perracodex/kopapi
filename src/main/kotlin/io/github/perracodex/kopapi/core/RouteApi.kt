/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.core

import io.github.perracodex.kopapi.dsl.ApiMetadata
import io.github.perracodex.kopapi.parser.SchemaProvider
import io.github.perracodex.kopapi.parser.extractEndpointPath
import io.ktor.http.*
import io.ktor.server.routing.*

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
 * } api {
 *     summary = "Retrieve an item"
 *     description = "Fetches an item by its unique identifier."
 *     tags = listOf("Item Operations")
 *     response<Item>(HttpStatusCode.OK, "Successful fetch")
 *     response(HttpStatusCode.NotFound, "Item not found")
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
        ?: throw KopapiException(message = buildApiErrorMessage(route = this))

    // Create an instance of ApiMetadata and apply the configuration.
    val metadata: ApiMetadata = ApiMetadata(
        path = this.extractEndpointPath(),
        method = method
    ).apply(configure)

    // Store the metadata in the route's attributes.
    this.attributes.put(key = SchemaProvider.ApiMetadataKey, value = metadata)
    return this
}

/**
 * Builds an error message for when the [Route.api] extension function
 * is applied to a route without an HTTP method.
 *
 * @param route The [Route] missing an HTTP method.
 * @return A formatted error message string.
 */
private fun buildApiErrorMessage(route: Route): String {
    return """
        Error: The 'api' extension function must be attached to a route that has an HTTP method (e.g., GET, POST, PUT, DELETE).
        The current route "${route.extractEndpointPath()}" does not have an HTTP method associated with it.

        Possible causes:
        - You might have forgotten to define an HTTP method (e.g., `get("/path")`, `post("/path")`).
        - You might have applied 'api' to a route that is not directly tied to a specific method.

        Example of proper usage:
        ```
        get("/items/{id}") {
            // Handle GET request
        } api {
            summary = "Retrieve an item"
        }
        ```
    """.trimIndent()
}
