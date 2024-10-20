/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.elements

import io.github.perracodex.kopapi.dsl.operation.builders.ApiOperationBuilder
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.types.Composition
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.http.*
import kotlin.reflect.KType

/**
 * Represents the metadata of an API response.
 *
 * @property status The [HttpStatusCode] code associated with this response.
 * @property headers A list of [ApiHeader] objects representing the headers that may be included in the response.
 * @property description A human-readable description of the response, providing context about what this response signifies.
 * @property descriptionSet A set of descriptions to ensure uniqueness when merging responses.
 * @property composition The composition of the response. Only meaningful if multiple types are provided.
 * @property content  A map of [KType] to a set of [ContentType] that this response may return.
 * @property links A list of [ApiLink] objects representing possible links to other operations.
 *
 * @see [ApiOperationBuilder.response]
 * @see [ApiHeader]
 * @see [ApiLink]
 */
@PublishedApi
internal data class ApiResponse(
    val status: HttpStatusCode,
    val description: String?,
    val descriptionSet: MutableSet<String> = LinkedHashSet(),
    val headers: Set<ApiHeader>?,
    val composition: Composition?,
    val content: Map<KType, Set<ContentType>>?,
    val links: Set<ApiLink>?,
) {
    init {
        content?.forEach { (_, contentTypes) ->
            if (contentTypes.isEmpty()) {
                throw KopapiException("At least one ContentType must be associated with each KType.")
            }
        }
    }

    /**
     * Merges this `ApiResponse` with another `ApiResponse` to combine their properties.
     * If the composition of the other response is specified, it takes precedence;
     * otherwise, the original composition is retained.
     *
     * #### Merging Rules
     * - Headers from both responses are combined, eliminating duplicates.
     * - Links from both responses are combined, eliminating duplicates.
     * - Types from both responses are combined, with duplicates removed to ensure uniqueness.
     * - Descriptions from both responses are concatenated, separated by a newline. Duplicate descriptions are eliminated.
     * - Composition from the other response takes precedence if it is non-null;
     *   otherwise, the composition of this response remains unchanged.
     *
     * @param other The other ApiResponse to merge with this one.
     * @return A new `ApiResponse` instance that represents the merged result.
     */
    fun mergeWith(other: ApiResponse): ApiResponse {
        val combinedHeaders: Set<ApiHeader>? = headers?.plus(elements = other.headers ?: emptySet()) ?: other.headers
        val precedenceComposition: Composition? = other.composition ?: composition
        val combinedContent: Map<KType, Set<ContentType>> = mergeContent(first = content, second = other.content)
        val combinedLinks: Set<ApiLink>? = links?.plus(elements = other.links ?: emptySet()) ?: other.links

        // Combine descriptions and eliminate duplicates.
        val combinedDescription: String? = other.description.trimOrNull()?.let {
            descriptionSet.add(it)
            descriptionSet.joinToString(separator = "\n")
        } ?: description

        // Create a newly combined ApiResponse instance.
        return copy(
            description = combinedDescription,
            descriptionSet = descriptionSet,
            headers = combinedHeaders,
            composition = precedenceComposition,
            content = combinedContent,
            links = combinedLinks
        )
    }

    /**
     * Merges two maps of KType to sets of content types, ensuring no duplicates.
     *
     * @param first The first map of KType to content types.
     * @param second The second map of KType to content types.
     * @return A merged map of KType to sets of content types.
     */
    private fun mergeContent(
        first: Map<KType, Set<ContentType>>?,
        second: Map<KType, Set<ContentType>>?
    ): Map<KType, Set<ContentType>> {
        val result: MutableMap<KType, MutableSet<ContentType>> = mutableMapOf()

        first?.forEach { (kType, contentTypes) ->
            result.getOrPut(kType) { mutableSetOf() }.addAll(contentTypes)
        }

        second?.forEach { (kType, contentTypes) ->
            result.getOrPut(kType) { mutableSetOf() }.addAll(contentTypes)
        }

        return result.mapValues { it.value.toSet() }
    }
}
