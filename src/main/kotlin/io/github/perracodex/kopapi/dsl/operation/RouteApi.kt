/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.dsl.operation

import io.github.perracodex.kopapi.dsl.operation.builder.operation.ApiOperationBuilder
import io.github.perracodex.kopapi.dsl.operation.element.ApiOperation
import io.github.perracodex.kopapi.dsl.path.apiPath
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.util.RoutePathDetails
import io.github.perracodex.kopapi.util.extractRoutePath
import io.ktor.http.*
import io.ktor.server.routing.*

/**
 * Defines API Operation metadata for a Ktor route.
 * Intended for use with terminal route handlers that define an HTTP method.
 *
 * For route paths without an HTTP method tied to them, use the [Route.apiPath] extension function instead.
 *
 * #### Usage
 * ```
 * get("/items/{data_id}") {
 *     // Implement as usual
 * } api {
 *     tags = setOf("Items", "Data")
 *     summary = "Retrieve data items."
 *     description = "Fetches all items for a data set."
 *     operationId = "getDataItems"
 *     pathParameter<Uuid>("data_id") { description = "The data Id." }
 *     queryParameter<String>("item_id") { description = "Optional item Id." }
 *     response<List<Item>>(HttpStatusCode.OK) { description = "Successful." }
 *     response(HttpStatusCode.NotFound) { description = "Data not found." }
 *     bearerSecurity(name = "Authentication") {
 *          description = "Access to data."
 *     }
 * }
 * ```
 *
 * @receiver [ApiOperationBuilder] The builder used to configure the API operation metadata.
 *
 * @return The current [Route] instance
 * @throws KopapiException If the route does not have an HTTP method selector.
 */
public infix fun Route.api(builder: ApiOperationBuilder.() -> Unit): Route {
    if (this !is RoutingNode) {
        throw KopapiException(message = buildErrorMessage(route = this))
    }

    // Resolve the HTTP method of the route: GET, POST, PUT, DELETE, etc.
    val method: HttpMethod = (this.selector as? HttpMethodRouteSelector)?.method
        ?: throw KopapiException(message = buildErrorMessage(route = this))

    val rotePathDetails: RoutePathDetails = this.extractRoutePath()

    // Apply the configuration within the ApiOperationBuilder's scope.
    val operationBuilder = ApiOperationBuilder(endpoint = "[$method] ${rotePathDetails.path}")
    with(operationBuilder) { builder() }

    // Build the operation using the provided configuration.
    val apiOperation: ApiOperation = operationBuilder.build(
        method = method,
        endpointPath = rotePathDetails.path,
        errors = rotePathDetails.errors
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
private fun buildErrorMessage(route: Route): String {
    return """
        |Error: The 'api' extension function must be attached to a route that has an HTTP method (e.g., GET, POST, PUT, DELETE).
        |The current route "${route.extractRoutePath()}" does not have an HTTP method associated with it.
        |Possible causes:
        |   - You might have applied 'api' to a route that is not directly tied to a specific HTTP method,
        |To resolve:
        |   - Make sure 'api' is applied to a route with an HTTP method.
        """.trimMargin()
}
