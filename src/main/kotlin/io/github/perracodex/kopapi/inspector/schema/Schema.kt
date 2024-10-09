/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.schema

import io.github.perracodex.kopapi.keys.DataType

/**
 * Defines the various types of schemas that can be represented.
 *
 * Each subclass corresponds to a specific kind of schema in OpenAPI
 * and contains the necessary properties to fully describe that schema.
 */
internal sealed class Schema(open val type: DataType) {
    /**
     * Represents a schema for primitive types (e.g., `string`, `integer`, etc.).
     *
     * @property type The primitive data type (e.g., `string`, `integer`).
     * @property format An optional format to further define the data type (e.g., `date-time`, `uuid`).
     * @property minLength Minimum length for string values.
     * @property maxLength Maximum length for string values.
     * @property minimum Minimum value for numeric types. Defines the inclusive lower bound.
     * @property maximum Maximum value for numeric types. Defines the inclusive upper bound.
     * @property exclusiveMinimum Exclusive lower bound for numeric types. The value is strictly greater.
     * @property exclusiveMaximum Exclusive upper bound for numeric types. The value is strictly less.
     * @property multipleOf Factor that constrains the value to be a multiple of a number.
     * @property additional Map for specifying any additional custom properties not covered by the above fields.
     */
    data class Primitive(
        override val type: DataType,
        val format: String? = null,
        val minLength: Int? = null,
        val maxLength: Int? = null,
        val minimum: Number? = null,
        val maximum: Number? = null,
        val exclusiveMinimum: Number? = null,
        val exclusiveMaximum: Number? = null,
        val multipleOf: Number? = null,
        val additional: Map<String, Any>? = null
    ) : Schema(type = type)

    /**
     * Represents an enumeration schema, defining a set of allowed values for a data type.
     *
     * @property values The list of allowed values for the enumeration.
     */
    data class Enum(
        val values: List<String>
    ) : Schema(type = DataType.STRING)

    /**
     * Represents an array schema, defining a collection of items of a specified schema.
     *
     * @property type The data type, which is `array`.
     * @property items The schema of the items contained in the array.
     */
    data class Array(
        val items: Schema
    ) : Schema(type = DataType.ARRAY)

    /**
     * Represents an object schema with a set of named properties.
     * This class defines the structure of an object type in OpenAPI, where each property has
     * an associated schema and metadata.
     *
     * @property properties A map of property names to their corresponding schemas and metadata.
     */
    data class Object(
        val properties: MutableMap<String, SchemaProperty> = mutableMapOf()
    ) : Schema(type = DataType.OBJECT)

    /**
     * Represents a reference to another schema defined elsewhere.
     * This is used to avoid duplication by referencing a schema definition using a `$ref` pointer.
     *
     * @property ref The reference string pointing to the schema definition (e.g., `#/components/schemas/MySchema`).
     */
    data class Reference(
        val ref: String
    ) : Schema(type = DataType.OBJECT)

    /**
     * Represents a schema with additional properties (for maps).
     */
    /**
     * Represents a schema that allows for additional properties of a specified schema.
     * This is used for maps or dictionaries where the property names are dynamic,
     * and all values conform to the same schema.
     *
     * @property additionalProperties The schema that defines the allowed types of the additional properties.
     */
    data class AdditionalProperties(
        val additionalProperties: Schema
    ) : Schema(type = DataType.OBJECT)

    companion object {
        /** The key used to reference another schema. */
        @Suppress("unused")
        const val REFERENCE: String = "\$ref"

        /** The path to the schema definitions in the OpenAPI specification. */
        const val REFERENCE_PATH: String = "#/components/schemas/"
    }
}
