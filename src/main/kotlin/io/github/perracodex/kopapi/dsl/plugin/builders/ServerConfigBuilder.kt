/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.plugin.builders

import io.github.perracodex.kopapi.dsl.plugin.elements.ApiServerConfig
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiServerVariable
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.http.*

/**
 * Builder for configuring a server.
 *
 * #### Sample Usage
 * ```
 * servers {
 *     add("http://localhost:8080") {
 *         description = "Local server for development."
 *     }
 *
 *     add("https://{environment}.example.com") {
 *         description = "The server for the API with environment variable."
 *         variable("environment") {
 *             description = "Specifies the environment (production, etc.)"
 *             defaultValue = "production"
 *             choices = setOf("production", "staging", "development")
 *         }
 *         variable("version") {
 *             description = "The version of the API"
 *             defaultValue = "v1"
 *             choices = setOf("v1", "v2")
 *         }
 *     }
 *
 *     add("https://{region}.api.example.com") {
 *         description = "Server for the API by region"
 *         variable("region") {
 *             description = "Specifies the region for the API (us, eu)."
 *             defaultValue = "us"
 *             choices = setOf("us", "eu")
 *         }
 *     }
 * }
 * ```
 * Multiple descriptions can be defined to construct a final multiline description.
 *
 * @property url The URL of the server.
 * @property description A description of the server.
 *
 * @see [ServerVariableBuilder]
 * @see [ServerBuilder]
 */
public class ServerConfigBuilder(
    private val url: Url
) {
    public var description: String by MultilineString()
    private val variables: MutableMap<String, ApiServerVariable> = mutableMapOf()

    /**
     * Adds a server variable.
     *
     * ### Sample Usage
     * ```
     * variable("environment") {
     *      description = "Specifies the environment (production, etc.)"
     *      defaultValue = "production"
     *      choices = setOf("production", "staging", "development")
     * }
     * ```
     * Multiple descriptions can be defined to construct a final multiline description.
     *
     * @param name The name of the variable.
     * @param configure The configuration block for the variable.
     *
     * @see [ServerVariableBuilder]
     * @see [ServerBuilder]
     * @see [ServerConfigBuilder]
     */
    public fun variable(name: String, configure: ServerVariableBuilder.() -> Unit) {
        require(name.isNotBlank()) { "Server variable name cannot be blank." }
        variables[name.trim()] = ServerVariableBuilder().apply(configure).build()
    }

    /**
     * Builds the final immutable [ApiServerConfig].
     */
    internal fun build(): ApiServerConfig = ApiServerConfig(
        url = url,
        description = description.trimOrNull(),
        variables = variables.toMap()
    )
}
