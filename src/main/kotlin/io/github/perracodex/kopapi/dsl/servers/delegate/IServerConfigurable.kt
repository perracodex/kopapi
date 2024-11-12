/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.servers.delegate

import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.servers.builders.ServerBuilder

/**
 * Handles the registration of servers.
 */
@KopapiDsl
public interface IServerConfigurable {
    /**
     * Sets up servers, with optional support for variables.
     *
     * #### Usage
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
     * @receiver [ServerBuilder] The builder used to configure the server.
     */
    public fun servers(builder: ServerBuilder.() -> Unit)
}
