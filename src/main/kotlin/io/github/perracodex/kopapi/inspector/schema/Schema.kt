/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.schema

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.inspector.utils.SchemaConstraints
import io.github.perracodex.kopapi.types.ApiType
import io.github.perracodex.kopapi.utils.safeName

/**
 * Defines the various types of schemas that can be represented in OpenAPI.
 *
 * Each subclass defines a specific schema type with relevant properties
 * based on the OpenAPI specification.
 *
 * @property definition A string identifier used for debugging and clarity when converting to JSON.
 */
@PublishedApi
internal sealed class Schema(
    @JsonIgnore
    open val definition: String,
) {
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
        @JsonIgnore
        override val definition: String = Primitive::class.safeName(),
        @JsonProperty("type")
        val schemaType: ApiType,
        @JsonProperty("format")
        val format: String? = null,
        @JsonProperty("minLength")
        val minLength: Int? = null,
        @JsonProperty("maxLength")
        val maxLength: Int? = null,
        @JsonProperty("minimum")
        val minimum: Number? = null,
        @JsonProperty("maximum")
        val maximum: Number? = null,
        @JsonProperty("exclusiveMinimum")
        val exclusiveMinimum: Number? = null,
        @JsonProperty("exclusiveMaximum")
        val exclusiveMaximum: Number? = null,
        @JsonProperty("multipleOf")
        val multipleOf: Number? = null,
    ) : Schema(definition = definition) {
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
        @JsonIgnore
        override val definition: String = Enum::class.safeName(),
        @JsonProperty("type")
        val schemaType: ApiType = ApiType.STRING,
        @JsonProperty("enum")
        val values: List<String>
    ) : Schema(definition = definition)

    /**
     * Represents an array schema, defining a collection of items of a specified schema.
     *
     * @property schemaType The API type of the schema as defined in the OpenAPI specification.
     * @property items The schema of the items contained in the array.
     */
    data class Array(
        @JsonIgnore
        override val definition: String = Array::class.safeName(),
        @JsonProperty("type")
        val schemaType: ApiType = ApiType.ARRAY,
        @JsonProperty("items")
        val items: Schema
    ) : Schema(definition = definition)

    /**
     * Represents an object schema with a set of named properties.
     *
     * @property schemaType The API type of the schema as defined in the OpenAPI specification.
     * @property properties A map of property names to their corresponding schemas and metadata.
     */
    data class Object(
        @JsonIgnore
        override val definition: String = Object::class.safeName(),
        @JsonProperty("type")
        val schemaType: ApiType = ApiType.OBJECT,
        @JsonProperty("properties")
        val properties: MutableMap<String, SchemaProperty> = mutableMapOf()
    ) : Schema(definition = definition)

    /**
     * Represents a reference to another schema.
     * This is used to reference existing schemas elsewhere in the OpenAPI document.
     *
     * @property schemaType The API type of the schema as defined in the OpenAPI specification.
     * @property schemaName The name of the schema being referenced.
     * @property ref The reference path to the schema definition.
     */
    data class Reference(
        @JsonIgnore
        override val definition: String = Reference::class.safeName(),
        @JsonIgnore
        val schemaType: ApiType = ApiType.OBJECT,
        @JsonIgnore
        val schemaName: String
    ) : Schema(definition = definition) {
        @JsonProperty("\$ref")
        val ref: String = "$PATH$schemaName"

        companion object {
            /** The path to the schema definitions in the OpenAPI specification. */
            const val PATH: String = "#/components/schemas/"

            /** The key used to reference another schema. */
            @Suppress("unused")
            const val REFERENCE: String = "\$ref"
        }
    }

    /**
     * Represents a schema that allows for additional properties of a specified type.
     * This is commonly used for maps or dictionaries with dynamic keys.
     *
     * @property schemaType The API type of the schema as defined in the OpenAPI specification.
     * @property additionalProperties The schema that defines the allowed types of the additional properties.
     */
    data class AdditionalProperties(
        @JsonIgnore
        override val definition: String = AdditionalProperties::class.safeName(),
        @JsonProperty("type")
        val schemaType: ApiType = ApiType.OBJECT,
        @JsonProperty("additionalProperties")
        val additionalProperties: Schema
    ) : Schema(definition = definition)

    /**
     * Represents a schema that allows for one or more schemas to be used interchangeably.
     *
     * @property anyOf A list of schemas, any of which can validate the data.
     */
    data class AnyOf(
        @JsonIgnore
        override val definition: String = AnyOf::class.safeName(),
        @JsonProperty("anyOf")
        val anyOf: List<Schema>
    ) : Schema(definition = definition)

    /**
     * Represents a schema that requires all listed schemas to be validated against the data.
     *
     * @property allOf A list of schemas, all of which must validate the data.
     */
    data class AllOf(
        @JsonIgnore
        override val definition: String = AllOf::class.safeName(),
        @JsonProperty("allOf")
        val allOf: List<Schema>
    ) : Schema(definition = definition)

    /**
     * Represents a schema where data must match exactly one of the listed schemas.
     *
     * @property oneOf A list of schemas, one of which must validate the data.
     */
    data class OneOf(
        @JsonIgnore
        override val definition: String = OneOf::class.safeName(),
        @JsonProperty("oneOf")
        val oneOf: List<Schema>
    ) : Schema(definition = definition)
}
