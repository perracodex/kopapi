/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.elements

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.dsl.operation.builders.ApiOperationBuilder
import io.github.perracodex.kopapi.types.Composition
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.http.*
import kotlin.reflect.KType

/**
 * Represents the metadata of an API response.
 *
 * @property types The [KType] objects representing the types of the response content.
 * @property status The [HttpStatusCode] code associated with this response.
 * @property description A human-readable description of the response, providing context about what this response signifies.
 * @property descriptionSet A set of descriptions to ensure uniqueness when merging responses.
 * @property content A map of [ContentType] to [ContentSchema] for the content in the response.
 * @property composition The composition of the response. Only meaningful if multiple types are provided.
 * @property headers A list of [ApiHeader] objects representing the headers that may be included in the response.
 * @property links A list of [ApiLink] objects representing possible links to other operations.
 *
 * @see [ApiOperationBuilder.response]
 * @see [ApiHeader]
 * @see [ApiLink]
 */
@PublishedApi
internal data class ApiResponse(
    @JsonIgnore
    val types: List<KType>?,
    @JsonIgnore
    val status: HttpStatusCode,
    @JsonProperty("description")
    val description: String?,
    @JsonIgnore
    val descriptionSet: MutableSet<String> = LinkedHashSet(),
    @JsonProperty("content")
    val content: MutableMap<ContentType, List<ContentSchema>>?,
    @JsonIgnore
    val composition: Composition?,
    @JsonProperty("headers")
    val headers: Set<ApiHeader>?,
    @JsonProperty("links")
    val links: Set<ApiLink>?
) {

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
        val combinedLinks: Set<ApiLink>? = links?.plus(elements = other.links ?: emptySet()) ?: other.links
        val combinedTypes: List<KType> = ((types ?: emptyList()) + (other.types ?: emptyList())).distinct()
        val precedenceComposition: Composition? = other.composition ?: composition

        // Combine content type maps.
        val combinedContent: MutableMap<ContentType, List<ContentSchema>>? = if (content != null || other.content != null) {
            mergeContentTypes(first = content, second = other.content)
        } else {
            null
        }

        // Combine descriptions and eliminate duplicates.
        val combinedDescription: String? = other.description.trimOrNull()?.let {
            descriptionSet.add(it)
            descriptionSet.joinToString(separator = "\n")
        } ?: description

        return copy(
            types = combinedTypes,
            description = combinedDescription,
            descriptionSet = descriptionSet,
            content = combinedContent,
            composition = precedenceComposition,
            headers = combinedHeaders,
            links = combinedLinks
        )
    }

    /**
     * Merges two maps of content types to their respective lists of content schemas.
     *
     * @param first The first content map.
     * @param second The second content map.
     * @return A merged content map with combined lists of content schemas for each content type.
     */
    private fun mergeContentTypes(
        first: Map<ContentType, List<ContentSchema>>?,
        second: Map<ContentType, List<ContentSchema>>?
    ): MutableMap<ContentType, List<ContentSchema>> {
        val combinedContent: MutableMap<ContentType, MutableList<ContentSchema>> = mutableMapOf()

        // Add all schemas from the first content map.
        first?.forEach { (type, schemas) ->
            val existingSchemas: MutableList<ContentSchema> = combinedContent.getOrPut(type) { mutableListOf() }
            existingSchemas.addAll(schemas)
        }

        // Add all schemas from the second content map and merge with existing if present.
        second?.forEach { (type, schemas) ->
            val existingSchemas: MutableList<ContentSchema> = combinedContent.getOrPut(type) { mutableListOf() }
            existingSchemas.addAll(schemas)
        }

        // Remove duplicate schemas per content type.
        combinedContent.forEach { (type, schemas) ->
            combinedContent[type] = schemas.distinctBy { it.type }.toMutableList()
        }

        // Convert MutableList to List before returning.
        return combinedContent.mapValues { it.value.toList() }.toMutableMap()
    }
}
