/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.elements

import io.ktor.http.*
import kotlin.reflect.KType

/**
 * Represents the metadata of a response header.
 *
 * @property type The type of the header.
 * @property description A human-readable description of the header.
 * @property required Indicates whether the header is mandatory.
 * @property explode Indicates whether arrays and objects are serialized as a single comma-separated header.
 * @property contentType Optional [ContentType] when a specific media format is required.
 * @property pattern Optional regular expression pattern that the header value must match. Meaningful only for string headers.
 * @property deprecated Indicates whether the header is deprecated and should be avoided.
 *
 * @see [ApiResponse]
 */
internal data class ApiHeader(
    val type: KType,
    val description: String?,
    val required: Boolean,
    val explode: Boolean?,
    val contentType: ContentType?,
    val pattern: String?,
    val deprecated: Boolean?
)
