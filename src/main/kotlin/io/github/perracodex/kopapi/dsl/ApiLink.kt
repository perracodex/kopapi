/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * Represents a possible design-time link for a response.
 *
 * @property operationId The name of an existing, resolvable OAS operation.
 * @property parameters A map representing parameters to pass to the linked operation.
 * @property description A human-readable description of the link.
 */
@Serializable
public data class ApiLink(
    val operationId: String,
    val parameters: Map<String, @Contextual Any?> = emptyMap(),
    val description: String? = null
)
