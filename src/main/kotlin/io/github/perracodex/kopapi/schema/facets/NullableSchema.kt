/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.schema.facets

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.types.ApiType

/**
 * Represents a schema marked as nullable type.
 *
 * Nullable types are represented as a composition of the base schema and a null type.
 *
 * @property description Description of the property.
 * @property minLength Property-level minimum length (if applicable).
 * @property maxLength Property-level maximum length (if applicable).
 * @property minimum Property-level minimum value (if applicable).
 * @property maximum Property-level maximum value (if applicable).
 * @property exclusiveMinimum Property-level exclusive minimum value (if applicable).
 * @property exclusiveMaximum Property-level exclusive maximum value (if applicable).
 * @property multipleOf Property-level multipleOf value (if applicable).
 * @property defaultValue Default value of the property.
 * @property anyOf The list that includes the base schema (with stripped attributes) and null type.
 */
internal data class NullableSchema private constructor(
    @JsonProperty("description") val description: String? = null,
    @JsonProperty("minLength") val minLength: Int? = null,
    @JsonProperty("maxLength") val maxLength: Int? = null,
    @JsonProperty("minimum") val minimum: Number? = null,
    @JsonProperty("maximum") val maximum: Number? = null,
    @JsonProperty("exclusiveMinimum") val exclusiveMinimum: Number? = null,
    @JsonProperty("exclusiveMaximum") val exclusiveMaximum: Number? = null,
    @JsonProperty("multipleOf") val multipleOf: Number? = null,
    @JsonProperty("default") val defaultValue: Any? = null,
    @JsonProperty("anyOf") val anyOf: List<Any>,
) : ISchemaFacet {

    companion object {
        /**
         * Factory method to create a [NullableSchema] instance.
         *
         * @param schemaProperty The [SchemaProperty] holding the schema to be marked as nullable.
         * @return A new instance of [NullableSchema].
         */
        fun create(
            schemaProperty: SchemaProperty,
        ): NullableSchema {
            return NullableSchema(
                description = schemaProperty.schema.description,
                minLength = schemaProperty.schema.minLength,
                maxLength = schemaProperty.schema.maxLength,
                minimum = schemaProperty.schema.minimum,
                maximum = schemaProperty.schema.maximum,
                exclusiveMinimum = schemaProperty.schema.exclusiveMinimum,
                exclusiveMaximum = schemaProperty.schema.exclusiveMaximum,
                multipleOf = schemaProperty.schema.multipleOf,
                defaultValue = schemaProperty.schema.defaultValue,
                anyOf = listOf(
                    stripAttributes(schema = schemaProperty.schema),
                    mapOf("type" to ApiType.NULL())
                ),
            )
        }

        /**
         * Strips the attributes from the schema. Required when composing a nullable schema
         * as these attributes must be placed outside the nullable composition.
         *
         * @param schema The original [ElementSchema].
         * @return The schema with the attributes stripped.
         */
        private fun stripAttributes(schema: ElementSchema): ElementSchema {
            return when (schema) {
                is ElementSchema.Object -> schema.copy(
                    description = null
                )

                is ElementSchema.AdditionalProperties -> schema.copy(
                    description = null,
                    minLength = null,
                    maxLength = null,
                    minimum = null,
                    maximum = null,
                    exclusiveMinimum = null,
                    exclusiveMaximum = null,
                    multipleOf = null,
                    defaultValue = null
                )

                is ElementSchema.Array -> schema.copy(
                    description = null,
                    minLength = null,
                    maxLength = null,
                    minimum = null,
                    maximum = null,
                    exclusiveMinimum = null,
                    exclusiveMaximum = null,
                    multipleOf = null,
                    defaultValue = null
                )

                is ElementSchema.Enum -> schema.copy(
                    description = null,
                    minLength = null,
                    maxLength = null,
                    minimum = null,
                    maximum = null,
                    exclusiveMinimum = null,
                    exclusiveMaximum = null,
                    multipleOf = null,
                    defaultValue = null
                )

                is ElementSchema.Primitive -> schema.copy(
                    description = null,
                    minLength = null,
                    maxLength = null,
                    minimum = null,
                    maximum = null,
                    exclusiveMinimum = null,
                    exclusiveMaximum = null,
                    multipleOf = null,
                    defaultValue = null
                )

                is ElementSchema.Reference -> schema.copy(
                    description = null,
                    minLength = null,
                    maxLength = null,
                    minimum = null,
                    maximum = null,
                    exclusiveMinimum = null,
                    exclusiveMaximum = null,
                    multipleOf = null,
                    defaultValue = null
                )
            }
        }
    }
}
