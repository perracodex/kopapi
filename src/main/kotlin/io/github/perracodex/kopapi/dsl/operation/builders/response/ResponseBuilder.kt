/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.response

import io.github.perracodex.kopapi.dsl.operation.builders.ApiOperationBuilder
import io.github.perracodex.kopapi.dsl.operation.builders.attributes.HeaderBuilder
import io.github.perracodex.kopapi.dsl.operation.builders.attributes.LinkBuilder
import io.github.perracodex.kopapi.dsl.operation.elements.ApiHeader
import io.github.perracodex.kopapi.dsl.operation.elements.ApiLink
import io.github.perracodex.kopapi.dsl.operation.elements.ApiResponse
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.types.Composition
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.http.*
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * A builder for constructing a response in an API endpoint's metadata.
 *
 * @property description A description of the response content and what it represents.
 * @property composition The composition of the response. Only meaningful if multiple types are provided.
 *
 * @see [ApiOperationBuilder.response]
 */
@Suppress("DuplicatedCode")
public class ResponseBuilder {
    public var description: String by MultilineString()
    public var composition: Composition? = null

    private val headers: MutableSet<ApiHeader> = mutableSetOf()
    private val links: MutableSet<ApiLink> = mutableSetOf()

    /** Holds the types associated with the response. */
    @PublishedApi
    internal val allTypes: MutableMap<ContentType, MutableSet<KType>> = mutableMapOf()

    /** Holds the composition per content type. */
    private val compositionsPerContentType: MutableMap<ContentType, Composition?> = mutableMapOf()

    /**
     * The primary `ContentType` for the response.
     * Applied to subsequent types if these do not specify their own `ContentType`.
     */
    @PublishedApi
    internal var primaryContentType: Set<ContentType>? = null

    /**
     * Associates a specific `ContentType` with a given `Composition`.
     *
     * This allows defining how multiple types should be combined for a particular content type,
     * using the provided `composition` strategy.
     *
     * Note: The provided `composition` will only take effect if the `response` includes
     * the specified `contentType` either globally (for the entire response) or for
     * any individual type associated with that content type. If the `contentType` is not present
     * in the response, the composition setting will be ignored.
     *
     * @param contentType The content type to be associated with the provided [composition].
     * @param composition The [Composition] to associate with the provided [contentType].
     */
    public fun compositionFor(contentType: ContentType, composition: Composition) {
        compositionsPerContentType[contentType] = composition
    }

    /**
     * Registers a new type for the response.
     *
     * #### Sample Usage
     * ```
     * response<MyResponseType>(status = HttpStatusCode.OK) {
     *      // Optional additional type.
     *      // All content types will be associated with this type.
     *      addType<AnotherType>()
     *
     *      // Register another type to a specific content type.
     *      addType<YetAnotherType>(
     *          setOf(ContentType.Application.Pdf)
     *      )
     * }
     * ```
     *
     * @param T The type of the response.
     * @param contentType Optional set of [ContentType]s to associate with the type.
     *                    Defaults to the primary `ContentType`, or to `JSON` if no primary type is set.
     */
    public inline fun <reified T : Any> addType(contentType: Set<ContentType>? = null) {
        // Ensure there's at least one ContentType.
        val effectiveContentTypes: Set<ContentType> = when {
            contentType.isNullOrEmpty() -> primaryContentType ?: setOf(ContentType.Application.Json)
            else -> contentType
        }

        // When a response is build, the first registered type is always the primary one.
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

    @PublishedApi
    internal fun build(status: HttpStatusCode): ApiResponse {
        // Create the map of ContentType to Set<KType>, ensuring each ContentType maps to its specific types.
        val contentMap: Map<ContentType, Set<KType>>? = allTypes.takeIf { it.isNotEmpty() }
            ?.mapValues { it.value.toSet() }
            ?.filterValues { it.isNotEmpty() }
            ?.toSortedMap(
                compareBy(
                    { it.contentType },
                    { it.contentSubtype }
                )
            )

        // Determine the appropriate composition for each content type.
        val finalCompositions: Map<ContentType, Composition?> = contentMap?.mapValues { (contentType, _) ->
            compositionsPerContentType[contentType] ?: composition
        } ?: emptyMap()

        // Return the constructed ApiResponse instance.
        return ApiResponse(
            status = status,
            description = description.trimOrNull(),
            headers = headers.takeIf { it.isNotEmpty() },
            composition = finalCompositions,
            content = contentMap,
            links = links.takeIf { it.isNotEmpty() }
        )
    }

    /**
     * Adds a header to the response.
     *
     * #### Sample Usage
     * ```
     * header("X-Rate-Limit") {
     *     description = "Number of allowed requests per period."
     *     required = true
     * }
     * ```
     *
     * @param name The name of the header.
     * @param configure A lambda receiver for configuring the [HeaderBuilder].
     */
    public fun header(name: String, configure: HeaderBuilder.() -> Unit) {
        val header: ApiHeader = HeaderBuilder(name = name).apply(configure).build()
        addHeader(header = header)
    }

    /**
     * Adds a link to the response.
     *
     * #### Sample Usage
     * ```
     * link("getNextItem") {
     *     description = "Link to the next item."
     * }
     * ```
     *
     * @param operationId The name of an existing, resolvable OAS operation.
     * @param configure A lambda receiver for configuring the [LinkBuilder].
     */
    public fun link(operationId: String, configure: LinkBuilder.() -> Unit) {
        val link: ApiLink = LinkBuilder(operationId = operationId).apply(configure).build()
        links.add(link)
    }

    /**
     * Adds a new [ApiHeader] instance to the cache, ensuring that the header name is unique
     *
     * @param header The [ApiHeader] instance to add to the cache.
     * @throws KopapiException If an [ApiHeader] with the same name already exists.
     */
    private fun addHeader(header: ApiHeader) {
        if (headers.any { it.name == header.name }) {
            throw KopapiException("Header with name '${header.name}' already exists within the same response.")
        }
        headers.add(header)
    }
}
