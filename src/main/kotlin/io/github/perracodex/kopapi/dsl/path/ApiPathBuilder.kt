/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.path

import io.github.perracodex.kopapi.dsl.common.server.ServerBuilder
import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiServerConfig
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.string.SpacedString
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.server.routing.*

/**
 * Builder for constructing API path-level metadata.
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
 *     }
 * }
 * ```
 *
 * @see [Route.apiPath]
 */
@KopapiDsl
public class ApiPathBuilder internal constructor(
    @PublishedApi internal val endpoint: String
) {
    /**
     * Optional short description of the path's purpose.
     *
     * Declaring the `summary` multiple times will concatenate all the summaries
     * delimited by a `space` character between each one.
     *
     * #### Sample Usage
     * ```
     * summary = "Retrieve data items."
     * ```
     *
     * @see [description]
     */
    public var summary: String by SpacedString()

    /**
     * Optional detailed explanation of the path.
     *
     * Declaring the `description` multiple times will concatenate all the descriptions
     * delimited by a `newline` character between each one.
     *
     * #### Sample Usage
     * ```
     * description = "Fetches all items for a group."
     * description = "In addition, it can fetch a specific item."
     * ```
     *
     * @see [summary]
     */
    public var description: String by MultilineString()

    /**
     * List of server configurations specific to this path.
     *
     * Use the [servers] function to configure the servers.
     *
     * @see [servers]
     */
    private val servers: MutableSet<ApiServerConfig> = mutableSetOf()

    /**
     * Sets up servers for the path, with optional support for variables.
     *
     * #### Sample Usage
     * ```
     * servers {
     *      // Simple example with no variables.
     *      add(urlString = "http://localhost:8080") {
     *         description = "Local server for development."
     *      }
     *
     *      // Example with variable placeholders.
     *      add(urlString = "{protocol}://{environment}.example.com:{port}") {
     *          description = "The server with environment variable."
     *
     *          // Environment.
     *          variable(name = "environment", defaultValue = "production") {
     *              choices = setOf("production", "staging", "development")
     *              description = "Specifies the environment (production, etc)"
     *          }
     *
     *          // Port.
     *          variable(name = "port", defaultValue = "8080") {
     *              choices = setOf("8080", "8443")
     *              description = "The port for the server."
     *          }
     *
     *          // Protocol.
     *          variable(name = "protocol", defaultValue = "http") {
     *              choices = setOf("http", "https")
     *          }
     *      }
     * }
     * ```
     *
     * @param configure A lambda receiver for configuring the [ServerBuilder].
     *
     * @see [ServerBuilder]
     */
    public fun servers(configure: ServerBuilder.() -> Unit) {
        val builder: ServerBuilder = ServerBuilder().apply(configure)
        servers.addAll(builder.build())
    }

    /**
     * Builds the [ApiPath] instance with the configured properties.
     *
     * @return The constructed [ApiPath] instance.
     */
    internal fun build(): ApiPath {
        return ApiPath(
            path = endpoint,
            summary = summary.trimOrNull(),
            description = description.trimOrNull(),
            servers = servers.takeIf { it.isNotEmpty() }
        )
    }
}
