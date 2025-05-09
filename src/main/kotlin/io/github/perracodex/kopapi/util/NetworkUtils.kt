/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.util

import io.github.perracodex.kopapi.system.Tracer
import io.ktor.server.application.*

/**
 * Utility class for network-related operations.
 */
internal object NetworkUtils {
    private val tracer: Tracer = Tracer<NetworkUtils>()

    private const val DEFAULT_HOST: String = "localhost"
    private const val DEFAULT_HTTP_PORT: Int = 80
    private const val DEFAULT_HTTPS_PORT: Int = 443
    private const val CUSTOM_DEFAULT_PORT: Int = 8080
    private const val HTTP_PROTOCOL: String = "http"
    private const val HTTPS_PROTOCOL: String = "https"

    /**
     * Attempts to resolve the server URL based on the current environment configuration.
     *
     * @param environment The application environment.
     * @return The server URL, or an empty string if it could not be resolved.
     */
    fun getServerUrl(environment: ApplicationEnvironment): String {
        return runCatching {
            // Extract host from environment configurations or use the default host.
            val host: String = environment.config.propertyOrNull("ktor.deployment.host")
                ?.getString()
                ?.let { if (it == "0.0.0.0" || it == "::" || it.isBlank()) DEFAULT_HOST else it }
                ?: DEFAULT_HOST

            // Extract port from environment configurations or use the custom default port.
            val port: Int = environment.config.propertyOrNull("ktor.deployment.port")
                ?.getString()?.toIntOrNull() ?: CUSTOM_DEFAULT_PORT

            // Determine if SSL is enabled based on environment configurations.
            val sslEnabled: Boolean = environment.config.propertyOrNull("ktor.deployment.ssl")
                ?.getString().toBoolean()

            // Select the appropriate protocol and default port based on SSL configuration.
            val (protocol, defaultPort) = if (sslEnabled) {
                HTTPS_PROTOCOL to DEFAULT_HTTPS_PORT
            } else {
                HTTP_PROTOCOL to DEFAULT_HTTP_PORT
            }

            // Determine whether to include the port in the URL.
            val portPart: String = if (port != defaultPort) ":$port" else ""

            // Construct and return the server URL.
            "$protocol://$host$portPart"
        }.onFailure { error ->
            tracer.error(message = "Failed to get the server URL.", cause = error)
        }.getOrElse { "" }
    }

    /**
     * Normalizes the URL by ensuring it starts with a `/`.
     *
     * @param url The URL to normalize.
     * @param defaultValue The default value to use if the URL is empty.
     */
    fun normalizeUrl(url: String?, defaultValue: String): String {
        val cleanUrl: String = url.trimOrDefault(defaultValue = defaultValue)
        return when (cleanUrl.startsWith(prefix = "/")) {
            true -> cleanUrl
            false -> "/$cleanUrl"
        }
    }
}
