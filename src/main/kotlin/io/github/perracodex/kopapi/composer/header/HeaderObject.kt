/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.composer.header

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.composer.annotation.ComposerApi
import io.github.perracodex.kopapi.schema.OpenApiSchema
import io.github.perracodex.kopapi.schema.facet.ElementSchema
import io.ktor.http.*

/**
 * Represents a response header adhering to the OpenAPI specification.
 *
 * @property description A description of the header.
 * @property required Indicates whether the header is mandatory.
 * @property explode Indicates whether arrays and objects are serialized as a single comma-separated header.
 * @property schema The schema of the header. Must be `null` if `content` is specified.
 * @property content [ContentType] when a specific media format is required. Must be `null` if `schema` is specified.
 * @property deprecated Indicates whether the response is deprecated and should be avoided.
 */
@ComposerApi
internal data class HeaderObject(
    @field:JsonProperty("description") val description: String?,
    @field:JsonProperty("required") val required: Boolean,
    @field:JsonProperty("explode") val explode: Boolean?,
    @field:JsonProperty("schema") val schema: ElementSchema?,
    @field:JsonProperty("content") var content: Map<ContentType, OpenApiSchema.ContentSchema>?,
    @field:JsonProperty("deprecated") val deprecated: Boolean?
)
