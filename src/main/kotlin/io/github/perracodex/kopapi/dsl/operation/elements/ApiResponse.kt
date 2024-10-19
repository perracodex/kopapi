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
    var content: MutableMap<ContentType, ContentSchema?>?,
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

        // Combine descriptions and eliminate duplicates.
        val combinedDescription: String? = other.description.trimOrNull()?.let {
            descriptionSet.add(it)
            descriptionSet.joinToString(separator = "\n")
        } ?: description

        return copy(
            description = combinedDescription,
            descriptionSet = descriptionSet,
            headers = combinedHeaders,
            links = combinedLinks,
            types = combinedTypes,
            composition = precedenceComposition
        )
    }
}
