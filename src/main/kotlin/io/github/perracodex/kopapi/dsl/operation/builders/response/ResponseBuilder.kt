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
 * @property contentType A set of [ContentType] items. If not provided, the response will default to application/json.
 * @property composition The composition of the response. Only meaningful if multiple types are provided.
 *
 * @see [ApiOperationBuilder.response]
 */
public class ResponseBuilder {
    public var description: String by MultilineString()
    public var contentType: Set<ContentType>? = null
    public var composition: Composition? = null
    private val headers: MutableSet<ApiHeader> = mutableSetOf()
    private val links: MutableSet<ApiLink> = mutableSetOf()

    @PublishedApi
    internal val types: MutableList<KType> = mutableListOf()

    /**
     * Registers a new type for the response.
     *
     * #### Sample Usage
     * ```
     * response<Employee>(status = HttpStatusCode.OK) {
     *    type<Int>()
     *    type<Array<Data>>()
     * }
     * ```
     *
     * @param T The type of the response.
     */
    public inline fun <reified T : Any> type() {
        types.add(typeOf<T>())
    }

    @PublishedApi
    internal fun build(status: HttpStatusCode, type: KType?): ApiResponse {
        // Combine the primary type with any additional types added via type<T>().
        val allTypes: List<KType> = type?.let {
            listOf(it) + types
        }?.distinct() ?: types

        // If there are multiple types and no composition is set, default to ANY_OF.
        if (allTypes.size > 1 && composition == null) {
            composition = Composition.ANY_OF
        }

        // Handle content types and composition logic based on types.
        val finalContentType: Set<ContentType>? = when {
            allTypes.isEmpty() -> {
                composition = null
                null
            }

            contentType == null -> {
                // Default to application/json if no contentType provided.
                setOf(ContentType.Application.Json)
            }

            else -> contentType
        }

        // Create the map of types to content types, if any.
        val typeToContentMap: Map<KType, Set<ContentType>>? = if (allTypes.isNotEmpty()) {
            allTypes.associateWith { finalContentType ?: emptySet() }
        } else {
            null
        }

        // Return the constructed ApiResponse instance.
        return ApiResponse(
            status = status,
            description = description.trimOrNull(),
            headers = headers.takeIf { it.isNotEmpty() },
            composition = composition,
            types = typeToContentMap,
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
