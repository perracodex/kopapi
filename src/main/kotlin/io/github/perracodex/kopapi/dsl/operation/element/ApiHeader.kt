/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.element

import io.github.perracodex.kopapi.dsl.schema.element.ApiSchemaAttributes
import io.github.perracodex.kopapi.system.KopapiException
import io.ktor.http.*
import kotlin.reflect.KClassifier
import kotlin.reflect.KType

/**
 * Represents the metadata of a response header.
 *
 * @property type The type of the header.
 * @property description A human-readable description of the header.
 * @property required Indicates whether the header is mandatory.
 * @property explode Indicates whether arrays and objects are serialized as a single comma-separated header.
 * @property contentType Optional [ContentType] when a specific media format is required.
 * @property deprecated Indicates whether the header is deprecated and should be avoided.
 * @property schemaAttributes Optional schema attributes for the header type. Not applicable for complex types.
 *
 * @see [ApiResponse]
 */
@PublishedApi
internal data class ApiHeader(
    val type: KType,
    val description: String?,
    val required: Boolean,
    val explode: Boolean?,
    val contentType: ContentType?,
    val deprecated: Boolean?,
    val schemaAttributes: ApiSchemaAttributes?
) {
    init {
        // Ensure non-supported types are not used.
        val classifier: KClassifier? = type.classifier
        if (classifier == Any::class || classifier == Unit::class || classifier == Nothing::class) {
            throw KopapiException("Header cannot be of type '${type.classifier}'. Define an explicit type.")
        }
    }
}
