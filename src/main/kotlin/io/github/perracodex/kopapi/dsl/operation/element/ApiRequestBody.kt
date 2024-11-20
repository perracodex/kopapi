/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.dsl.operation.element

import io.github.perracodex.kopapi.dsl.example.element.IExample
import io.github.perracodex.kopapi.dsl.operation.builder.operation.ApiOperationBuilder
import io.github.perracodex.kopapi.dsl.schema.element.ApiSchemaAttributes
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.type.Composition
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
 * @property examples Examples be used for documentation purposes.
 *
 * @see [ApiOperationBuilder.requestBody]
 */
@PublishedApi
internal data class ApiRequestBody internal constructor(
    val description: String?,
    val required: Boolean,
    val composition: Composition?,
    val content: Map<ContentType, Set<TypeDetails>>?,
    val multipartContent: Map<ContentType, ApiMultipart>?,
    val examples: IExample?
) {
    @PublishedApi
    internal data class TypeDetails(
        val type: KType,
        val schemaAttributes: ApiSchemaAttributes?
    )

    init {
        content?.forEach { (_, typeDetails) ->
            if (typeDetails.isEmpty()) {
                throw KopapiException("At least one Type must be associated with each ContentType.")
            }
            typeDetails.forEach { details ->
                val classifier: KClassifier? = details.type.classifier
                if (classifier == Any::class || classifier == Unit::class || classifier == Nothing::class) {
                    throw KopapiException("RequestBody cannot be of type '${details.type}'. Define an explicit type.")
                }
            }
        }
    }
}
