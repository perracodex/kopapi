/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.elements

import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.types.ApiType
import io.ktor.http.*
import io.ktor.http.content.*
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

/**
 * Represents the metadata of a multipart request.
 *
 * @property contentType The content type of the multipart request.
 * @property description Optional detailed explanation of the endpoint and its functionality.
 * @property parts The parts of the multipart request.
 */
@PublishedApi
internal data class ApiMultipart(
    val contentType: ContentType,
    val description: String?,
    val parts: List<Part>
) {
    init {
        // Check at runtime if the contentType belongs to the "multipart" category.
        // Using the `contentType.contentType` property for comparison, as all
        // `multipart` content types share the same contentType constant.
        if (contentType.contentType != ContentType.MultiPart.FormData.contentType) {
            throw KopapiException(
                "Invalid content type for multipart. Must be of type: `ContentType.MultiPart`"
            )
        }

    }

    /**
     * Represents the metadata for a part of a multipart request.
     *
     * @property type The [KType] of the part. Expected to be a subclass of [PartData].
     * @property name The name of the part.
     * @property contentType Optional set of [ContentType]s for the part.
     * @property schemaType Optional type of the schema.
     * @property schemaFormat Optional format of the schema type.
     * @property description Optional detailed explanation of the endpoint and its functionality.
     * @property isRequired Indicates whether the part is mandatory.
     */
    data class Part(
        val type: KType,
        val name: String,
        val contentType: Set<ContentType>?,
        val schemaType: ApiType?,
        val schemaFormat: String?,
        val description: String?,
        val isRequired: Boolean
    ) {
        init {
            if (name.isBlank()) {
                throw KopapiException("Name for Multipart-Part cannot be empty.")
            }

            // Ensure the type is a subtype of PartData.
            val partDataType: KType = PartData::class.createType()
            if (!type.isSubtypeOf(other = partDataType)) {
                throw KopapiException("Multipart-Part type must be a subtype of `PartData`.")
            }
        }
    }
}
