/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.composer.request

import io.github.perracodex.kopapi.annotation.SchemaAttributeUtils
import io.github.perracodex.kopapi.composer.annotation.ComposerApi
import io.github.perracodex.kopapi.composer.header.HeaderComposer
import io.github.perracodex.kopapi.composer.header.HeaderObject
import io.github.perracodex.kopapi.dsl.operation.element.ApiMultipart
import io.github.perracodex.kopapi.dsl.operation.element.ApiRequestBody
import io.github.perracodex.kopapi.schema.OpenApiSchema
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.github.perracodex.kopapi.schema.facet.CompositionSchema
import io.github.perracodex.kopapi.schema.facet.ElementSchema
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.system.Tracer
import io.github.perracodex.kopapi.type.ApiFormat
import io.github.perracodex.kopapi.type.ApiType
import io.github.perracodex.kopapi.util.orNull
import io.github.perracodex.kopapi.util.trimOrNull
import io.ktor.http.*
import io.ktor.http.content.*
import kotlin.reflect.KClassifier

/**
 * Responsible for composing the `request body` section of the OpenAPI schema.
 * This includes handling both standard request bodies and multipart/form-data bodies.
 *
 * @see [RequestBodyObject]
 * @see [ApiRequestBody]
 * @see [ApiMultipart]
 * @see [ApiMultipart.Part]
 */
@ComposerApi
internal object RequestBodyComposer {
    private val tracer: Tracer = Tracer<RequestBodyComposer>()

    /**
     * Generates the `request body` section of the OpenAPI schema.
     *
     * This method processes both standard request bodies and multipart request bodies.
     *
     * @param requestBody The [ApiRequestBody] object containing the request body metadata.
     * @return The constructed [RequestBodyObject] object representing the OpenAPI request body.
     */
    fun compose(requestBody: ApiRequestBody): RequestBodyObject {
        tracer.info("Composing the 'request body' section of the OpenAPI schema.")

        val standardContent: Map<ContentType, OpenApiSchema.ContentSchema>? = processStandardContent(
            requestBody = requestBody
        )

        val multipartContent: Map<ContentType, OpenApiSchema.ContentSchema>? = processMultipartContent(
            requestBody = requestBody
        )

        // Combine both standardContent and multipartContent.
        val content: Map<ContentType, OpenApiSchema.ContentSchema> = (standardContent.orEmpty() + multipartContent.orEmpty())
        if (content.isEmpty()) {
            throw KopapiException("No content types found for the request body.")
        }

        return RequestBodyObject(
            description = requestBody.description,
            required = requestBody.required,
            content = content
        )
    }

    /**
     * Processes `standard (non-multipart)` content types and generates their corresponding schemas.
     *
     * @param requestBody The [ApiRequestBody] object containing the request body metadata.
     * @return A map of [ContentType] to [OpenApiSchema.ContentSchema] for standard content.
     */
    private fun processStandardContent(
        requestBody: ApiRequestBody
    ): Map<ContentType, OpenApiSchema.ContentSchema>? {
        if (requestBody.content.isNullOrEmpty()) {
            tracer.info("No standard content types found for the request body.")
            return null
        }

        val schemasByContentType: MutableMap<ContentType, MutableList<ElementSchema>> = mutableMapOf()

        requestBody.content.forEach { (contentType, typeDetails) ->
            tracer.debug("Processing standard content type: $contentType")

            typeDetails.forEach { details ->
                var baseSchema: ElementSchema = SchemaRegistry.introspectType(type = details.type)?.schema
                    ?: throw KopapiException(
                        "No schema found for type: ${details.type} with content type: $contentType"
                    )

                // Apply additional parameter attributes.
                details.schemaAttributes?.let { attributes ->
                    baseSchema = SchemaAttributeUtils.copySchemaAttributes(
                        schema = baseSchema,
                        attributes = attributes
                    )
                }

                schemasByContentType.getOrPut(contentType) {
                    mutableListOf()
                }.add(baseSchema)
            }
        }

        return schemasByContentType.mapValues { (_, schemas) ->
            CompositionSchema.determine(
                composition = requestBody.composition,
                schemas = schemas,
                examples = requestBody.examples
            )
        }
    }

    /**
     * Processes `multipart` content types and generates their corresponding schemas.
     *
     * @param requestBody The [ApiRequestBody] object containing the request body metadata.
     * @return A map of [ContentType] to [OpenApiSchema.ContentSchema] for multipart content.
     */
    private fun processMultipartContent(
        requestBody: ApiRequestBody
    ): Map<ContentType, OpenApiSchema.ContentSchema>? {
        if (requestBody.multipartContent.isNullOrEmpty()) {
            tracer.info("No multipart content types found for the request body.")
            return null
        }

        return requestBody.multipartContent.mapValues { (_, apiMultipart) ->
            tracer.debug("Processing multipart content type: ${apiMultipart.contentType}")

            val properties: MutableMap<String, MultipartObject> = mutableMapOf()
            val requiredFields: MutableList<String> = mutableListOf()
            val encodings: MutableMap<String, Any> = mutableMapOf()

            apiMultipart.parts.forEach { part ->
                // Add encoding information for each part based on the content type.
                val partKClassifier: KClassifier = part.type.classifier
                    ?: throw KopapiException("KType classifier is null: ${part.type}")
                val defaultConfiguration: MultipartDefaultConfig = multipartConfigs[partKClassifier]
                    ?: throw KopapiException("Unsupported PartData type: $partKClassifier")

                // Create the part schema.
                val partSchema: MultipartObject = createPartSchema(
                    part = part,
                    defaultConfiguration = defaultConfiguration
                )
                properties[part.name] = partSchema

                // Track required fields for the multipart schema.
                if (partSchema.isRequired) {
                    requiredFields.add(part.name)
                }

                // Process headers for the part.
                val headers: MutableMap<String, HeaderObject>? = part.headers?.let {
                    HeaderComposer.compose(headers = part.headers)
                }

                // Determine the encoding for the part.
                val contentType: Set<ContentType> = part.contentType ?: setOf(defaultConfiguration.contentType)
                val partEncoding: String = contentType.joinToString(separator = ", ") { it.toString() }
                encodings[part.name] = mapOf(
                    "contentType" to partEncoding,
                    "headers" to headers
                )
            }

            // Construct the multipart schema without placeholders.
            val schema: MultipartObject.Object = MultipartObject.Object(
                description = apiMultipart.description.trimOrNull(),
                properties = properties,
                requiredFields = requiredFields.orNull(),
                encoding = encodings,
            )

            OpenApiSchema.ContentSchema(schema = schema, examples = apiMultipart.examples)
        }
    }

    /**
     * Factory function to create the corresponding [MultipartObject] for a given [part].
     *
     * @param part The [ApiMultipart.Part] instance containing part metadata.
     * @param defaultConfiguration The default configuration for the given [part].
     * @return The corresponding [MultipartObject] for the given [part].
     */
    private fun createPartSchema(
        part: ApiMultipart.Part,
        defaultConfiguration: MultipartDefaultConfig
    ): MultipartObject {
        // Override defaults with provided values if present.
        val schemaType: ApiType = part.schemaType ?: defaultConfiguration.schemaType
        val schemaFormat: String? = part.schemaFormat ?: defaultConfiguration.schemaFormat?.value

        // Construct the schema.
        return MultipartObject.PartItem(
            name = part.name,
            isRequired = part.isRequired,
            description = part.description.trimOrNull(),
            schemaType = schemaType,
            schemaFormat = schemaFormat.trimOrNull()
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
