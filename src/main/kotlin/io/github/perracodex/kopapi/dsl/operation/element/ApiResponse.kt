/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.dsl.operation.element

import io.github.perracodex.kopapi.dsl.example.element.IExample
import io.github.perracodex.kopapi.dsl.operation.builder.operation.ApiOperationBuilder
import io.github.perracodex.kopapi.dsl.schema.element.ApiSchemaAttributes
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.type.Composition
import io.github.perracodex.kopapi.util.orNull
import io.github.perracodex.kopapi.util.trimOrNull
import io.ktor.http.*
import kotlin.reflect.KType

/**
 * Represents the metadata of an API response.
 *
 * @property status The [HttpStatusCode] code associated with this response. Null for default responses.
 * @property headers A map of [ApiHeader] objects representing the headers that may be included in the response.
 * @property description A human-readable description of the response, providing context about what this response signifies.
 * @property descriptionSet A set of descriptions to ensure uniqueness when merging responses.
 * @property composition The composition of the response. Only meaningful if multiple types are provided.
 * @property content A map of [ContentType] to a set of [KType] that this response may return.
 * @property links A map of [ApiLink] objects representing possible links to other operations.
 * @property examples Examples be used for documentation purposes.
 *
 * @see [ApiOperationBuilder.response]
 * @see [ApiHeader]
 * @see [ApiLink]
 */
@PublishedApi
internal data class ApiResponse(
    val status: HttpStatusCode?,
    val description: String?,
    val descriptionSet: MutableSet<String> = LinkedHashSet(),
    val headers: Map<String, ApiHeader>?,
    val composition: Composition?,
    val content: Map<ContentType, Set<TypeDetails>>?,
    val links: Map<String, ApiLink>?,
    val examples: IExample?
) {
    @PublishedApi
    internal data class TypeDetails(
        val type: KType,
        val schemaAttributes: ApiSchemaAttributes?
    )

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
     * #### Merging Rules
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
        val combinedContent: Map<ContentType, Set<TypeDetails>> = mergeContent(
            current = content,
            merging = other.content
        )

        // Combine descriptions and eliminate duplicates.
        val combinedDescription: String? = other.description.trimOrNull()?.let {
            descriptionSet.add(it)
            descriptionSet.joinToString(separator = "\n")
        } ?: description

        // Merge headers, ensuring that each header name is unique.
        val combinedHeaders: Map<String, ApiHeader>? = mergeHeaders(current = headers, merging = other.headers)

        // Merge links, ensuring that each link name is unique.
        val combinedLinks: Map<String, ApiLink>? = mergeLinks(current = links, merging = other.links)

        // The merging composition takes precedence, if it is defined.
        val newComposition: Composition? = other.composition ?: composition

        // Create a newly combined ApiResponse instance.
        return copy(
            description = combinedDescription,
            descriptionSet = descriptionSet,
            headers = combinedHeaders,
            composition = newComposition,
            content = combinedContent,
            links = combinedLinks?.orNull()
        )
    }

    /**
     * Merges two header maps, ensuring that each header name is unique.
     *
     * @param current The current map of header.
     * @param merging The other map of header to merge.
     * @return A new merged map of header.
     * @throws KopapiException If duplicate header names are detected.
     */
    private fun mergeHeaders(
        current: Map<String, ApiHeader>?,
        merging: Map<String, ApiHeader>?
    ): Map<String, ApiHeader>? {
        return when {
            current == null && merging == null -> null
            current == null -> merging
            merging == null -> current
            else -> {
                val merged: MutableMap<String, ApiHeader> = current.toMutableMap()
                merging.forEach { (name, header) ->
                    if (merged.containsKey(name)) {
                        throw KopapiException("Duplicate header name '$name' detected during response merge.")
                    }
                    merged[name] = header
                }
                merged
            }
        }
    }

    /**
     * Merges two link maps, ensuring that each link name is unique.
     *
     * @param current The current map of links.
     * @param merging The other map of links to merge.
     * @return A new merged map of links.
     * @throws KopapiException If duplicate link names are detected.
     */
    private fun mergeLinks(
        current: Map<String, ApiLink>?,
        merging: Map<String, ApiLink>?
    ): Map<String, ApiLink>? {
        return when {
            current == null && merging == null -> null
            current == null -> merging
            merging == null -> current
            else -> {
                val merged: MutableMap<String, ApiLink> = current.toMutableMap()
                merging.forEach { (name, link) ->
                    if (merged.containsKey(name)) {
                        throw KopapiException("Duplicate link name '$name' detected during response merge.")
                    }
                    merged[name] = link
                }
                merged
            }
        }
    }

    /**
     * Merges two maps of KType to sets of content types, ensuring no duplicates.
     *
     * @param current The current map of `ContentType` to `KType` objects.
     * @param merging The merging map of `ContentType` to `KType` objects.
     * @return A merged map of KType to sets of content types.
     */
    private fun mergeContent(
        current: Map<ContentType, Set<TypeDetails>>?,
        merging: Map<ContentType, Set<TypeDetails>>?
    ): Map<ContentType, Set<TypeDetails>> {
        val result: MutableMap<ContentType, MutableSet<TypeDetails>> = mutableMapOf()

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
                links = null,
                examples = null
            )
        }
    }
}
