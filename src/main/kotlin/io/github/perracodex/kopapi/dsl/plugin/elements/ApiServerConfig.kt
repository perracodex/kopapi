/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.plugin.elements

import io.ktor.http.*

/**
 * Represents an immutable server configuration.
 *
 * @property url The [Url] of the server.
 * @property description A human-readable description of the server.
 * @property variables A map of [ApiServerVariable] objects representing the server's variables.
 */
internal data class ApiServerConfig(
    val url: Url,
    val description: String?,
    val variables: Map<String, ApiServerVariable>
)
