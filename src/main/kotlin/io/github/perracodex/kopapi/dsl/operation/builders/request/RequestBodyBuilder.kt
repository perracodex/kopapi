/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.request

import io.github.perracodex.kopapi.dsl.markers.OperationDsl
import io.github.perracodex.kopapi.dsl.operation.builders.ApiOperationBuilder
import io.github.perracodex.kopapi.dsl.operation.elements.ApiRequestBody
import io.github.perracodex.kopapi.schema.MultipartSchema
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.types.Composition
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.http.*
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
@Suppress("DuplicatedCode")
@OperationDsl
public class RequestBodyBuilder(
    public var required: Boolean = true,
    public var composition: Composition? = null
) {
    public var description: String by MultilineString()

    /** Holds the types associated with the request body (non-multipart). */
    @PublishedApi
    internal val allTypes: MutableMap<ContentType, MutableSet<KType>> = mutableMapOf()

    /** Holds the multipart parts schema. */
    @PublishedApi
    internal val multipartParts: MutableMap<ContentType, MultipartSchema.Object> = mutableMapOf()

    /**
     * The primary `ContentType` for the request.
     * Applied to subsequent types if these do not specify their own `ContentType`.
     */
    @PublishedApi
    internal var primaryContentType: Set<ContentType>? = null

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
    public inline fun <reified T : Any> addType(contentType: Set<ContentType>? = null) {
        // Ensure there's at least one ContentType.
        val effectiveContentTypes: Set<ContentType> = when {
            contentType.isNullOrEmpty() -> primaryContentType ?: setOf(ContentType.Application.Json)
            else -> contentType
        }

        // When a request is built, the first registered type is always the primary one.
        // Subsequent types are registered after the primary one.
        // Therefore, any subtype which does not specify its own ContentType will
        // default to the primary ContentType.
        if (primaryContentType == null) {
            primaryContentType = effectiveContentTypes
        }

        val type: KType = typeOf<T>()
        effectiveContentTypes.forEach { contentTypeKey ->
            allTypes.getOrPut(contentTypeKey) { mutableSetOf() }.add(type)
        }
    }

    /**
     * Registers a multipart request body.
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
     *      part<PartData.FormItem>("myFormPart") {
     *      description = "The form data."
     * }
     * ```
     */
    public fun multipart(
        contentType: ContentType = ContentType.MultiPart.FormData,
        configure: MultipartBuilder.() -> Unit
    ) {
        // Check at runtime if the contentType belongs to the "multipart" category
        // Using the contentType.contentType property for comparison, as all
        // multipart content types share the same content type, avoiding this hardcoding it here.
        if (contentType.contentType != ContentType.MultiPart.FormData.contentType) {
            throw IllegalArgumentException(
                "Invalid content type for multipart. Must be of type: `ContentType.MultiPart`"
            )
        }

        val multipartBuilder: MultipartBuilder = MultipartBuilder().apply(configure)
        multipartParts[contentType] = multipartBuilder.build()
    }

    /**
     * Builds an [ApiRequestBody] instance from the current builder state.
     *
     * @return The constructed [ApiRequestBody] instance.
     */
    @PublishedApi
    internal fun build(): ApiRequestBody {
        // Create the map of ContentType to Set<KType>, ensuring each ContentType maps to its specific types.
        val contentMap: Map<ContentType, Set<KType>> = allTypes
            .mapValues { it.value.toSet() }
            .filterValues { it.isNotEmpty() }
            .toSortedMap(
                compareBy(
                    { it.contentType },
                    { it.contentSubtype }
                )
            )

        // Determine the final composition without mutating the builder's property.
        val contentComposition: Composition? = when {
            allTypes.size > 1 && composition == null -> Composition.ANY_OF
            allTypes.size > 1 -> composition
            else -> null
        }

        if (allTypes.isEmpty() && multipartParts.isEmpty()) {
            throw KopapiException("RequestBody must have at least one type or a multipart part defined.")
        }

        return ApiRequestBody(
            description = description.trimOrNull(),
            required = required,
            composition = contentComposition,
            content = contentMap.takeIf { it.isNotEmpty() },
            multipartContent = multipartParts.takeIf { it.isNotEmpty() }
        )
    }
}
