/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.schema.facets

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.types.ApiType
import io.github.perracodex.kopapi.types.DefaultValue
import io.github.perracodex.kopapi.utils.trimOrNull

/**
 * Represents a schema marked as nullable type.
 *
 * Nullable types are represented as a composition of the base schema and a null type.
 *
 * @property description Description of the property.
 * @property defaultValue Default value of the property.
 * @property anyOf The list that includes the base schema (with stripped attributes) and null type.
 */
internal data class NullableSchema private constructor(
    @JsonProperty("description") val description: String? = null,
    @JsonProperty("default") val defaultValue: Any? = null,
    @JsonProperty("anyOf") val anyOf: List<Any>
) : ISchemaFacet {

    companion object {
        /**
         * Factory method to create a [NullableSchema] instance.
         *
         * @param schemaProperty The [SchemaProperty] holding the schema to be marked as nullable.
         * @return A new instance of [NullableSchema].
         */
        fun create(schemaProperty: SchemaProperty): NullableSchema {
            val decomposedSchema: DecomposedSchema = decomposeSchema(schema = schemaProperty.schema)

            return NullableSchema(
                description = decomposedSchema.description,
                defaultValue = decomposedSchema.defaultValue,
                anyOf = listOf(
                    decomposedSchema.strippedSchema,
                    mapOf("type" to ApiType.NULL())
                ),
            )
        }

        /**
         * Decomposes the provided schema into its core components:
         * - The schema structure itself, with general attributes like `description` and `default` removed.
         * - The `description` and `default` attributes as standalone elements, if they are present.
         *
         * This decomposition is required for creating nullable schemas using `anyOf`.
         * by isolating general attributes from the main schema, this method allows the `anyOf`
         * composition to retain the primary attributes (like `description` and `default`) at
         * the top level, while type-specific constraints remain embedded within the base type
         * definition inside `anyOf`.
         *
         * @param schema The original [ElementSchema] to decompose.
         * @return A [DecomposedSchema] containing the decomposed parts of the schema.
         */
        private fun decomposeSchema(schema: ElementSchema): DecomposedSchema {
            val strippedSchema: ElementSchema = when (schema) {
                is ElementSchema.ObjectDescriptor ->
                    schema.copy(description = null)

                is ElementSchema.AdditionalProperties ->
                    schema.copy(description = null, defaultValue = null)

                is ElementSchema.Array ->
                    schema.copy(description = null, defaultValue = null)

                is ElementSchema.Enum ->
                    schema.copy(description = null, defaultValue = null)

                is ElementSchema.Primitive ->
                    schema.copy(description = null, defaultValue = null)

                is ElementSchema.Reference ->
                    schema.copy(description = null, defaultValue = null)
            }

            val defaultValue: Any? = when (schema) {
                is ElementSchema.ObjectDescriptor -> null
                is ElementSchema.AdditionalProperties -> schema.defaultValue
                is ElementSchema.Array -> schema.defaultValue
                is ElementSchema.Enum -> schema.defaultValue
                is ElementSchema.Primitive -> schema.defaultValue
                is ElementSchema.Reference -> schema.defaultValue
            }

            return DecomposedSchema(
                strippedSchema = strippedSchema,
                description = schema.description.trimOrNull(),
                defaultValue = (defaultValue as? DefaultValue)?.toValue() ?: defaultValue
            )
        }
    }
}

/**
 * Represents the decomposition of an [ElementSchema].
 *
 * @property strippedSchema The schema with specific attributes removed.
 * @property description The description associated with the schema, if any.
 * @property defaultValue The default value associated with the schema, if any.
 */
private data class DecomposedSchema(
    val strippedSchema: ElementSchema,
    val description: String?,
    val defaultValue: Any?
)
