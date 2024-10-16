/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.elements

import io.github.perracodex.kopapi.dsl.operation.builders.ApiOperationBuilder
import io.github.perracodex.kopapi.system.KopapiException
import io.ktor.http.*
import kotlin.reflect.KClassifier
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
 * @see [ApiOperationBuilder.requestBody]
 */
@PublishedApi
internal data class ApiRequestBody internal constructor(
    val type: KType,
    val description: String?,
    val required: Boolean,
    val contentType: ContentType,
    val deprecated: Boolean?
) {
    init {
        val classifier: KClassifier? = type.classifier
        if (classifier == Any::class || classifier == Unit::class || classifier == Nothing::class) {
            throw KopapiException("RequestBody cannot be of type '${type.classifier}'. Define an explicit type.")
        }
    }
}
