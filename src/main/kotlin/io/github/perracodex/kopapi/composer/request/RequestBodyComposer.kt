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
import io.github.perracodex.kopapi.schema.IOpenApiSchema
import io.github.perracodex.kopapi.schema.MultipartSchema
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.types.ApiFormat
import io.github.perracodex.kopapi.types.ApiType
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.http.*
import io.ktor.http.content.*
import kotlin.reflect.KClassifier

/**
 * Responsible for composing the `request body` section of the OpenAPI schema.
 * This includes handling both standard request bodies and multipart/form-data bodies.
 *
 * @see [ApiRequestBody]
 * @see [ApiMultipart]
 * @see [ApiMultipart.Part]
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
        val standardContent: Map<ContentType, OpenAPiSchema.ContentSchema>? = processStandardContent(
            requestBody = requestBody
        )

        val multipartContent: Map<ContentType, OpenAPiSchema.ContentSchema>? = processMultipartContent(
            requestBody = requestBody
        )

        // Combine both standardContent and multipartContent.
        val content: Map<ContentType, OpenAPiSchema.ContentSchema> = (standardContent.orEmpty() + multipartContent.orEmpty())
        if (content.isEmpty()) {
            throw KopapiException("No content types found for the request body.")
        }

        return PathRequestBody(
            description = requestBody.description,
            required = requestBody.required,
            content = content
        )
    }

    /**
     * Processes `standard (non-multipart)` content types and generates their corresponding schemas.
     *
     * @param requestBody The [ApiRequestBody] object containing the request body metadata.
     * @return A map of [ContentType] to [OpenAPiSchema.ContentSchema] for standard content.
     */
    private fun processStandardContent(
        requestBody: ApiRequestBody
    ): Map<ContentType, OpenAPiSchema.ContentSchema>? {
        if (requestBody.content.isNullOrEmpty()) {
            return null
        }

        val schemas: MutableMap<ContentType, MutableList<ElementSchema>> = mutableMapOf()

        requestBody.content.forEach { (contentType, types) ->
            types.forEach { type ->
                val schema: ElementSchema = SchemaRegistry.inspectType(type = type)?.schema
                    ?: throw KopapiException("No schema found for type: $type with content type: $contentType")
                schemas.getOrPut(contentType) { mutableListOf() }.add(schema)
            }
        }

        return schemas.mapValues { (_, schemas) ->
            IOpenApiSchema.determineSchema(composition = requestBody.composition, schemas = schemas)
        }
    }

    /**
     * Processes `multipart` content types and generates their corresponding schemas.
     *
     * @param requestBody The [ApiRequestBody] object containing the request body metadata.
     * @return A map of [ContentType] to [OpenAPiSchema.ContentSchema] for multipart content.
     */
    private fun processMultipartContent(
        requestBody: ApiRequestBody
    ): Map<ContentType, OpenAPiSchema.ContentSchema>? {
        if (requestBody.multipartContent.isNullOrEmpty()) {
            return null
        }

        return requestBody.multipartContent.mapValues { (_, apiMultipart) ->
            val properties: MutableMap<String, MultipartSchema> = mutableMapOf()
            val requiredFields: MutableList<String> = mutableListOf()

            apiMultipart.parts.forEach { part ->
                val partSchema: MultipartSchema = createPartSchema(part = part)
                properties[part.name] = partSchema

                // Track required fields for the multipart schema.
                if (partSchema.isRequired) {
                    requiredFields.add(part.name)
                }
            }

            // Construct the multipart schema.
            val schema: MultipartSchema.Object = MultipartSchema.Object(
                description = apiMultipart.description.trimOrNull(),
                properties = properties,
                requiredFields = requiredFields.ifEmpty { null }
            )

            OpenAPiSchema.ContentSchema(schema = schema)
        }
    }

    /**
     * Factory function to create the corresponding [MultipartSchema] for a given [part].
     *
     * @param part The [ApiMultipart.Part] instance containing part metadata.
     * @return The corresponding [MultipartSchema] for the given [part].
     */
    private fun createPartSchema(part: ApiMultipart.Part): MultipartSchema {
        // Get the default configuration for the given PartData subclass.
        val partKClassifier: KClassifier = part.type.classifier
            ?: throw KopapiException("KType classifier is null: ${part.type}")
        val defaultConfiguration: MultipartDefaultConfig = multipartConfigs[partKClassifier]
            ?: throw KopapiException("Unsupported PartData type: $partKClassifier")

        // Override defaults with provided values if present.
        val contentType: ContentType = part.contentType ?: defaultConfiguration.contentType
        val schemaType: ApiType = part.schemaType ?: defaultConfiguration.schemaType
        val schemaFormat: ApiFormat? = part.schemaFormat ?: defaultConfiguration.schemaFormat

        // Construct the schema.
        return MultipartSchema.PartItem(
            name = part.name,
            isRequired = part.isRequired,
            description = part.description.trimOrNull(),
            contentType = contentType,
            schemaType = schemaType,
            schemaFormat = schemaFormat
        )
    }

    /**
     * Mapping for default configurations for each [PartData] subclass.
     */
    private val multipartConfigs: Map<KClassifier, MultipartDefaultConfig> = mapOf(
        PartData.FormItem::class to MultipartDefaultConfig(
            contentType = ContentType.Text.Plain,
            schemaType = ApiType.STRING,
            schemaFormat = null
        ),
        PartData.FileItem::class to MultipartDefaultConfig(
            contentType = ContentType.Application.OctetStream,
            schemaType = ApiType.STRING,
            schemaFormat = ApiFormat.BINARY
        ),
        PartData.BinaryItem::class to MultipartDefaultConfig(
            contentType = ContentType.Application.OctetStream,
            schemaType = ApiType.STRING,
            schemaFormat = ApiFormat.BINARY
        ),
        PartData.BinaryChannelItem::class to MultipartDefaultConfig(
            contentType = ContentType.Application.OctetStream,
            schemaType = ApiType.STRING,
            schemaFormat = ApiFormat.BINARY
        )
    )

    /**
     * Holds the default configuration for a concrete [PartData] subclass.
     */
    private data class MultipartDefaultConfig(
        val contentType: ContentType,
        val schemaType: ApiType,
        val schemaFormat: ApiFormat?
    )
}
