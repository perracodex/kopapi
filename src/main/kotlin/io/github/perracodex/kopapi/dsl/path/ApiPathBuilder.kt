/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.path

import io.github.perracodex.kopapi.dsl.common.parameter.configurable.ParameterConfigurable
import io.github.perracodex.kopapi.dsl.common.server.configurable.IServerConfigurable
import io.github.perracodex.kopapi.dsl.common.server.configurable.ServerConfigurable
import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.string.SpacedString
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.server.routing.*

/**
 * Builder for constructing API path-level metadata.
 *
 * #### Information
 * - [summary]: Optional short description of the endpoint's purpose.
 * - [description]: Optional detailed explanation of the endpoint and its functionality.
 * - [servers]: Optional list of server configurations specific to this path.
 *
 * #### Parameters
 * - [pathParameter]: Adds a path parameter applicable to all operations within this path.
 * - [queryParameter]: Adds a query parameter applicable to all operations within this path.
 * - [headerParameter]: Adds a header parameter applicable to all operations within this path.
 * - [cookieParameter]: Adds a cookie parameter applicable to all operations within this path.
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
 * @see [Route.apiPath]
 */
@KopapiDsl
public class ApiPathBuilder internal constructor(
    private val endpoint: String,
    private val serverConfigurable: ServerConfigurable = ServerConfigurable()
) : IServerConfigurable by serverConfigurable,
    ParameterConfigurable(endpoint = endpoint) {

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
     * Builds the [ApiPath] instance with the configured properties.
     *
     * @return The constructed [ApiPath] instance.
     */
    internal fun build(): ApiPath {
        return ApiPath(
            path = endpoint,
            summary = summary.trimOrNull(),
            description = description.trimOrNull(),
            servers = serverConfigurable.servers.takeIf { it.isNotEmpty() },
            parameters = _parametersConfig.parameters.takeIf { it.isNotEmpty() }
        )
    }
}
