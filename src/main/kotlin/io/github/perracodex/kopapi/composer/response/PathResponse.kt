/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.composer.OpenAPiSchema
import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.github.perracodex.kopapi.dsl.operation.elements.ApiHeader
import io.github.perracodex.kopapi.dsl.operation.elements.ApiLink
import io.ktor.http.*

/**
 * Represents the metadata of an API response.
 *
 * @property description A human-readable description of the response, providing context about what this response signifies.
 * @property headers A list of [ApiHeader] objects representing the headers that may be included in the response.
 * @property content A map of [ContentType] to [OpenAPiSchema.ContentSchema], or `null` if dealing with a response that has no content.
 * @property links A list of [ApiLink] objects representing possible links to other operations.
 */
@ComposerAPI
internal data class PathResponse(
    @JsonProperty("description")
    val description: String?,
    @JsonProperty("headers")
    val headers: Set<ApiHeader>?,
    @JsonProperty("content")
    var content: Map<ContentType, OpenAPiSchema.ContentSchema>?,
    @JsonProperty("links")
    val links: Set<ApiLink>?
)
