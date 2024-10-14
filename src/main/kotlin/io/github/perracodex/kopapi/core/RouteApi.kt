/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.core

import io.github.perracodex.kopapi.dsl.api.builders.ApiMetadataBuilder
import io.github.perracodex.kopapi.utils.extractRoutePath
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.http.*
import io.ktor.server.routing.*

/**
 * Attaches API metadata to a Ktor route, intended for use with terminal route handlers that define an HTTP method.
 *
 * The metadata includes the endpoint full path, the HTTP method associated with the route, and optionally other
 * concrete details such as a summary, description, tags, parameters, request body, and responses.
 *
 * #### Sample Usage
 * ```
 * get("/items/{group_id}/{item_id?}") {
 *     // Handle GET request
 * } api {
 *     summary = "Retrieve data items."
 *     description = "Fetches all items for a group."
 *     tags("Items", "Data")
 *     pathParameter<Uuid>("group_id") { description = "The Id of the group." }
 *     queryParameter<String>("item_id") { description = "Optional item Id." }
 *     response<List<Item>>(HttpStatusCode.OK) { description = "Successful" }
 *     response(HttpStatusCode.NotFound) { description = "Data not found" }
 * }
 * ```
 *
 * @param configure A lambda receiver for configuring the [ApiMetadataBuilder].
 * @return The current [Route] instance with attached metadata.
 * @throws IllegalArgumentException If the route does not have an HTTP method selector.
 *
 * @see [ApiMetadataBuilder]
 */
public infix fun Route.api(configure: ApiMetadataBuilder.() -> Unit): Route {
    if (this !is RoutingNode) {
        throw KopapiException(message = buildApiErrorMessage(route = this))
    }

    // Resolve the HTTP method of the route: GET, POST, PUT, DELETE, etc.
    val method: HttpMethod = (this.selector as? HttpMethodRouteSelector)?.method
        ?: throw KopapiException(message = buildApiErrorMessage(route = this))

    val endpointPath: String = this.extractRoutePath()

    // Build the metadata using the provided configuration.
    val builder: ApiMetadataBuilder = ApiMetadataBuilder(
        endpoint = "[$method] $endpointPath"
    ).apply(configure)
    val apiMetadata = ApiMetadata(
        path = endpointPath,
        method = method,
        summary = builder.summary.trimOrNull(),
        description = builder.description.trimOrNull(),
        tags = builder.tags.takeIf { it.isNotEmpty() },
        parameters = builder.parameters.takeIf { it.isNotEmpty() },
        requestBody = builder.requestBody,
        responses = builder.responses.takeIf { it.isNotEmpty() },
        securitySchemes = builder.securitySchemes.takeIf { it.isNotEmpty() }
    )

    // Register the metadata for later retrieval.
    SchemaComposer.registerApiMetadata(metadata = apiMetadata)

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
        The current route "${route.extractRoutePath()}" does not have an HTTP method associated with it.

        Possible causes:
        - You might have applied 'api' to a route that is not directly tied to a specific HTTP method,
        - Make sure 'api' is applied to a route either by infix or chained:
            ```
            post { ... } api { ... } // Infix example
            post { ... }.api { ... } // Chained '.' example
            ```

        Example of proper usage (infix notation):
            ```
            get("/items/{group_id}/{item_id?}") {
                // Handle GET request
            } api { 
                summary = "Retrieve data items."
                description = "Fetches all items for a group."
                tags("Items", "Data")
                pathParameter<Uuid>("group_id") { description = "The Id of the group to resolve." }
                queryParameter<String>("item_id") { description = "Optional item Id to locate." }
                response<List<Item>>(HttpStatusCode.OK) { description = "Successful fetch" }
                response(HttpStatusCode.NotFound) { description = "Data not found" }
            }
            ```

            Error
    """.trimIndent()
}
