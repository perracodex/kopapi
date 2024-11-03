/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation

import io.github.perracodex.kopapi.dsl.markers.OperationDsl
import io.github.perracodex.kopapi.dsl.operation.builders.operation.ApiOperationBuilder
import io.github.perracodex.kopapi.dsl.operation.elements.ApiOperation
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.utils.extractRoutePath
import io.ktor.http.*
import io.ktor.server.routing.*

/**
 * Defines API Operation metadata for a Ktor route.
 * Intended for use with terminal route handlers that define an HTTP method.
 *
 * #### Sample Usage
 * ```
 * get("/items/{data_id}/{item_id?}") {
 *     // Handle GET request
 * } api {
 *     tags = setOf("Items", "Data")
 *
 *     summary = "Retrieve data items."
 *
 *     description = "Fetches all items for a data set."
 *
 *     operationId = "getDataItems"
 *
 *     pathParameter<Uuid>("data_id") { description = "The data Id." }
 *
 *     queryParameter<String>("item_id") { description = "Optional item Id." }
 *
 *     response<List<Item>>(HttpStatusCode.OK) { description = "Successful." }
 *
 *     response(HttpStatusCode.NotFound) { description = "Data not found." }
 *
 *     bearerSecurity(name = "Authentication") {
 *          description = "Access to data."
 *     }
 * }
 * ```
 *
 * @param configure A lambda receiver for configuring the [ApiOperationBuilder].
 * @return The current [Route] instance
 * @throws KopapiException If the route does not have an HTTP method selector.
 *
 * @see [ApiOperationBuilder]
 */
@OperationDsl
public infix fun Route.api(configure: ApiOperationBuilder.() -> Unit): Route {
    if (this !is RoutingNode) {
        throw KopapiException(message = buildApiErrorMessage(route = this))
    }

    // Resolve the HTTP method of the route: GET, POST, PUT, DELETE, etc.
    val method: HttpMethod = (this.selector as? HttpMethodRouteSelector)?.method
        ?: throw KopapiException(message = buildApiErrorMessage(route = this))

    val endpointPath: String = this.extractRoutePath()

    // Build the operation using the provided configuration.
    val builder: ApiOperationBuilder = ApiOperationBuilder(
        endpoint = "[$method] $endpointPath"
    ).apply(configure)
    val apiOperation: ApiOperation = builder.build(
        method = method,
        endpointPath = endpointPath
    )

    // Register the operation with the schema registry
    // for later use in generating OpenAPI documentation.
    SchemaRegistry.registerApiOperation(operation = apiOperation)

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
        To resolve:
            - Make sure 'api' is applied to a route either by infix or chained:
                ```
                post { ... } api { ... } // Infix example
                post { ... }.api { ... } // Chained '.' example
                ```

        Example of proper usage (infix notation):
            ```
            get("/items/{data_id}/{item_id?}") {
                // Handle GET request
            } api {
                tags = setOf("Items", "Data")
                summary = "Retrieve data items."
                description = "Fetches all items for a data set."
                operationId = "getDataItems"
                pathParameter<Uuid>("data_id") { description = "The data Id." }
                queryParameter<String>("item_id") { description = "Optional item Id to locate." }
                response<List<Item>>(status = HttpStatusCode.OK) { description = "Successful fetch." }
                response(status = HttpStatusCode.NotFound) { description = "Data not found." }
            }
            ```

            Error
    """.trimIndent()
}
