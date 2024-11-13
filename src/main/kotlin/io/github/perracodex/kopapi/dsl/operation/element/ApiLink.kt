/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.element

import io.github.perracodex.kopapi.dsl.plugin.element.ApiServerConfig
import java.util.*

/**
 * Represents a possible design-time link for a response.
 *
 * @property operationId The name of an existing, resolvable OAS operation.
 * @property operationRef A relative or absolute URI reference to an OAS operation.
 * @property description A human-readable description of the link.
 * @property parameters A map of parameters to pass to the linked operation.
 * @property requestBody A single expression or literal value to be used as the request body when calling the target operation.
 * @property server A server object to be used by the target operation.
 *
 * @see [ApiResponse]
 */
internal data class ApiLink(
    val operationId: String?,
    val operationRef: String?,
    val description: String?,
    val parameters: SortedMap<String, String>?,
    val requestBody: String?,
    val server: ApiServerConfig?
)
