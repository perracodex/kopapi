/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.utils

import io.github.perracodex.kopapi.system.Tracer
import io.ktor.server.application.*

/**
 * Utility class for network-related operations.
 */
internal object NetworkUtils {
    private val tracer = Tracer<NetworkUtils>()

    /**
     * Attempts to resolve the server URL based on the current environment configuration.
     *
     * @param environment The application environment.
     * @return The server URL, or an empty string if it could not be resolved.
     */
    fun getServerUrl(environment: ApplicationEnvironment): String {
        return runCatching {
            // Extract host and port from environment configurations.
            val host: String = environment.config.propertyOrNull(path = "ktor.deployment.host")
                ?.getString() ?: "0.0.0.0"
            val port: Int = environment.config.propertyOrNull(path = "ktor.deployment.port")
                ?.getString()?.toIntOrNull() ?: 8080

            // Check for SSL configuration to determine the protocol.
            val sslEnabled: Boolean = environment.config.propertyOrNull(path = "ktor.deployment.ssl")
                ?.getString()?.toBoolean() ?: false

            // Determine the protocol.
            val protocol: String = if (sslEnabled) "https" else "http"

            // Include the port only if it's not the default for the protocol.
            val portPart: String = when {
                (protocol == "http" && port == 80) || (protocol == "https" && port == 443) -> ""
                else -> ":$port"
            }

            "$protocol://$host$portPart"
        }.onFailure { error ->
            tracer.error(message = "Failed to get the server URL.", cause = error)
        }.getOrElse { "" }
    }
}
