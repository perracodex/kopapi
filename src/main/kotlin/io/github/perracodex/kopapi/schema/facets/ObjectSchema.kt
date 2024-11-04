/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.schema.facets

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.types.ApiType

/**
 * Represents an object schema with a set of named properties.
 *
 * This is a component schema object aligned with the OpenAPI specification.
 *
 * @property type The API type of the schema as defined in the OpenAPI specification.
 * @property description A brief description of the schema.
 * @property properties A map of property names to their corresponding OpenAPI schemas.
 * @property required A set of required property names.
 *
 * @see [ElementSchema.ObjectDescriptor]
 */
internal data class ObjectSchema(
    @JsonProperty("type") val type: ApiType = ApiType.OBJECT,
    @JsonProperty("description") val description: String?,
    @JsonProperty("properties") val properties: Map<String, ISchemaFacet>?,
    @JsonProperty("required") val required: MutableSet<String>?
) : ISchemaFacet
