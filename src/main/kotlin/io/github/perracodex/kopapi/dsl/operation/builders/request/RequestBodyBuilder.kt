/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.request

import io.github.perracodex.kopapi.dsl.common.schema.configurable.ISchemaAttributeConfigurable
import io.github.perracodex.kopapi.dsl.common.schema.configurable.SchemaAttributeConfigurable
import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.builders.operation.ApiOperationBuilder
import io.github.perracodex.kopapi.dsl.operation.builders.type.TypeConfig
import io.github.perracodex.kopapi.dsl.operation.elements.ApiMultipart
import io.github.perracodex.kopapi.dsl.operation.elements.ApiRequestBody
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.types.Composition
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull
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
    public var required: Boolean = true,
    public var composition: Composition? = null,
    public var contentType: Set<ContentType> = setOf(ContentType.Application.Json),

    @Suppress("PropertyName")
    @PublishedApi
    internal val _schemaAttributeConfigurable: SchemaAttributeConfigurable = SchemaAttributeConfigurable()
) : ISchemaAttributeConfigurable by _schemaAttributeConfigurable {
    public var description: String by MultilineString()

    @Suppress("PropertyName")
    @PublishedApi
    internal val _config: Config = Config()

    /**
     * Registers a new type for the request body.
     *
     * #### Sample Usage
     * ```
     * // Register a type defaulting to JSON.
     * addType<SomeType>()
     *
     * // Register another type to a specific content type.
     * addType<SomeType> {
     *      contentType = setOf(ContentType.Application.Xml)
     * }
     *
     * // Register a type with multiple content types.
     * addType<SomeType> {
     *      contentType = setOf(
     *          ContentType.Application.Json,
     *          ContentType.Application.Xml
     *      )
     * }
     * ```
     *
     * @param T The type of the request body.
     * @param configure An optional lambda for configuring the type. Default: `JSON`.
     */
    @Suppress("DuplicatedCode")
    public inline fun <reified T : Any> addType(noinline configure: TypeConfig.() -> Unit = {}) {
        // Ensure there is always a default content type.
        if (contentType.isEmpty()) {
            contentType = setOf(ContentType.Application.Json)
        }

        // Determine the effective content types for new type being added.
        val typeConfig: TypeConfig = TypeConfig().apply(configure)
        val effectiveContentTypes: Set<ContentType> = when {
            typeConfig.contentType.isNullOrEmpty() -> contentType
            else -> typeConfig.contentType ?: contentType
        }

        // Register the new type with the effective content types.
        val typeDetails: ApiRequestBody.TypeDetails = ApiRequestBody.TypeDetails(
            type = typeOf<T>(),
            schemaAttributes = typeConfig._schemaAttributeConfigurable.attributes
        )
        effectiveContentTypes.forEach { contentTypeKey ->
            _config.allTypes.getOrPut(contentTypeKey) { mutableSetOf() }.add(typeDetails)
        }
    }

    /**
     * Registers a multipart request body.
     *
     * #### Sample Usage
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
     * // Explicit ContentType.MultiPart.Encrypted.
     * multipart {
     *      contentType = ContentType.MultiPart.Encrypted
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
     */
    public fun multipart(configure: MultipartBuilder.() -> Unit) {
        val multipartBuilder: MultipartBuilder = MultipartBuilder().apply(configure)
        val multipartContentType: ContentType = multipartBuilder.contentType ?: ContentType.MultiPart.FormData
        val apiMultipart = ApiMultipart(
            contentType = multipartContentType,
            description = multipartBuilder.description.trimOrNull(),
            parts = multipartBuilder._config.parts
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
            content = contentMap.takeIf { it.isNotEmpty() },
            multipartContent = _config.multipartParts.takeIf { it.isNotEmpty() }
        )
    }

    @PublishedApi
    internal class Config {
        /** Holds the types associated with the request body (non-multipart). */
        val allTypes: MutableMap<ContentType, MutableSet<ApiRequestBody.TypeDetails>> = mutableMapOf()

        /** Holds the multipart parts schema. */
        val multipartParts: MutableMap<ContentType, ApiMultipart> = mutableMapOf()
    }
}
