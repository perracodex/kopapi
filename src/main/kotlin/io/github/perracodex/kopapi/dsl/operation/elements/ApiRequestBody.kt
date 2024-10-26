/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.elements

import io.github.perracodex.kopapi.dsl.operation.builders.operation.ApiOperationBuilder
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.types.Composition
import io.github.perracodex.kopapi.utils.safeName
import io.ktor.http.*
import kotlin.reflect.KClassifier
import kotlin.reflect.KType

/**
 * Represents the metadata for the request body of an API endpoint.
 *
 * @property description A human-readable description of the parameter.
 * @property composition The composition of the response. Only meaningful if multiple types are provided.
 * @property required Indicates whether the request body is mandatory.
 * @property content A map of [ContentType] to a set of [KType] that this request requires.
 * @property multipartContent A list of [ApiMultipart] for multipart requests.
 *
 * @see [ApiOperationBuilder.requestBody]
 */
@PublishedApi
internal data class ApiRequestBody internal constructor(
    val description: String?,
    val required: Boolean,
    val composition: Composition?,
    val content: Map<ContentType, Set<KType>>?,
    val multipartContent: Map<ContentType, ApiMultipart>?,
) {
    init {
        content?.forEach { (_, types) ->
            if (types.isEmpty()) {
                throw KopapiException("At least one Type must be associated with each ContentType.")
            }
            types.forEach { type ->
                val classifier: KClassifier? = type.classifier
                if (classifier == Any::class || classifier == Unit::class || classifier == Nothing::class) {
                    throw KopapiException("RequestBody cannot be of type '${type.safeName()}'. Define an explicit type.")
                }
            }
        }
    }
}
