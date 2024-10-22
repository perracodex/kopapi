/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.request

import io.github.perracodex.kopapi.dsl.markers.OperationDsl
import io.github.perracodex.kopapi.schema.MultipartSchema
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.types.ApiFormat
import io.github.perracodex.kopapi.types.ApiType
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.http.*
import io.ktor.http.content.*
import kotlin.collections.set
import kotlin.reflect.KClass


/**
 * Builder for constructing multipart request bodies.
 *
 * Allows defining individual parts with names and types, along with their descriptions.
 *
 * @property description An optional description of the multipart request.
 */
@OperationDsl
public class MultipartBuilder {
    /** Holds the parts of the multipart request. */
    @PublishedApi
    internal val parts: MutableMap<String, MultipartSchema> = mutableMapOf()

    public var description: String by MultilineString()

    /**
     * Adds a part to the multipart request body.
     *
     * #### Sample Usage
     * ```
     * // Default ContentType.MultiPart.FormData
     * multipart {
     *      part<PartData.FileItem>("myFilePart") {
     *          description = "The file to upload."
     *      }
     * }
     *
     * // Specify the part type explicitly.
     * multipart(contentType = ContentType.MultiPart.Signed) {
     *     part<PartData.FormItem>("myFormPart") {
     *     description = "The form data."
     * }
     * ```
     *
     * @param T The type of the part, typically a subclass of [PartData].
     * @param name The name of the part.
     * @param contentType Optional content type for the part.
     * @param configure A lambda receiver for configuring the part's metadata.
     */
    public inline fun <reified T : PartData> part(
        name: String,
        contentType: ContentType? = null,
        configure: PartBuilder.() -> Unit = {}
    ) {
        val partBuilder: PartBuilder = PartBuilder(name = name).apply(configure)
        val resolvedContentType: ContentType = contentType ?: determineContentType(kClass = T::class)
        val (schemaType: ApiType, schemaFormat: ApiFormat?) = determineSchemaAttributes(
            kClass = T::class,
            partBuilder = partBuilder
        )

        val schema: MultipartSchema = createMultipartSchema(
            partDataClass = T::class,
            name = name,
            isRequired = partBuilder.required,
            description = partBuilder.description.trimOrNull(),
            contentType = resolvedContentType,
            schemaType = schemaType,
            schemaFormat = schemaFormat
        )

        parts[name] = schema
    }

    /**
     * Determines the default content type based on the PartData subclass.
     */
    @PublishedApi
    internal fun <T : PartData> determineContentType(kClass: KClass<T>): ContentType = when (kClass) {
        PartData.FormItem::class -> ContentType.Text.Plain
        PartData.FileItem::class,
        PartData.BinaryItem::class,
        PartData.BinaryChannelItem::class -> ContentType.Application.OctetStream

        else -> throw KopapiException("Unsupported PartData type: $kClass")
    }

    /**
     * Determines the default schema type and format based on the PartData subclass.
     */
    @PublishedApi
    internal fun <T : PartData> determineSchemaAttributes(
        kClass: KClass<T>,
        partBuilder: PartBuilder
    ): Pair<ApiType, ApiFormat?> = when (kClass) {
        PartData.FormItem::class -> Pair(
            partBuilder.schemaType ?: ApiType.STRING,
            partBuilder.schemaFormat
        )

        PartData.FileItem::class,
        PartData.BinaryItem::class,
        PartData.BinaryChannelItem::class -> Pair(
            partBuilder.schemaType ?: ApiType.STRING,
            partBuilder.schemaFormat ?: ApiFormat.BINARY
        )

        else -> throw KopapiException("Unsupported PartData type: $kClass")
    }

    /**
     * Creates the appropriate MultipartSchema based on the PartData type.
     */
    @PublishedApi
    internal fun createMultipartSchema(
        partDataClass: KClass<out PartData>,
        name: String,
        isRequired: Boolean,
        description: String?,
        contentType: ContentType,
        schemaType: ApiType,
        schemaFormat: ApiFormat?
    ): MultipartSchema {
        return when (partDataClass) {
            PartData.FormItem::class -> MultipartSchema.FormItem(
                name = name,
                isRequired = isRequired,
                description = description,
                contentType = contentType,
                schemaType = schemaType,
                schemaFormat = schemaFormat
            )

            PartData.FileItem::class -> MultipartSchema.FileItem(
                name = name,
                isRequired = isRequired,
                description = description,
                contentType = contentType,
                schemaType = schemaType,
                schemaFormat = schemaFormat
            )

            PartData.BinaryItem::class -> MultipartSchema.BinaryItem(
                name = name,
                isRequired = isRequired,
                description = description,
                contentType = contentType,
                schemaType = schemaType,
                schemaFormat = schemaFormat
            )

            PartData.BinaryChannelItem::class -> MultipartSchema.BinaryChannelItem(
                name = name,
                isRequired = isRequired,
                description = description,
                contentType = contentType,
                schemaType = schemaType,
                schemaFormat = schemaFormat
            )

            else -> throw KopapiException("Unsupported PartData type: $partDataClass")
        }
    }

    /**
     * Builds the multipart schema object.
     */
    internal fun build(): MultipartSchema.Object {
        val requiredFields: List<String> = parts.filterValues { it.isRequired }.keys.toList()

        return MultipartSchema.Object(
            description = description.trimOrNull(),
            properties = parts.toMutableMap(),
            requiredFields = requiredFields.takeIf { it.isNotEmpty() },
        )
    }
}
