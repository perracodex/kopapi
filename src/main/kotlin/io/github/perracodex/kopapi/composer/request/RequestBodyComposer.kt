/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.request

import io.github.perracodex.kopapi.composer.OpenAPiSchema
import io.github.perracodex.kopapi.composer.SchemaRegistry
import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.github.perracodex.kopapi.dsl.operation.elements.ApiMultipart
import io.github.perracodex.kopapi.dsl.operation.elements.ApiRequestBody
import io.github.perracodex.kopapi.schema.ElementSchema
import io.github.perracodex.kopapi.schema.ISchema
import io.github.perracodex.kopapi.schema.MultipartSchema
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.types.ApiFormat
import io.github.perracodex.kopapi.types.ApiType
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.http.*
import io.ktor.http.content.*
import kotlin.reflect.KClassifier
import kotlin.reflect.KType

/**
 * Responsible for composing the `request body` section of the OpenAPI schema.
 *
 * This includes handling both standard request bodies and multipart/form-data bodies,
 * which may have compositions like `ANY_OF`, `ALL_OF`, and `ONE_OF`.
 */
@ComposerAPI
internal object RequestBodyComposer {
    /**
     * Generates the `request body` section of the OpenAPI schema.
     *
     * This method processes both standard request bodies and multipart request bodies.
     *
     * @param requestBody The [ApiRequestBody] object containing the request body metadata.
     * @return The constructed [PathRequestBody] object representing the OpenAPI request body.
     */
    fun compose(requestBody: ApiRequestBody): PathRequestBody {
        val schemasByContentType: MutableMap<ContentType, MutableList<ElementSchema>> = mutableMapOf()

        // Handling standard content types (non-multipart).
        requestBody.content?.forEach { (contentType, types) ->
            types.forEach { type ->
                val schema: ElementSchema = SchemaRegistry.inspectType(type = type)?.schema
                    ?: throw KopapiException("No schema found for type: $type")
                schemasByContentType.getOrPut(contentType) { mutableListOf() }.add(schema)
            }
        }

        // Collected schemas for multipart content types.
        val multipartSchema: Map<ContentType, OpenAPiSchema.ContentSchema>? = requestBody
            .multipartContent?.let { parts ->
                parts.mapValues { (_, apiMultipart) ->
                    val multipartSchema: MultipartSchema.Object = composeMultipart(apiMultipart = apiMultipart)
                    OpenAPiSchema.ContentSchema(schema = multipartSchema)
                }
            }

        // Combining schemas for standard content types.
        val standardSchema: Map<ContentType, OpenAPiSchema.ContentSchema> = schemasByContentType
            .mapValues { (_, schemas) ->
                ISchema.determineSchema(composition = requestBody.composition, schemas = schemas)
            }

        // Building the final request body object by combining standard and multipart content.
        val content: Map<ContentType, OpenAPiSchema.ContentSchema> = multipartSchema?.let {
            standardSchema + it
        } ?: standardSchema

        return PathRequestBody(
            description = requestBody.description,
            required = requestBody.required,
            content = content
        )
    }

    /**
     * Composes a multipart schema from an [ApiMultipart] instance.
     *
     * Transforms [ApiMultipart] into [MultipartSchema.Object], where each part is converted to its
     * corresponding [MultipartSchema] subclass and added to the `properties` map.
     *
     * @param apiMultipart The [ApiMultipart] instance containing multipart metadata.
     * @return A [MultipartSchema.Object] representing the multipart schema.
     */
    private fun composeMultipart(apiMultipart: ApiMultipart): MultipartSchema.Object {
        val properties: MutableMap<String, MultipartSchema> = mutableMapOf()
        val requiredFields: MutableList<String> = mutableListOf()

        apiMultipart.parts.forEach { part ->
            val kType: KType = part.type
            val kClassifier: KClassifier = kType.classifier
                ?: run {
                    throw KopapiException("KType classifier is null: $kType")
                }

            val multipartSchema: MultipartSchema = createMultipartSchema(
                kClassifier = kClassifier,
                name = part.name,
                isRequired = part.isRequired,
                description = part.description.trimOrNull(),
                contentType = part.contentType ?: determineContentType(kClassifier = kClassifier),
                schemaType = part.schemaType ?: determineDefaultSchemaType(kClassifier = kClassifier),
                schemaFormat = part.schemaFormat ?: determineDefaultSchemaFormat(kClassifier = kClassifier)
            )

            properties[part.name] = multipartSchema
            if (part.isRequired) {
                requiredFields.add(part.name)
            }
        }

        return MultipartSchema.Object(
            description = apiMultipart.description.trimOrNull(),
            properties = properties,
            requiredFields = requiredFields.ifEmpty { null }
        )
    }

    /**
     * Determines the default content type based on the PartData subclass.
     */
    private fun determineContentType(kClassifier: KClassifier): ContentType = when (kClassifier) {
        PartData.FormItem::class -> ContentType.Text.Plain
        PartData.FileItem::class,
        PartData.BinaryItem::class,
        PartData.BinaryChannelItem::class -> ContentType.Application.OctetStream

        else -> throw KopapiException("Unsupported PartData type: $kClassifier")
    }

    /**
     * Determines the default schema type based on the PartData subclass.
     */
    private fun determineDefaultSchemaType(kClassifier: KClassifier): ApiType = when (kClassifier) {
        PartData.FormItem::class -> ApiType.STRING
        PartData.FileItem::class,
        PartData.BinaryItem::class,
        PartData.BinaryChannelItem::class -> ApiType.STRING

        else -> throw KopapiException("Unsupported PartData type: $kClassifier")
    }

    /**
     * Determines the default schema format based on the PartData subclass.
     */
    private fun determineDefaultSchemaFormat(kClassifier: KClassifier): ApiFormat? = when (kClassifier) {
        PartData.FormItem::class -> null
        PartData.FileItem::class,
        PartData.BinaryItem::class,
        PartData.BinaryChannelItem::class -> ApiFormat.BINARY

        else -> throw KopapiException("Unsupported PartData type: $kClassifier")
    }

    /**
     * Creates the appropriate [MultipartSchema] based on the PartData subclass.
     */
    private fun createMultipartSchema(
        kClassifier: KClassifier,
        name: String,
        isRequired: Boolean,
        description: String?,
        contentType: ContentType,
        schemaType: ApiType,
        schemaFormat: ApiFormat?
    ): MultipartSchema {
        return when (kClassifier) {
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

            else -> throw KopapiException("Unsupported PartData type: $kClassifier")
        }
    }
}
