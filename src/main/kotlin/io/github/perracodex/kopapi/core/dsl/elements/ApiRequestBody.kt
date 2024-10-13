/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.core.dsl.elements

import io.github.perracodex.kopapi.core.dsl.builders.ApiMetadataBuilder
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
 *
 * @see [ApiMetadataBuilder.requestBody]
 */
@PublishedApi
internal data class ApiRequestBody internal constructor(
    val type: KType,
    val description: String?,
    val required: Boolean,
    val contentType: ContentType,
    val deprecated: Boolean
) {
    init {
        require(type.classifier != Any::class) { "Request body cannot be of type 'Any'. Define an explicit type." }
        require(type.classifier != Unit::class) { "Request body cannot be of type 'Unit'. Define an explicit type." }
        require(type.classifier != Nothing::class) { "Request body cannot be of type 'Nothing'. Define an explicit type." }
    }
}
