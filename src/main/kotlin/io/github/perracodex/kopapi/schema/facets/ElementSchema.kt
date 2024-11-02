/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.schema.facets

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.types.ApiType
import io.github.perracodex.kopapi.utils.safeName

/**
 * Defines the core element schemas used in OpenAPI.
 *
 * Each subclass corresponds to a specific schema type, such as object, array, primitive, or enum.
 * Element schemas are the building blocks for more complex OpenAPI schemas.
 *
 * @property definition A unique identifier for debugging and clarity during schema generation.
 * @property description A brief description of the schema.
 * @property minLength Minimum length for string types.
 * @property maxLength Maximum length for string types.
 * @property minimum Minimum value for numeric types. Defines the inclusive lower bound.
 * @property maximum Maximum value for numeric types. Defines the inclusive upper bound.
 * @property exclusiveMinimum Exclusive lower bound for numeric types. The value is strictly greater.
 * @property exclusiveMaximum Exclusive upper bound for numeric types. The value is strictly less.
 * @property multipleOf Factor that constrains the value to be a multiple of a number.
 * @property defaultValue An optional default value for the schema.
 */
internal sealed class ElementSchema(
    @JsonIgnore open val definition: String,
    @JsonIgnore open val description: String? = null,
    @JsonIgnore open val minLength: Int? = null,
    @JsonIgnore open val maxLength: Int? = null,
    @JsonIgnore open val minimum: Number? = null,
    @JsonIgnore open val maximum: Number? = null,
    @JsonIgnore open val exclusiveMinimum: Number? = null,
    @JsonIgnore open val exclusiveMaximum: Number? = null,
    @JsonIgnore open val multipleOf: Number? = null,
    @JsonIgnore open val defaultValue: Any? = null
) : ISchemaFacet {
    /**
     * Represents an object schema with a set of named properties.
     *
     * @property schemaType The API type of the schema as defined in the OpenAPI specification.
     * @property objectProperties A map of property names to their corresponding raw schemas and metadata.
     * @property properties A map of property names to their corresponding OpenAPI schemas.
     * @property required A set of required property names.
     */
    data class Object(
        @JsonIgnore override val definition: String = Object::class.safeName(),
        @JsonProperty("type") val schemaType: ApiType = ApiType.OBJECT,
        @JsonIgnore val objectProperties: MutableMap<String, SchemaProperty> = mutableMapOf(),
        @JsonProperty("description") override val description: String? = null,
        @JsonProperty("properties") val properties: Map<String, ISchemaFacet>? = null,
        @JsonProperty("required") val required: MutableSet<String>? = null
    ) : ElementSchema(definition = definition)

    /**
     * Represents an array schema, defining a collection of items of a specified schema.
     *
     * @property schemaType The API type of the schema as defined in the OpenAPI specification.
     * @property items The schema of the items contained in the array.
     */
    data class Array(
        @JsonIgnore override val definition: String = Array::class.safeName(),
        @JsonProperty("type") val schemaType: ApiType = ApiType.ARRAY,
        @JsonProperty("description") override val description: String? = null,
        @JsonProperty("minLength") override val minLength: Int? = null,
        @JsonProperty("maxLength") override val maxLength: Int? = null,
        @JsonProperty("minimum") override val minimum: Number? = null,
        @JsonProperty("maximum") override val maximum: Number? = null,
        @JsonProperty("exclusiveMinimum") override val exclusiveMinimum: Number? = null,
        @JsonProperty("exclusiveMaximum") override val exclusiveMaximum: Number? = null,
        @JsonProperty("multipleOf") override val multipleOf: Number? = null,
        @JsonProperty("items") val items: ElementSchema,
        @JsonProperty("default") override val defaultValue: Any? = null
    ) : ElementSchema(definition = definition, defaultValue = defaultValue)

    /**
     * Represents a schema that allows for additional properties of a specified type.
     * This is commonly used for maps or dictionaries with dynamic keys.
     *
     * @property schemaType The API type of the schema as defined in the OpenAPI specification.
     * @property additionalProperties The schema that defines the allowed types of the additional properties.
     */
    data class AdditionalProperties(
        @JsonIgnore override val definition: String = AdditionalProperties::class.safeName(),
        @JsonProperty("type") val schemaType: ApiType = ApiType.OBJECT,
        @JsonProperty("description") override val description: String? = null,
        @JsonProperty("minLength") override val minLength: Int? = null,
        @JsonProperty("maxLength") override val maxLength: Int? = null,
        @JsonProperty("minimum") override val minimum: Number? = null,
        @JsonProperty("maximum") override val maximum: Number? = null,
        @JsonProperty("exclusiveMinimum") override val exclusiveMinimum: Number? = null,
        @JsonProperty("exclusiveMaximum") override val exclusiveMaximum: Number? = null,
        @JsonProperty("multipleOf") override val multipleOf: Number? = null,
        @JsonProperty("additionalProperties") val additionalProperties: ElementSchema,
        @JsonProperty("default") override val defaultValue: Any? = null
    ) : ElementSchema(definition = definition, defaultValue = defaultValue)

    /**
     * Represents an enumeration schema, defining a set of allowed values.
     *
     * @property schemaType The API type of the schema as defined in the OpenAPI specification.
     * @property values The list of allowed values for the enumeration.
     */
    data class Enum(
        @JsonIgnore override val definition: String = Enum::class.safeName(),
        @JsonProperty("type") val schemaType: ApiType = ApiType.STRING,
        @JsonProperty("enum") val values: List<String>,
        @JsonProperty("description") override val description: String? = null,
        @JsonProperty("minLength") override val minLength: Int? = null,
        @JsonProperty("maxLength") override val maxLength: Int? = null,
        @JsonProperty("minimum") override val minimum: Number? = null,
        @JsonProperty("maximum") override val maximum: Number? = null,
        @JsonProperty("exclusiveMinimum") override val exclusiveMinimum: Number? = null,
        @JsonProperty("exclusiveMaximum") override val exclusiveMaximum: Number? = null,
        @JsonProperty("multipleOf") override val multipleOf: Number? = null,
        @JsonProperty("default") override val defaultValue: Any? = null
    ) : ElementSchema(definition = definition, defaultValue = defaultValue)

    /**
     * Represents a schema for primitive types (e.g., `string`, `integer`, etc.).
     *
     * @property schemaType The primitive [ApiType] as defined in the OpenAPI specification.
     * @property schemaType The API type of the schema as defined in the OpenAPI specification.
     * @property format An optional format to further define the api type (e.g., `date-time`, `uuid`).
     */
    data class Primitive(
        @JsonIgnore override val definition: String = Primitive::class.safeName(),
        @JsonProperty("type") val schemaType: ApiType,
        @JsonProperty("format") val format: String? = null,
        @JsonProperty("description") override val description: String? = null,
        @JsonProperty("minLength") override val minLength: Int? = null,
        @JsonProperty("maxLength") override val maxLength: Int? = null,
        @JsonProperty("minimum") override val minimum: Number? = null,
        @JsonProperty("maximum") override val maximum: Number? = null,
        @JsonProperty("exclusiveMinimum") override val exclusiveMinimum: Number? = null,
        @JsonProperty("exclusiveMaximum") override val exclusiveMaximum: Number? = null,
        @JsonProperty("multipleOf") override val multipleOf: Number? = null,
        @JsonProperty("default") override val defaultValue: Any? = null
    ) : ElementSchema(definition = definition, defaultValue = defaultValue)

    /**
     * Represents a reference to another schema.
     * This is used to reference existing schemas elsewhere in the OpenAPI document.
     *
     * @property schemaType The API type of the schema as defined in the OpenAPI specification.
     * @property schemaName The name of the schema being referenced.
     * @property ref The reference path to the schema definition.
     */
    data class Reference(
        @JsonIgnore override val definition: String = Reference::class.safeName(),
        @JsonIgnore val schemaType: ApiType = ApiType.OBJECT,
        @JsonIgnore val schemaName: String,
        @JsonProperty("description") override val description: String? = null,
        @JsonProperty("minLength") override val minLength: Int? = null,
        @JsonProperty("maxLength") override val maxLength: Int? = null,
        @JsonProperty("minimum") override val minimum: Number? = null,
        @JsonProperty("maximum") override val maximum: Number? = null,
        @JsonProperty("exclusiveMinimum") override val exclusiveMinimum: Number? = null,
        @JsonProperty("exclusiveMaximum") override val exclusiveMaximum: Number? = null,
        @JsonProperty("multipleOf") override val multipleOf: Number? = null,
        @JsonProperty("default") override val defaultValue: Any? = null
    ) : ElementSchema(definition = definition, defaultValue = defaultValue) {
        @JsonProperty(REFERENCE)
        val ref: String = "$PATH$schemaName"

        companion object {
            /** The path to the schema definitions in the OpenAPI specification. */
            const val PATH: String = "#/components/schemas/"

            /** The key used to reference another schema. */
            const val REFERENCE: String = "\$ref"
        }
    }
}
