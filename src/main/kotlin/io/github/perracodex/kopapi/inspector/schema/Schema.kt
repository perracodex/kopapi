/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.schema

import io.github.perracodex.kopapi.inspector.utils.SchemaConstraints
import io.github.perracodex.kopapi.keys.ApiType

/**
 * Defines the various types of schemas that can be represented.
 *
 * Each subclass corresponds to a specific kind of schema in OpenAPI
 * and contains the necessary properties to fully describe that schema.
 */
@PublishedApi
internal sealed class Schema(open val type: ApiType) {
    /**
     * Represents a schema for primitive types (e.g., `string`, `integer`, etc.).
     *
     * @property type The primitive api type (e.g., `string`, `integer`).
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
        override val type: ApiType,
        val format: String? = null,
        val minLength: Int? = null,
        val maxLength: Int? = null,
        val minimum: Number? = null,
        val maximum: Number? = null,
        val exclusiveMinimum: Number? = null,
        val exclusiveMaximum: Number? = null,
        val multipleOf: Number? = null,
    ) : Schema(type = type) {
        init {
            SchemaConstraints.validate(
                apiType = type,
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
     * Represents an enumeration schema, defining a set of allowed values for a data type.
     *
     * @property values The list of allowed values for the enumeration.
     */
    data class Enum(
        val values: List<String>
    ) : Schema(type = ApiType.STRING)

    /**
     * Represents an array schema, defining a collection of items of a specified schema.
     *
     * @property items The schema of the items contained in the array.
     */
    data class Array(
        val items: Schema
    ) : Schema(type = ApiType.ARRAY)

    /**
     * Represents an object schema with a set of named properties.
     * This class defines the structure of an object type in OpenAPI, where each property has
     * an associated schema and metadata.
     *
     * @property properties A map of property names to their corresponding schemas and metadata.
     */
    data class Object(
        val properties: MutableMap<String, SchemaProperty> = mutableMapOf()
    ) : Schema(type = ApiType.OBJECT)

    /**
     * Represents a reference to another schema defined elsewhere.
     * This is used to avoid duplication by referencing a schema definition using a `$ref` pointer.
     *
     * @property schemaName The name of the schema being referenced.
     * @property ref The reference path to the schema definition.
     */
    data class Reference(
        val schemaName: String
    ) : Schema(type = ApiType.OBJECT) {
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
     * Represents a schema that allows for additional properties of a specified schema.
     * This is used for maps or dictionaries where the property names are dynamic,
     * and all values conform to the same schema.
     *
     * @property additionalProperties The schema that defines the allowed types of the additional properties.
     */
    data class AdditionalProperties(
        val additionalProperties: Schema
    ) : Schema(type = ApiType.OBJECT)
}
