/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.schema

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.inspector.utils.SchemaConstraints
import io.github.perracodex.kopapi.types.ApiType
import io.github.perracodex.kopapi.utils.safeName

/**
 * Defines the core element schemas used in OpenAPI.
 *
 * Each subclass corresponds to a specific schema type, such as object, array, primitive, or enum.
 * Element schemas are the building blocks for more complex OpenAPI schemas.
 *
 * @property definition A unique identifier for debugging and clarity during schema generation.
 * @property defaultValue An optional default value for the schema.
 */
internal sealed class ElementSchema(
    @JsonIgnore open val definition: String,
    @JsonProperty("default") open val defaultValue: Any? = null
) : IOpenApiSchema {
    /**
     * Represents an object schema with a set of named properties.
     *
     * @property schemaType The API type of the schema as defined in the OpenAPI specification.
     * @property properties A map of property names to their corresponding schemas and metadata.
     */
    data class Object(
        override val definition: String = Object::class.safeName(),
        val schemaType: ApiType = ApiType.OBJECT,
        val properties: MutableMap<String, SchemaProperty> = mutableMapOf(),
        override val defaultValue: Any? = null
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

    /**
     * Represents a schema for primitive types (e.g., `string`, `integer`, etc.).
     *
     * @property schemaType The primitive [ApiType] as defined in the OpenAPI specification.
     * @property schemaType The API type of the schema as defined in the OpenAPI specification.
     * @property format An optional format to further define the api type (e.g., `date-time`, `uuid`).
     * @property minLength Minimum length for string types.
     * @property maxLength Maximum length for string types.
     * @property minimum Minimum value for numeric types. Defines the inclusive lower bound.
     * @property maximum Maximum value for numeric types. Defines the inclusive upper bound.
     * @property exclusiveMinimum Exclusive lower bound for numeric types. The value is strictly greater.
     * @property exclusiveMaximum Exclusive upper bound for numeric types. The value is strictly less.
     * @property multipleOf Factor that constrains the value to be a multiple of a number.
     */
    data class Primitive(
        @JsonIgnore override val definition: String = Primitive::class.safeName(),
        @JsonProperty("type") val schemaType: ApiType,
        @JsonProperty("format") val format: String? = null,
        @JsonProperty("minLength") val minLength: Int? = null,
        @JsonProperty("maxLength") val maxLength: Int? = null,
        @JsonProperty("minimum") val minimum: Number? = null,
        @JsonProperty("maximum") val maximum: Number? = null,
        @JsonProperty("exclusiveMinimum") val exclusiveMinimum: Number? = null,
        @JsonProperty("exclusiveMaximum") val exclusiveMaximum: Number? = null,
        @JsonProperty("multipleOf") val multipleOf: Number? = null,
        @JsonProperty("default") override val defaultValue: Any? = null
    ) : ElementSchema(definition = definition, defaultValue = defaultValue) {
        init {
            SchemaConstraints.validate(
                apiType = schemaType,
                minLength = minLength,
                maxLength = maxLength,
                minimum = minimum,
                maximum = maximum,
                exclusiveMinimum = exclusiveMinimum,
                exclusiveMaximum = exclusiveMaximum,
                multipleOf = multipleOf
            )
        }
    }

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
        @JsonProperty("default") override val defaultValue: Any? = null
    ) : ElementSchema(definition = definition, defaultValue = defaultValue)

    /**
     * Represents an array schema, defining a collection of items of a specified schema.
     *
     * @property schemaType The API type of the schema as defined in the OpenAPI specification.
     * @property items The schema of the items contained in the array.
     */
    data class Array(
        @JsonIgnore override val definition: String = Array::class.safeName(),
        @JsonProperty("type") val schemaType: ApiType = ApiType.ARRAY,
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
        @JsonProperty("additionalProperties") val additionalProperties: ElementSchema,
        @JsonProperty("default") override val defaultValue: Any? = null
    ) : ElementSchema(definition = definition, defaultValue = defaultValue)

    /**
     * Represents a schema marked as nullable type.
     *
     * @property anyOf The map structure that includes the base schema marked as nullable.
     */
    data class Nullable(
        @JsonIgnore override val definition: String = Nullable::class.safeName(),
        @JsonProperty("anyOf") val anyOf: List<Any>,
        @JsonProperty("default") override val defaultValue: Any? = null
    ) : ElementSchema(definition = "Nullable", defaultValue = defaultValue) {
        constructor(schema: ElementSchema) : this(
            anyOf = listOf(
                schema,
                mapOf("type" to ApiType.NULL())
            )
        )
    }

    /**
     * Represents a transformed object schema ready for OpenAPI generation.
     *
     * @property schemaType The API type of the schema as defined in the OpenAPI specification.
     * @property properties A map of property names to their corresponding schemas and metadata.
     * @property required A set of required property names.
     */
    data class TransformedObject(
        @JsonIgnore override val definition: String = TransformedObject::class.safeName(),
        @JsonProperty("type") val schemaType: ApiType = ApiType.OBJECT,
        @JsonProperty("properties") val properties: Map<String, ElementSchema>?,
        @JsonProperty("required") var required: MutableSet<String>?,
        @JsonProperty("default") override val defaultValue: Any? = null
    ) : ElementSchema(definition = definition, defaultValue = defaultValue)
}
