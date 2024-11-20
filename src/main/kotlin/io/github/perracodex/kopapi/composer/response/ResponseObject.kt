/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.composer.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.composer.annotation.ComposerApi
import io.github.perracodex.kopapi.composer.header.HeaderObject
import io.github.perracodex.kopapi.dsl.operation.element.ApiLink
import io.github.perracodex.kopapi.schema.OpenApiSchema
import io.ktor.http.*
import java.util.*

/**
 * Represents the metadata of an API response.
 *
 * @property description A human-readable description of the response, providing context about what this response signifies.
 * @property headers A list of [HeaderObject] instances representing the headers that may be included in the response.
 * @property content A map of [ContentType] to [OpenApiSchema.ContentSchema], or `null` if dealing with a response that has no content.
 * @property links A map of [ApiLink] objects representing possible links to other operations.
 */
@ComposerApi
internal data class ResponseObject(
    @JsonProperty("description") val description: String?,
    @JsonProperty("headers") val headers: Map<String, HeaderObject>?,
    @JsonProperty("content") var content: Map<ContentType, OpenApiSchema.ContentSchema>?,
    @JsonProperty("links") val links: SortedMap<String, ApiLink>?
)
