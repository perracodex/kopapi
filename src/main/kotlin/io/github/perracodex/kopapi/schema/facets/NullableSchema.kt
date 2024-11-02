/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.schema.facets

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.attribute.ParsedAttributes
import io.github.perracodex.kopapi.types.ApiType
import io.github.perracodex.kopapi.types.DefaultValue

/**
 * Represents a schema marked as nullable type.
 *
 * Nullable types are represented as a composition of the base schema and a null type.
 *
 * @property description Description of the property.
 * @property defaultValue Default value of the property.
 * @property minLength Property-level minimum length (if applicable).
 * @property maxLength Property-level maximum length (if applicable).
 * @property pattern Property-level pattern (if applicable).
 * @property minimum Property-level minimum value (if applicable).
 * @property maximum Property-level maximum value (if applicable).
 * @property exclusiveMinimum Property-level exclusive minimum value (if applicable).
 * @property exclusiveMaximum Property-level exclusive maximum value (if applicable).
 * @property multipleOf Property-level multipleOf value (if applicable).
 * @property minItems Property-level minimum items (if applicable).
 * @property maxItems Property-level maximum items (if applicable).
 * @property uniqueItems Property-level unique items (if applicable).
 * @property anyOf The list that includes the base schema (with stripped attributes) and null type.
 */
internal data class NullableSchema private constructor(
    @JsonProperty("description") val description: String? = null,
    @JsonProperty("default") val defaultValue: Any? = null,
    @JsonProperty("minLength") val minLength: Int? = null,
    @JsonProperty("maxLength") val maxLength: Int? = null,
    @JsonProperty("pattern") val pattern: String? = null,
    @JsonProperty("minimum") val minimum: Number? = null,
    @JsonProperty("maximum") val maximum: Number? = null,
    @JsonProperty("exclusiveMinimum") val exclusiveMinimum: Number? = null,
    @JsonProperty("exclusiveMaximum") val exclusiveMaximum: Number? = null,
    @JsonProperty("multipleOf") val multipleOf: Number? = null,
    @JsonProperty("minItems") val minItems: Int? = null,
    @JsonProperty("maxItems") val maxItems: Int? = null,
    @JsonProperty("uniqueItems") val uniqueItems: Boolean? = null,
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
                description = decomposedSchema.attributes.description,
                minLength = decomposedSchema.attributes.minLength,
                maxLength = decomposedSchema.attributes.maxLength,
                pattern = decomposedSchema.attributes.pattern,
                minimum = decomposedSchema.attributes.minimum,
                maximum = decomposedSchema.attributes.maximum,
                exclusiveMinimum = decomposedSchema.attributes.exclusiveMinimum,
                exclusiveMaximum = decomposedSchema.attributes.exclusiveMaximum,
                multipleOf = decomposedSchema.attributes.multipleOf,
                defaultValue = decomposedSchema.defaultValue,
                anyOf = listOf(
                    decomposedSchema.strippedSchema,
                    mapOf("type" to ApiType.NULL())
                ),
            )
        }

        /**
         * Decomposes the provided schema into its core components:
         *  - the schema itself with certain attributes removed,
         *  - the extracted attributes,
         *  - and any default value.
         *
         * This decomposition is necessary when creating a nullable schema,
         * allowing the attributes to be handled separately from the schema's core structure.
         *
         * @param schema The original [ElementSchema] to decompose.
         * @return A [DecomposedSchema] containing the decomposed parts of the schema.
         */
        private fun decomposeSchema(schema: ElementSchema): DecomposedSchema {
            val attributes: ParsedAttributes = getAttributes(schema = schema)

            val strippedSchema: ElementSchema = when (schema) {
                is ElementSchema.Object ->
                    ElementSchema.Object(objectProperties = schema.objectProperties)

                is ElementSchema.AdditionalProperties ->
                    ElementSchema.AdditionalProperties(additionalProperties = schema.additionalProperties)

                is ElementSchema.Array ->
                    ElementSchema.Array(items = schema.items)

                is ElementSchema.Enum ->
                    ElementSchema.Enum(values = schema.values)

                is ElementSchema.Primitive ->
                    ElementSchema.Primitive(schemaType = schema.schemaType, format = schema.format)

                is ElementSchema.Reference ->
                    ElementSchema.Reference(schemaName = schema.schemaName)
            }

            val defaultValue: Any? = when (schema) {
                is ElementSchema.Object -> null
                is ElementSchema.AdditionalProperties -> schema.defaultValue
                is ElementSchema.Array -> schema.defaultValue
                is ElementSchema.Enum -> schema.defaultValue
                is ElementSchema.Primitive -> schema.defaultValue
                is ElementSchema.Reference -> schema.defaultValue
            }

            return DecomposedSchema(
                strippedSchema = strippedSchema,
                attributes = attributes,
                defaultValue = (defaultValue as? DefaultValue)?.toValue() ?: defaultValue
            )
        }

        /**
         * Maps the attributes from the schema to a [ParsedAttributes] instance.
         */
        private fun getAttributes(schema: ElementSchema): ParsedAttributes {
            return when (schema) {
                is ElementSchema.Object -> {
                    ParsedAttributes(
                        description = schema.description,
                    )
                }

                is ElementSchema.AdditionalProperties -> {
                    ParsedAttributes(
                        description = schema.description,
                    )
                }

                is ElementSchema.Array -> {
                    ParsedAttributes(
                        description = schema.description,
                        minItems = schema.minItems,
                        maxItems = schema.maxItems,
                        uniqueItems = schema.uniqueItems,
                    )
                }

                is ElementSchema.Enum -> {
                    ParsedAttributes(
                        description = schema.description,
                    )
                }

                is ElementSchema.Primitive -> {
                    ParsedAttributes(
                        description = schema.description,
                        minLength = schema.minLength,
                        maxLength = schema.maxLength,
                        minimum = schema.minimum,
                        maximum = schema.maximum,
                        exclusiveMinimum = schema.exclusiveMinimum,
                        exclusiveMaximum = schema.exclusiveMaximum,
                        multipleOf = schema.multipleOf,
                    )
                }

                is ElementSchema.Reference -> {
                    ParsedAttributes(
                        description = schema.description,
                    )
                }
            }
        }
    }
}

/**
 * Represents the decomposition of an [ElementSchema] into its core parts.
 *
 * @property strippedSchema The schema with specific attributes removed.
 * @property attributes Attributes extracted from the original schema.
 * @property defaultValue The default value associated with the schema, if any.
 */
private data class DecomposedSchema(
    val strippedSchema: ElementSchema,
    val attributes: ParsedAttributes,
    val defaultValue: Any?
)
