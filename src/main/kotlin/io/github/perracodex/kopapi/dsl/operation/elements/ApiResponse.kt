/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.elements

import io.github.perracodex.kopapi.dsl.operation.builders.operation.ApiOperationBuilder
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
 * @property content A map of [ContentType] to a set of [KType] that this response may return.
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
    val content: Map<ContentType, Set<KType>>?,
    val links: Set<ApiLink>?,
) {
    init {
        content?.forEach { (_, types) ->
            if (types.isEmpty()) {
                throw KopapiException("At least one Type must be associated with each ContentType.")
            }
        }
    }

    /**
     * Merges this `ApiResponse` with another `ApiResponse` to combine their properties.
     *
     * #### Merging Rules:
     * - Headers from both responses are combined, eliminating duplicates.
     * - Links from both responses are combined, eliminating duplicates.
     * - Types from both responses are combined per `ContentType`, ensuring uniqueness.
     * - Descriptions from both responses are concatenated, separated by a newline. Duplicate descriptions are eliminated.
     * - For each `ContentType`, if a composition is defined in both responses, the composition from `other` takes precedence.
     * - If only one response defines a composition for a `ContentType`, that composition is the one retained.
     * - If neither response defines a composition for a `ContentType` with more than one type,
     *   the default composition (`Composition.ANY_OF`) is used.
     *
     * @param other The other `ApiResponse` to merge with this one.
     * @return A new `ApiResponse` instance that represents the merged result.
     */
    fun mergeWith(other: ApiResponse): ApiResponse {
        val combinedHeaders: Set<ApiHeader>? = headers?.plus(elements = other.headers ?: emptySet()) ?: other.headers
        val combinedContent: Map<ContentType, Set<KType>> = mergeContent(current = content, merging = other.content)
        val combinedLinks: Set<ApiLink>? = links?.plus(elements = other.links ?: emptySet()) ?: other.links

        // Combine descriptions and eliminate duplicates.
        val combinedDescription: String? = other.description.trimOrNull()?.let {
            descriptionSet.add(it)
            descriptionSet.joinToString(separator = "\n")
        } ?: description


        // The merging composition takes precedence, if it is defined.
        val newComposition: Composition? = other.composition ?: composition

        // Create a newly combined ApiResponse instance.
        return copy(
            description = combinedDescription,
            descriptionSet = descriptionSet,
            headers = combinedHeaders,
            composition = newComposition,
            content = combinedContent,
            links = combinedLinks
        )
    }

    /**
     * Merges two maps of KType to sets of content types, ensuring no duplicates.
     *
     * @param current The current map of `ContentType` to `KType` objects.
     * @param merging The merging map of `ContentType` to `KType` objects.
     * @return A merged map of KType to sets of content types.
     */
    private fun mergeContent(
        current: Map<ContentType, Set<KType>>?,
        merging: Map<ContentType, Set<KType>>?
    ): Map<ContentType, Set<KType>> {
        val result: MutableMap<ContentType, MutableSet<KType>> = mutableMapOf()

        current?.forEach { (contentType, types) ->
            result.getOrPut(contentType) { mutableSetOf() }.addAll(types)
        }

        merging?.forEach { (contentType, types) ->
            result.getOrPut(contentType) { mutableSetOf() }.addAll(types)
        }

        return result.mapValues {
            it.value.toSet()
        }.toSortedMap(
            compareBy(
                { it.contentType },
                { it.contentSubtype }
            )
        )
    }

    companion object {
        /**
         * Builds an `ApiResponse` instance with no content.
         *
         * @return A new `ApiResponse` instance with no content.
         */
        fun buildWithNoContent(): ApiResponse {
            return ApiResponse(
                status = HttpStatusCode.NoContent,
                description = HttpStatusCode.NoContent.description,
                headers = null,
                composition = null,
                content = null,
                links = null
            )
        }
    }
}
