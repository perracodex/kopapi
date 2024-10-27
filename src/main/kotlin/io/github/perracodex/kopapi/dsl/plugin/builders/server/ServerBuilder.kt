/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.plugin.builders.server

import io.github.perracodex.kopapi.dsl.markers.ConfigurationDsl
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiServerConfig
import io.github.perracodex.kopapi.system.Tracer
import io.ktor.utils.io.*

/**
 * Builder constructing server configurations.
 *
 * @see [ServerConfigBuilder]
 * @see [ServerVariableBuilder]
 */
@KtorDsl
@ConfigurationDsl
public class ServerBuilder {
    private val tracer = Tracer<ServerBuilder>()

    /** The internal set to enforce uniqueness of server configurations. */
    private val serverConfigs: MutableSet<ApiServerConfig> = LinkedHashSet()

    /**
     * Adds a new server configuration with optional variables.
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
     * @param urlString The URL of the server. Expected to be a valid URL. If blank, the server is skipped.
     * @param configure The configuration block for the server.
     *
     * @see [ServerConfigBuilder]
     * @see [ServerVariableBuilder]
     */
    public fun add(urlString: String, configure: ServerConfigBuilder.() -> Unit = {}) {
        if (urlString.isBlank()) {
            tracer.warning("Provided server URL is blank. Skipping server configuration.")
            return
        }
        val serverConfig: ApiServerConfig = ServerConfigBuilder(urlString = urlString).apply(configure).build()
        serverConfigs.add(serverConfig)
    }

    /**
     * Builds and returns the immutable set of [ApiServerConfig].
     *
     * @return A read-only set of server configurations.
     */
    internal fun build(): Set<ApiServerConfig> {
        return serverConfigs.toSet()
    }

    /** Clears the internal set of server configurations. */
    override fun toString(): String = serverConfigs.toString()

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
