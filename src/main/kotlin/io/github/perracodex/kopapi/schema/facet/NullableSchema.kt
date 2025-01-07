/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.schema.facet

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.composer.annotation.ComposerApi
import io.github.perracodex.kopapi.dsl.example.element.IExample
import io.github.perracodex.kopapi.type.ApiType
import io.github.perracodex.kopapi.type.DefaultValue
import io.github.perracodex.kopapi.util.trimOrNull

/**
 * Represents a schema marked as nullable type.
 *
 * Nullable types are represented as a composition of the base schema and a null type.
 *
 * @property description Description of the schema.
 * @property defaultValue Default value of the schema.
 * @property examples Examples for the schema.
 * @property type The list that includes the base schema type and null type. (For primitive types).
 * @property format The format of the schema type, meaningful only for primitive types.
 * @property oneOf The list that includes the base schema (with stripped attributes) and null type. (For complex types).
 */
@ComposerApi
internal data class NullableSchema private constructor(
    @JsonProperty("description") val description: String? = null,
    @JsonProperty("default") val defaultValue: Any? = null,
    @JsonProperty("examples") val examples: IExample? = null,
    @JsonProperty("type") val type: List<String>? = null,
    @JsonProperty("format") val format: String? = null,
    @JsonProperty("oneOf") val oneOf: List<Any>? = null
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

            // Determine if the schema is a Primitive type to use less verbose syntax.
            return if (decomposedSchema.strippedSchema is ElementSchema.Primitive) {
                NullableSchema(
                    description = decomposedSchema.description.trimOrNull(),
                    defaultValue = decomposedSchema.defaultValue,
                    examples = decomposedSchema.examples,
                    type = listOf(decomposedSchema.strippedSchema.schemaType.toString(), ApiType.NULL()),
                    format = decomposedSchema.strippedSchema.format.trimOrNull()
                )
            } else {
                NullableSchema(
                    description = decomposedSchema.description.trimOrNull(),
                    defaultValue = decomposedSchema.defaultValue,
                    examples = decomposedSchema.examples,
                    oneOf = listOf(
                        decomposedSchema.strippedSchema,
                        mapOf("type" to ApiType.NULL())
                    )
                )
            }
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
                    schema.copy(description = null, defaultValue = null, examples = null)

                is ElementSchema.AdditionalProperties ->
                    schema.copy(description = null, defaultValue = null, examples = null)

                is ElementSchema.Array ->
                    schema.copy(description = null, defaultValue = null, examples = null)

                is ElementSchema.Enum ->
                    schema.copy(description = null, defaultValue = null, examples = null)

                is ElementSchema.Primitive ->
                    schema.copy(description = null, defaultValue = null, examples = null)

                is ElementSchema.Reference ->
                    schema.copy(description = null, defaultValue = null, examples = null)
            }

            val defaultValue: Any? = schema.defaultValue
            val examples: IExample? = schema.examples

            return DecomposedSchema(
                strippedSchema = strippedSchema,
                description = schema.description.trimOrNull(),
                defaultValue = (defaultValue as? DefaultValue)?.toValue() ?: defaultValue,
                examples = examples
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
 * @property examples The examples associated with the schema, if any.
 */
private data class DecomposedSchema(
    val strippedSchema: ElementSchema,
    val description: String?,
    val defaultValue: Any?,
    val examples: IExample?
)
