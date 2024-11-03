/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.request

import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.builders.operation.ApiOperationBuilder
import io.github.perracodex.kopapi.dsl.operation.elements.ApiMultipart
import io.github.perracodex.kopapi.dsl.operation.elements.ApiRequestBody
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.types.Composition
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.http.*
import kotlin.reflect.KClassifier
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Builds a request body for an API endpoint's metadata, supporting multiple content types,
 * including multipart requests. Note that only one request body can be defined per API Operation.
 *
 * @property description A description of the request body's content and what it represents.
 * @property required Indicates whether the request body is mandatory for the API call.
 * @property composition The composition of the request body. Only meaningful if multiple types are provided.
 *
 * @see [ApiOperationBuilder.requestBody]
 */
@KopapiDsl
public class RequestBodyBuilder(
    public var required: Boolean = true,
    public var composition: Composition? = null
) {
    public var description: String by MultilineString()

    @Suppress("PropertyName")
    @PublishedApi
    internal val _config: Config = Config()

    /**
     * Registers a new type for the request body.
     *
     * #### Sample Usage
     * ```
     * requestBody<MyRequestBodyType>() {
     *      // Register an additional type.
     *      addType<AnotherType>()
     *
     *      // Register another type to the Pdf ContentType
     *      // instead of the default.
     *      addType<YetAnotherType>(
     *          contentType = setOf(ContentType.Application.Pdf)
     *      )
     * }
     * ```
     *
     * @param T The type of the request body.
     * @param contentType Optional set of [ContentType]s to associate with the type.
     *                    Defaults to the primary `ContentType`, or to `JSON` if no primary type is set.
     */
    @Suppress("DuplicatedCode")
    public inline fun <reified T : Any> addType(contentType: Set<ContentType>? = null) {
        // Ensure there's at least one ContentType.
        val effectiveContentTypes: Set<ContentType> = when {
            contentType.isNullOrEmpty() -> _config.primaryContentType ?: setOf(ContentType.Application.Json)
            else -> contentType
        }

        // When a request is built, the first registered type is always the primary one.
        // Subsequent types are registered after the primary one.
        // Therefore, any subtype which does not specify its own ContentType will
        // share the primary ContentType.
        if (_config.primaryContentType == null) {
            _config.primaryContentType = effectiveContentTypes
        }

        val type: KType = typeOf<T>()
        effectiveContentTypes.forEach { contentTypeKey ->
            _config.allTypes.getOrPut(contentTypeKey) { mutableSetOf() }.add(type)
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
     *      }
     *      part<PartData.FormItem>("metadata") {
     *          description = "Metadata about the file, provided as JSON."
     *      }
     * }
     *
     * // Explicit ContentType.MultiPart.Encrypted.
     * multipart(contentType = ContentType.MultiPart.Encrypted) {
     *      part<PartData.FileItem>("secureFile") {
     *          description = "A securely uploaded file."
     *      }
     *      part<PartData.FormItem>("metadata") {
     *          description = "Additional metadata about the file."
     *      }
     * }
     * ```
     */
    public fun multipart(
        contentType: ContentType = ContentType.MultiPart.FormData,
        configure: MultipartBuilder.() -> Unit
    ) {
        val multipartBuilder: MultipartBuilder = MultipartBuilder().apply(configure)
        val apiMultipart = ApiMultipart(
            contentType = contentType,
            description = multipartBuilder.description.trimOrNull(),
            parts = multipartBuilder._config.parts
        )

        _config.multipartParts[contentType] = apiMultipart
    }

    /**
     * Builds an [ApiRequestBody] instance from the current builder state.
     *
     * @return The constructed [ApiRequestBody] instance.
     */
    @PublishedApi
    internal fun build(): ApiRequestBody {
        // Create the map of ContentType to Set<KType>, ensuring each ContentType maps to its specific types.
        val contentMap: Map<ContentType, Set<KType>> = _config.allTypes
            .mapValues { (_, types) ->
                // Filter out types that are not explicitly defined.
                types.filterNot { type ->
                    val classifier: KClassifier? = type.classifier
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
        val allTypes: MutableMap<ContentType, MutableSet<KType>> = mutableMapOf()

        /** Holds the multipart parts schema. */
        val multipartParts: MutableMap<ContentType, ApiMultipart> = mutableMapOf()

        /**
         * The primary `ContentType` for the request.
         * Applied to subsequent types if these do not specify their own `ContentType`.
         */
        var primaryContentType: Set<ContentType>? = null
    }
}
