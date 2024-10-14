/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.plugin.builders

import io.github.perracodex.kopapi.core.KopapiException
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiServerConfig
import io.github.perracodex.kopapi.utils.Tracer
import io.ktor.http.*

/**
 * Builder constructing server configurations.
 *
 * #### Sample Usage
 * ```
 * servers {
 *      add("https://{environment}.example.com") {
 *          description = "Server with environment variable."
 *          variable("environment", "production") {
 *              choices = setOf("production", "staging", "development")
 *              description = "API environment."
 *          }
 *      }
 * }
 * ```
 *
 * @see [ServerConfigBuilder]
 * @see [ServerVariableBuilder]
 */
public class ServerBuilder {
    private val tracer = Tracer<ServerBuilder>()

    /** The internal set to enforce uniqueness of server configurations. */
    private val internalSet: MutableSet<ApiServerConfig> = LinkedHashSet()

    /**
     * Adds a new server configuration with optional variables.
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
     *         variable("environment", "production") {
     *             choices = setOf("production", "staging", "development")
     *             description = "Specifies the environment (production, etc.)"
     *         }
     *         variable("version", "v1") {
     *             choices = setOf("v1", "v2")
     *             description = "The version of the API."
     *         }
     *     }
     *
     *     add("https://{region}.api.example.com") {
     *         description = "Server for the API by region."
     *         variable("region", "us") {
     *             choices = setOf("us", "eu")
     *             description = "Specifies the region for the API (us, eu)."
     *         }
     *     }
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
        try {
            val url = Url(urlString = urlString.trim())
            val serverConfig: ApiServerConfig = ServerConfigBuilder(url = url).apply(configure).build()
            internalSet.add(serverConfig)
        } catch (e: Exception) {
            tracer.error("Failed to parse server URL: $urlString", e)
            throw KopapiException("Failed to parse server configuration URL: $urlString", e)
        }
    }

    /**
     * Builds and returns the immutable set of [ApiServerConfig].
     *
     * @return A read-only set of server configurations.
     */
    internal fun build(): Set<ApiServerConfig> {
        return internalSet.toSet()
    }

    override fun toString(): String = internalSet.toString()

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
