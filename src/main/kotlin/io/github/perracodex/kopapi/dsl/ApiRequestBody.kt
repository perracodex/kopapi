/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl

import io.ktor.http.*
import kotlin.reflect.KType

/**
 * Represents the metadata for the request body of an API endpoint.
 *
 * @property type The [KType] of the parameter, specifying the Kotlin type.
 * @property description A human-readable description of the parameter.
 * @property required Indicates whether the request body is mandatory.
 * @property contentType The [ContentType] specifying how the data is represented (e.g., application/json).
 * @property deprecated Indicates whether the request body is deprecated and should be avoided.
 */
@ConsistentCopyVisibility
public data class ApiRequestBody @PublishedApi internal constructor(
    val type: KType,
    val description: String? = null,
    val required: Boolean = true,
    val contentType: ContentType = ContentType.Application.Json,
    val deprecated: Boolean = false
)
