/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.schema.facet

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.composer.annotation.ComposerApi
import io.github.perracodex.kopapi.dsl.example.element.IExample
import io.github.perracodex.kopapi.type.ApiType

/**
 * Represents an object schema with a set of named properties.
 *
 * This is a component schema object aligned with the OpenAPI specification.
 *
 * @property type The API type of the schema as defined in the OpenAPI specification.
 * @property description A brief description of the schema.
 * @property defaultValue The default value of the schema.
 * @property examples A set of examples of the schema.
 * @property properties A map of property names to their corresponding OpenAPI schemas.
 * @property required A set of required property names.
 *
 * @see [ElementSchema.ObjectDescriptor]
 */
@ComposerApi
internal data class ObjectSchema(
    @field:JsonProperty("type") val type: ApiType = ApiType.OBJECT,
    @field:JsonProperty("description") val description: String?,
    @field:JsonProperty("default") val defaultValue: Any?,
    @field:JsonProperty("examples") val examples: IExample?,
    @field:JsonProperty("properties") val properties: Map<String, ISchemaFacet>?,
    @field:JsonProperty("required") val required: MutableSet<String>?
) : ISchemaFacet
