/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.elements

/**
 * Represents a possible design-time link for a response.
 *
 * @property operationId The name of an existing, resolvable OAS operation.
 * @property description A human-readable description of the link.
 * @property parameters A set of parameters to pass to the linked operation.
 *
 * @see [ApiResponse]
 * @see [ApiLinkParameter]
 */
internal data class ApiLink(
    val operationId: String,
    val description: String?,
    val parameters: Set<ApiLinkParameter>?
)
