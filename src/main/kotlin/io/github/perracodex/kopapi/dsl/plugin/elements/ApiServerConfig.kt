/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.plugin.elements

/**
 * Represents an immutable server configuration.
 *
 * #### IMPORTANT
 * The naming, order and nullability of properties are defined
 * as per the OpenAPI specification, so no transformations are
 * needed generating the OpenAPI schema.
 *
 * @property url The Url of the server.
 * @property description A human-readable description of the server.
 * @property variables Optional map of [ApiServerVariable] objects representing the server's variables.
 */
internal data class ApiServerConfig(
    val url: String,
    val description: String?,
    val variables: Map<String, ApiServerVariable>?
)
