/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.path

import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.utils.extractRoutePath
import io.ktor.server.routing.*

/**
 * Defines API Path metadata for a Ktor route.
 * Intended for use with route paths without an HTTP method tied to them.
 *
 * For HTTP methods, use the [Route.api] extension function instead.
 *
 * #### Sample Usage
 * - Define for a `routing` block:
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
 * - Define for a `route` block:
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
 * @param configure A lambda receiver for configuring the [ApiPathBuilder].
 * @return The current [Route] instance
 * @throws KopapiException If the route does not have an HTTP method selector.
 *
 * @see [ApiPathBuilder]
 */
public infix fun Route.apiPath(configure: ApiPathBuilder.() -> Unit): Route {
    if (this !is RoutingNode || this.selector is HttpMethodRouteSelector) {
        throw KopapiException(message = buildErrorMessage(route = this))
    }

    val endpointPath: String = this.extractRoutePath()

    // Apply the configuration within the ApiPathBuilder's scope.
    val builder = ApiPathBuilder(endpoint = endpointPath)
    with(builder) { configure() }

    // Build the path using the provided configuration.
    val apiPath: ApiPath = builder.build()

    // Register the path with the schema registry
    // for later use in generating OpenAPI documentation.
    SchemaRegistry.registerApiPath(apiPath)

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
