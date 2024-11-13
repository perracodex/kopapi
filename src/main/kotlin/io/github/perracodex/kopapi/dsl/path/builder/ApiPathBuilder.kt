/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.path.builder

import io.github.perracodex.kopapi.dsl.marker.KopapiDsl
import io.github.perracodex.kopapi.dsl.parameter.builder.ParametersBuilder
import io.github.perracodex.kopapi.dsl.parameter.delegate.IParameterConfigurable
import io.github.perracodex.kopapi.dsl.parameter.delegate.ParameterDelegate
import io.github.perracodex.kopapi.dsl.path.apiPath
import io.github.perracodex.kopapi.dsl.path.element.ApiPath
import io.github.perracodex.kopapi.dsl.server.delegate.IServerConfigurable
import io.github.perracodex.kopapi.dsl.server.delegate.ServerDelegate
import io.github.perracodex.kopapi.util.string.MultilineString
import io.github.perracodex.kopapi.util.string.SpacedString
import io.github.perracodex.kopapi.util.trimOrNull
import io.ktor.server.routing.*

/**
 * Builder for constructing API path-level metadata.
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
 * #### Information
 * - [summary]: Optional short description of the endpoint's purpose.
 * - [description]: Optional detailed explanation of the endpoint and its functionality.
 * - [servers]: Optional list of server configurations specific to this path.
 *
 * #### Parameters
 * - [ParametersBuilder.pathParameter]: Adds a path parameter applicable to all operations within this path.
 * - [ParametersBuilder.queryParameter]: Adds a query parameter applicable to all operations within this path.
 * - [ParametersBuilder.headerParameter]: Adds a header parameter applicable to all operations within this path.
 * - [ParametersBuilder.cookieParameter]: Adds a cookie parameter applicable to all operations within this path.
 *
 * @see [Route.apiPath]
 */
@KopapiDsl
public class ApiPathBuilder internal constructor(
    private val endpoint: String,
    private val serverDelegate: ServerDelegate = ServerDelegate(),
    private val parameterDelegate: ParameterDelegate = ParameterDelegate(endpoint = endpoint)
) : IServerConfigurable by serverDelegate,
    IParameterConfigurable by parameterDelegate {

    /**
     * Optional short description of the path's purpose.
     *
     * Declaring the `summary` multiple times will concatenate all the summaries
     * delimited by a `space` character between each one.
     *
     * #### Usage
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
     * #### Usage
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
            servers = serverDelegate.build(),
            parameters = parameterDelegate.build()
        )
    }
}
