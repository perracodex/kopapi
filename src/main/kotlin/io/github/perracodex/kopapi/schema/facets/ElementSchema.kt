/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.schema.facets

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.types.ApiType
import io.github.perracodex.kopapi.utils.safeName
import kotlin.reflect.KType

/**
 * Defines the core element schemas used in OpenAPI.
 *
 * Each subclass corresponds to a specific schema type, such as object, array, primitive, or enum.
 * Element schemas are the building blocks for more complex OpenAPI schemas.
 *
 * @property definition A unique identifier for debugging and clarity during schema generation.
 * @property description A brief description of the schema.
 */
internal sealed class ElementSchema(
    @JsonIgnore open val definition: String,
    open val schemaType: ApiType,
    open val description: String?
) : ISchemaFacet {
    /**
     * Represents an object schema with a set of named properties.
     *
     * This element is not meant to be serialized as part of the OpenAPI schema,
     * as it holds unprocessed raw schemas and metadata.
     * Instead, when composing the OpenAPI schema, must be transformed into an [ObjectFacet] schema.
     *
     * @property schemaType The API type of the schema as defined in the OpenAPI specification.
     * @property objectProperties A map of property names to their corresponding unprocessed raw schemas and metadata.
     */
    data class Object(
        override val definition: String = Object::class.safeName(),
        val objectProperties: MutableMap<String, SchemaProperty>,
        override val description: String? = null,
    ) : ElementSchema(
        definition = definition,
        schemaType = ApiType.OBJECT,
        description = description
    )

    /**
     * Represents an array schema, defining a collection of items of a specified schema.
     *
     * @property schemaType The API type of the schema as defined in the OpenAPI specification.
     * @property minItems The minimum number of items in the array.
     * @property maxItems The maximum number of items in the array.
     * @property uniqueItems Specifies that all items in the array must be unique.
     * @property items The schema of the items contained in the array.
     * @property defaultValue An optional default value for the schema.
     */
    data class Array(
        @JsonIgnore override val definition: String = Array::class.safeName(),
        @JsonProperty("type") override val schemaType: ApiType = ApiType.ARRAY,
        @JsonProperty("description") override val description: String? = null,
        @JsonProperty("minItems") val minItems: Int? = null,
        @JsonProperty("maxItems") val maxItems: Int? = null,
        @JsonProperty("uniqueItems") val uniqueItems: Boolean? = null,
        @JsonProperty("items") val items: ElementSchema,
        @JsonProperty("default") val defaultValue: Any? = null
    ) : ElementSchema(
        definition = definition,
        schemaType = ApiType.ARRAY,
        description = description
    )

    /**
     * Represents a schema that allows for additional properties of a specified type.
     * This is commonly used for maps or dictionaries with dynamic keys.
     *
     * @property schemaType The API type of the schema as defined in the OpenAPI specification.
     * @property additionalProperties The schema that defines the allowed types of the additional properties.
     * @property defaultValue An optional default value for the schema.
     */
    data class AdditionalProperties(
        @JsonIgnore override val definition: String = AdditionalProperties::class.safeName(),
        @JsonProperty("type") override val schemaType: ApiType = ApiType.OBJECT,
        @JsonProperty("description") override val description: String? = null,
        @JsonProperty("additionalProperties") val additionalProperties: ElementSchema,
        @JsonProperty("default") val defaultValue: Any? = null
    ) : ElementSchema(
        definition = definition,
        schemaType = ApiType.OBJECT,
        description = description
    )

    /**
     * Represents an enumeration schema, defining a set of allowed values.
     *
     * @property schemaType The API type of the schema as defined in the OpenAPI specification.
     * @property values The list of allowed values for the enumeration.
     * @property defaultValue An optional default value for the schema.
     */
    data class Enum(
        @JsonIgnore override val definition: String = Enum::class.safeName(),
        @JsonProperty("type") override val schemaType: ApiType = ApiType.STRING,
        @JsonProperty("enum") val values: List<String>,
        @JsonProperty("description") override val description: String? = null,
        @JsonProperty("default") val defaultValue: Any? = null
    ) : ElementSchema(
        definition = definition,
        schemaType = ApiType.STRING,
        description = description
    )

    /**
     * Represents a schema for primitive types (e.g., `string`, `integer`, etc.).
     *
     * @property schemaType The primitive [ApiType] as defined in the OpenAPI specification.
     * @property schemaType The API type of the schema as defined in the OpenAPI specification.
     * @property format An optional format to further define the api type (e.g., `date-time`, `uuid`).
     * @property defaultValue An optional default value for the schema.
     * @property minLength The minimum character length for string fields.
     * @property maxLength The maximum character length for string fields.
     * @property pattern A regular expression pattern that the field must match.
     * @property minimum The minimum allowed value for numeric fields.
     * @property maximum The maximum allowed value for numeric fields.
     * @property exclusiveMinimum The exclusive lower bound for numeric fields.
     * @property exclusiveMaximum The exclusive upper bound for numeric fields.
     * @property multipleOf Specifies that the field’s value must be a multiple of this number.
     */
    data class Primitive(
        @JsonIgnore override val definition: String = Primitive::class.safeName(),
        @JsonProperty("type") override val schemaType: ApiType,
        @JsonProperty("format") val format: String?,
        @JsonProperty("description") override val description: String? = null,
        @JsonProperty("default") val defaultValue: Any? = null,
        @JsonProperty("minLength") val minLength: Int? = null,
        @JsonProperty("maxLength") val maxLength: Int? = null,
        @JsonProperty("pattern") val pattern: String? = null,
        @JsonProperty("minimum") val minimum: Number? = null,
        @JsonProperty("maximum") val maximum: Number? = null,
        @JsonProperty("exclusiveMinimum") val exclusiveMinimum: Number? = null,
        @JsonProperty("exclusiveMaximum") val exclusiveMaximum: Number? = null,
        @JsonProperty("multipleOf") val multipleOf: Number? = null
    ) : ElementSchema(
        definition = definition,
        schemaType = schemaType,
        description = description
    )

    /**
     * Represents a reference to another schema.
     * This is used to reference existing schemas elsewhere in the OpenAPI document.
     *
     * @property schemaType The API type of the schema as defined in the OpenAPI specification.
     * @property schemaName The name of the schema being referenced.
     * @property defaultValue An optional default value for the schema.
     * @property ref The reference path to the schema definition.
     */
    data class Reference(
        @JsonIgnore override val definition: String = Reference::class.safeName(),
        @JsonIgnore override val schemaType: ApiType = ApiType.OBJECT,
        @JsonIgnore val referencedType: KType,
        @JsonIgnore val schemaName: String,
        @JsonProperty("description") override val description: String? = null,
        @JsonProperty("default") val defaultValue: Any? = null
    ) : ElementSchema(
        definition = definition,
        schemaType = ApiType.OBJECT,
        description = description
    ) {
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
