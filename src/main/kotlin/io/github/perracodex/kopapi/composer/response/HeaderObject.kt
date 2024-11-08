/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.composer.annotation.ComposerApi
import io.github.perracodex.kopapi.schema.facets.ElementSchema

/**
 * Represents a response header adhering to the OpenAPI specification.
 *
 * @param description A description of the header.
 * @param required Indicates whether the header is mandatory.
 * @property explode Indicates if arrays and objects are serialized as a single comma-separated header. Has no effect on other types.
 * @param schema The schema of the header.
 * @param deprecated Indicates whether the response is deprecated and should be avoided.
 */
@ComposerApi
internal data class HeaderObject(
    @JsonProperty("description") val description: String?,
    @JsonProperty("required") val required: Boolean,
    @JsonProperty("explode") val explode: Boolean?,
    @JsonProperty("schema") val schema: ElementSchema,
    @JsonProperty("deprecated") val deprecated: Boolean?
)
