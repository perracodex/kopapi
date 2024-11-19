/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.path

import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.path.builder.ApiPathBuilder
import io.github.perracodex.kopapi.dsl.path.element.ApiPath
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.util.extractRoutePath
import io.ktor.server.routing.*

/**
 * Defines API Path metadata for a Ktor route.
 * Intended for use with route paths without an HTTP method tied to them.
 *
 * Useful to define shared metadata to multiple routes, such as servers and parameters
 * which are reused across multiple routes.
 *
 * **Note:** For HTTP methods, use the [Route.api] extension function instead.
 *
 * #### Usage
 * - Definition for a `routing` block:
 * ```
 * routing {
 *     // Implement routes as usual
 * } apiPath {
 *     summary = "Some summary"
 *     description = "Some description"
 *     servers {
 *         add(urlString = "https://api.example.com") {
 *             description = "Some server description"
 *         }
 *     }
 *     pathParameter<Uuid>(name = "id") {
 *         description = "The unique identifier of the item."
 *    }
 * }
 * ```
 * - Definition for a `route` block:
 * ```
 * routing {
 *     route("some-endpoint") {
 *         // Implement operations as usual (e.g., get, put, post, etc)
 *     } apiPath {
 *         summary = "Some summary"
 *         description = "Some description"
 *         servers {
 *             add(urlString = "https://api.example.com") {
 *                 description = "Some server description"
 *             }
 *         }
 *         pathParameter<Uuid>(name = "id") {
 *             description = "The unique identifier of the item."
 *         }
 *     }
 * }
 * ```
 *
 * @receiver [ApiPathBuilder] The builder used to configure the API path metadata.
 *
 * @return The current [Route] instance
 * @throws KopapiException If the route does not have an HTTP method selector.
 */
public infix fun Route.apiPath(builder: ApiPathBuilder.() -> Unit): Route {
    if (this !is RoutingNode || this.selector is HttpMethodRouteSelector) {
        throw KopapiException(message = buildErrorMessage(route = this))
    }

    val endpointPath: String = this.extractRoutePath()

    // Apply the configuration within the ApiPathBuilder's scope.
    val pathBuilder = ApiPathBuilder(endpoint = endpointPath)
    with(pathBuilder) { builder() }

    // Build the path using the provided configuration.
    val apiPath: ApiPath = pathBuilder.build()

    // Register the path with the schema registry
    // for later use in generating OpenAPI documentation.
    SchemaRegistry.registerApiPath(path = apiPath)

    return this
}

/**
 * Builds an error message for when the [Route.apiPath] extension function
 * is applied to a route with an HTTP method.
 *
 * @param route The [Route] that incorrectly has an HTTP method.
 * @return A formatted error message string.
 */
private fun buildErrorMessage(route: Route): String {
    return """
        |Error: The 'apiPath' extension function must be attached to a route or routing block that does not have an HTTP method.
        |The current route "${route.extractRoutePath()}" has an HTTP method associated with it.
        |Possible causes:
        |   - You might have applied 'apiPath' to a route that is directly tied to a specific HTTP method.
        |To resolve:
        |   - Make sure 'apiPath' is applied to a route or routing block without an HTTP method:
        """.trimMargin()
}
