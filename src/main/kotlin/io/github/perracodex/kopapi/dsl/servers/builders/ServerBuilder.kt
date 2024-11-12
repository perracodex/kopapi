/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.servers.builders

import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiServerConfig
import io.github.perracodex.kopapi.system.KopapiException

/**
 * Builder constructing server configurations.
 *
 * @see [ServerConfigBuilder]
 * @see [ServerVariableBuilder]
 */
@KopapiDsl
public class ServerBuilder internal constructor() {
    /** Cached server configurations. */
    private val servers: MutableSet<ApiServerConfig> = LinkedHashSet()

    /**
     * Adds a new server configuration with optional variables.
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
     * @receiver [ServerConfigBuilder] The builder used to configure the server.
     *
     * @param urlString The URL of the server. Expected to be a valid URL. If blank, the server is skipped.
     *
     * @see [ServerVariableBuilder]
     */
    public fun add(urlString: String, builder: ServerConfigBuilder.() -> Unit = {}) {
        if (urlString.isBlank()) {
            throw KopapiException("Server URL cannot be blank.")
        }
        val serverConfig: ApiServerConfig = ServerConfigBuilder(urlString = urlString).apply(builder).build()
        servers.add(serverConfig)
    }

    /**
     * Builds and returns the immutable set of [ApiServerConfig].
     *
     * @return A read-only set of server configurations.
     */
    internal fun build(): Set<ApiServerConfig> {
        return servers.toSet()
    }

    /** Provides a string representation of the server configurations. */
    override fun toString(): String = servers.toString()

    internal companion object {
        /** The default server URL to be used if no servers are added. */
        private const val DEFAULT_SERVER = "http://localhost:8080"

        /**
         * Returns a default server configuration.
         * Useful for when no servers are added through the DSL.
         */
        internal fun defaultServer(): ApiServerConfig {
            return ApiServerConfig(
                url = DEFAULT_SERVER,
                description = "Default Server", emptyMap()
            )
        }
    }
}
