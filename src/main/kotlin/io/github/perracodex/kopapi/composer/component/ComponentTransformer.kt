/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.component

import io.github.perracodex.kopapi.schema.ElementSchema
import io.github.perracodex.kopapi.schema.SchemaProperty
import io.github.perracodex.kopapi.types.Composition

/**
 * Responsible for transforming [ElementSchema] instances into OpenAPI compatible map structures.
 */
internal object ComponentTransformer {
    /**
     * Converts an [ElementSchema] into a map with the provided name as the top-level key.
     *
     * @param name The key under which the schema will be stored.
     * @param elementSchema The schema object to convert.
     * @return A map with the schema name as the key.
     */
    fun transform(name: String, elementSchema: ElementSchema): Map<String, Any?> {
        return mapOf(
            name to transformElementSchema(elementSchema = elementSchema)
        )
    }

    /**
     * Converts an [ElementSchema] into a structured map based on the schema type.
     *
     * @param elementSchema The schema to be processed.
     * @return A map representing the schema's structure.
     */
    private fun transformElementSchema(elementSchema: ElementSchema): Map<String, Any?> {
        return when (elementSchema) {
            is ElementSchema.Object -> transformObject(schema = elementSchema)
            is ElementSchema.Primitive -> transformPrimitive(schema = elementSchema)
            is ElementSchema.Reference -> transformReference(schema = elementSchema)
            is ElementSchema.Enum -> transformEnum(schema = elementSchema)
            is ElementSchema.Array -> transformArray(schema = elementSchema)
            is ElementSchema.AdditionalProperties -> transformAdditionalProperties(schema = elementSchema)
        }
    }

    /**
     * Handles transforming object schemas, including processing their properties and required fields.
     *
     * @param schema The object schema to process.
     * @return A map containing the processed object schema.
     */
    private fun transformObject(schema: ElementSchema.Object): Map<String, Any?> {
        val result: MutableMap<String, Any?> = mutableMapOf(
            "type" to schema.schemaType()
        )

        val requiredFields: MutableList<String> = mutableListOf()

        val properties: Map<String, Map<String, Any?>> = schema.properties
            .mapNotNull { (name, property) ->
                if (property.isTransient) {
                    null // Skip if the property is transient.
                } else {
                    val transformedProperty: Map<String, Any?> = transformSchemaProperty(
                        property = property
                    )

                    // Add to required fields if marked as required.
                    if (property.isRequired) {
                        requiredFields.add(name)
                    }

                    name to transformedProperty
                }
            }.toMap()

        if (properties.isNotEmpty()) {
            result["properties"] = properties
        }

        // Add required fields if any were found.
        if (requiredFields.isNotEmpty()) {
            result["required"] = requiredFields
        }

        return result
    }

    /**
     * Processes primitive schemas, applying any format or constraints like length or range.
     *
     * @param schema The primitive schema to process.
     * @return A map representing the primitive schema.
     */
    private fun transformPrimitive(schema: ElementSchema.Primitive): Map<String, Any?> {
        val result: MutableMap<String, Any?> = mutableMapOf(
            "type" to schema.schemaType()
        )

        schema.format?.let { result["format"] = it }

        // Optional constraints.
        schema.minLength?.let { result["minLength"] = it }
        schema.maxLength?.let { result["maxLength"] = it }
        schema.minimum?.let { result["minimum"] = it }
        schema.maximum?.let { result["maximum"] = it }
        schema.exclusiveMinimum?.let { result["exclusiveMinimum"] = it }
        schema.exclusiveMaximum?.let { result["exclusiveMaximum"] = it }
        schema.multipleOf?.let { result["multipleOf"] = it }

        return result
    }

    /**
     * Processes reference schemas by returning the reference to another schema.
     *
     * @param schema The reference schema.
     * @return A map containing the reference.
     */
    private fun transformReference(schema: ElementSchema.Reference): Map<String, Any?> {
        return mapOf(
            ElementSchema.Reference.REFERENCE to schema.ref
        )
    }

    /**
     * Processes enum schemas by including the list of possible values.
     *
     * @param schema The enum schema to process.
     * @return A map representing the enum schema.
     */
    private fun transformEnum(schema: ElementSchema.Enum): Map<String, Any?> {
        return mapOf(
            "type" to schema.schemaType(),
            "enum" to schema.values
        )
    }

    /**
     * Processes array schemas by transforming the items into their appropriate schema format.
     *
     * @param schema The array schema.
     * @return A map representing the array schema.
     */
    private fun transformArray(schema: ElementSchema.Array): Map<String, Any?> {
        return mapOf(
            "type" to schema.schemaType(),
            "items" to transformElementSchema(elementSchema = schema.items)
        )
    }

    /**
     * Processes schemas that allow additional properties, ensuring they are transformed correctly.
     *
     * @param schema The schema for additional properties.
     * @return A map representing the additional properties.
     */
    private fun transformAdditionalProperties(schema: ElementSchema.AdditionalProperties): Map<String, Any?> {
        return mapOf(
            "type" to schema.schemaType(),
            "additionalProperties" to transformElementSchema(elementSchema = schema.additionalProperties)
        )
    }

    /**
     * Converts a [SchemaProperty] into a map, handling nullable properties by allowing null type values.
     *
     * @param property The property to process.
     * @return A map representing the property, or `null` if the property is transient.
     */
    private fun transformSchemaProperty(property: SchemaProperty): Map<String, Any?> {
        // Transform the base schema.
        val baseSchema: MutableMap<String, Any?> = transformElementSchema(
            elementSchema = property.schema
        ).toMutableMap()

        // Handle nullable fields by turning type into ["string", "null"].
        return if (property.isNullable) {
            mapOf(
                Composition.ANY_OF() to listOf(
                    baseSchema,
                    mapOf("type" to "null")
                )
            )
        } else {
            baseSchema
        }
    }
}
