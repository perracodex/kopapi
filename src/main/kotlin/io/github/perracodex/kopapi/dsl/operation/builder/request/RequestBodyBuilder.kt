/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.dsl.operation.builder.request

import io.github.perracodex.kopapi.dsl.example.delegate.ExampleDelegate
import io.github.perracodex.kopapi.dsl.example.delegate.IExampleConfigurable
import io.github.perracodex.kopapi.dsl.marker.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.builder.operation.ApiOperationBuilder
import io.github.perracodex.kopapi.dsl.operation.builder.type.TypeConfig
import io.github.perracodex.kopapi.dsl.operation.element.ApiMultipart
import io.github.perracodex.kopapi.dsl.operation.element.ApiRequestBody
import io.github.perracodex.kopapi.dsl.schema.delegate.ISchemaAttributeConfigurable
import io.github.perracodex.kopapi.dsl.schema.delegate.SchemaAttributeDelegate
import io.github.perracodex.kopapi.dsl.schema.element.ApiSchemaAttributes
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.type.Composition
import io.github.perracodex.kopapi.util.orNull
import io.github.perracodex.kopapi.util.string.MultilineString
import io.github.perracodex.kopapi.util.trimOrNull
import io.ktor.http.*
import kotlin.reflect.KClassifier
import kotlin.reflect.typeOf

/**
 * Builds a request body for an API endpoint's metadata, supporting multiple content types,
 * including multipart requests. Note that only one request body can be defined per API Operation.
 *
 * @property description A description of the request body's content and what it represents.
 * @property required Indicates whether the request body is mandatory for the API call.
 * @property composition The composition of the request body. Only meaningful if multiple types are provided.
 * @property contentType A set of [ContentType]s for the request body. Default: `JSON`.
 *
 * @see [ApiOperationBuilder.requestBody]
 */
@KopapiDsl
public class RequestBodyBuilder @PublishedApi internal constructor(
    private val schemaAttributeDelegate: SchemaAttributeDelegate = SchemaAttributeDelegate(),
    private val examplesDelegate: ExampleDelegate = ExampleDelegate()
) : ISchemaAttributeConfigurable by schemaAttributeDelegate,
    IExampleConfigurable by examplesDelegate {

    public var description: String by MultilineString()
    public var required: Boolean = true
    public var composition: Composition? = null
    public var contentType: Set<ContentType> = setOf(ContentType.Application.Json)

    @Suppress("PropertyName")
    @PublishedApi
    internal val _config: Config = Config()

    /**
     * Registers a new type for the request body.
     *
     * #### Usage
     * ```
     * // Register a type defaulting to JSON.
     * addType<SomeType>()
     * ```
     * ```
     * // Register another type to a specific content type.
     * addType<SomeType> {
     *      contentType = setOf(ContentType.Application.Xml)
     * }
     * ```
     * ```
     * // Register a type with multiple content types.
     * addType<SomeType> {
     *      contentType = setOf(
     *          ContentType.Application.Json,
     *          ContentType.Application.Xml
     *      )
     * }
     * ```
     *
     * @receiver [TypeConfig] Optional lambda for configuring the type. Default: `JSON`.
     *
     * @param T The type of the request body.
     */
    @Suppress("DuplicatedCode")
    public inline fun <reified T : Any> addType(noinline builder: TypeConfig.() -> Unit = {}) {
        // Ensure there is always a default content type.
        if (contentType.isEmpty()) {
            contentType = setOf(ContentType.Application.Json)
        }

        // Determine the effective content types for new type being added.
        val typeConfig: TypeConfig = TypeConfig().apply(builder)
        val effectiveContentTypes: Set<ContentType> = when {
            typeConfig.contentType.isNullOrEmpty() -> contentType
            else -> typeConfig.contentType ?: contentType
        }

        // Register the new type with the effective content types.
        val typeDetails: ApiRequestBody.TypeDetails = ApiRequestBody.TypeDetails(
            type = typeOf<T>(),
            schemaAttributes = typeConfig._schemaAttributes
        )
        effectiveContentTypes.forEach { contentTypeKey ->
            _config.allTypes.getOrPut(contentTypeKey) { mutableSetOf() }.add(typeDetails)
        }
    }

    /**
     * Registers a multipart request body.
     *
     * #### Usage
     * ```
     * // Implicit ContentType.MultiPart.FormData (default).
     * multipart {
     *      part<PartData.FileItem>("file") {
     *          description = "The file to upload."
     *          contentType = setOf(
     *              ContentType.Image.JPEG,
     *              ContentType.Image.PNG
     *          )
     *      }
     *      part<PartData.FormItem>("metadata") {
     *          description = "Metadata about the file, provided as JSON."
     *      }
     * }
     * ```
     * ```
     * // Explicit ContentType.MultiPart.Signed.
     * multipart {
     *      contentType = ContentType.MultiPart.Signed
     *
     *      part<PartData.FileItem>("secureFile") {
     *          description = "A securely uploaded file."
     *          contentType = setOf(
     *              ContentType.Image.JPEG,
     *              ContentType.Image.PNG
     *          )
     *      }
     *      part<PartData.FormItem>("metadata") {
     *          description = "Additional metadata about the file."
     *      }
     * }
     * ```
     *
     * @receiver [MultipartBuilder] The builder used to configure the multipart.
     */
    public fun multipart(builder: MultipartBuilder.() -> Unit) {
        val multipartBuilder: MultipartBuilder = MultipartBuilder().apply(builder)
        val multipartContentType: ContentType = multipartBuilder.contentType ?: ContentType.MultiPart.FormData
        val apiMultipart = ApiMultipart(
            contentType = multipartContentType,
            description = multipartBuilder.description.trimOrNull(),
            parts = multipartBuilder._config.parts,
            examples = multipartBuilder._config.examples()
        )

        _config.multipartParts[multipartContentType] = apiMultipart
    }

    /**
     * Builds an [ApiRequestBody] instance from the current builder state.
     *
     * @return The constructed [ApiRequestBody] instance.
     */
    @PublishedApi
    internal fun build(): ApiRequestBody {
        // Create the map of ContentType to Set<KType>, ensuring each ContentType maps to its specific types.
        val contentMap: Map<ContentType, Set<ApiRequestBody.TypeDetails>> = _config.allTypes
            .mapValues { (_, typeDetails) ->
                // Filter out types that are not explicitly defined.
                typeDetails.filterNot { details ->
                    val classifier: KClassifier? = details.type.classifier
                    (classifier == Unit::class) || (classifier == Any::class) || (classifier == Nothing::class)
                }.toSet()
            }.filter { (_, types) ->
                types.isNotEmpty()
            }.toSortedMap(
                compareBy(
                    { it.contentType },
                    { it.contentSubtype }
                )
            )

        // A request must either define an explicit content type or a multipart part. Both can also be defined.
        if (contentMap.isEmpty() && _config.multipartParts.isEmpty()) {
            throw KopapiException("RequestBody must define either an explicit type, a multipart part, or both.")
        }

        // Determine the final composition without mutating the builder's property.
        val contentComposition: Composition? = when {
            contentMap.size > 1 -> composition ?: Composition.ANY_OF
            else -> null
        }

        return ApiRequestBody(
            description = description.trimOrNull(),
            required = required,
            composition = contentComposition,
            content = contentMap.orNull(),
            multipartContent = _config.multipartParts.orNull(),
            examples = examplesDelegate.build()
        )
    }

    @PublishedApi
    internal inner class Config {
        /** Holds the types associated with the request body (non-multipart). */
        val allTypes: MutableMap<ContentType, MutableSet<ApiRequestBody.TypeDetails>> = mutableMapOf()

        /** Holds the multipart parts schema. */
        val multipartParts: MutableMap<ContentType, ApiMultipart> = mutableMapOf()

        /**
         * Returns the registered schema attributes.
         */
        fun schemaAttributes(): ApiSchemaAttributes? {
            return schemaAttributeDelegate.attributes
        }
    }
}
