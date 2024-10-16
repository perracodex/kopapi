/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.keys

import com.fasterxml.jackson.annotation.JsonValue
import io.github.perracodex.kopapi.inspector.schema.factory.SchemaFactory

/**
 * Enum representing the api types used in OpenAPI specifications.
 *
 * Each type corresponds to a valid OpenAPI schema type as defined in the OpenAPI 3.1 specification.
 *
 * @property value The string representation of the OpenAPI type.
 *
 * @see [SchemaFactory] For schema creation using these types.
 * @see [ApiFormat] For additional format constraints.
 */
public enum class ApiType(@JsonValue internal val value: String) {

    /** Represents a schema for an `array` type, defining a collection of ordered items. */
    ARRAY(value = "array"),

    /** Represents a schema for a `boolean` type, allowing `true` or `false` values. */
    BOOLEAN(value = "boolean"),

    /** Represents a schema for an `integer` type, allowing whole number values. */
    INTEGER(value = "integer"),

    /** Represents a schema for a `number` type, allowing both integer and floating-point values. */
    NUMBER(value = "number"),

    /** Represents a schema that allows a `null` value. */
    NULL(value = "null"),

    /** Represents a schema for an `object` type, defining a collection of key-value pairs (properties). */
    OBJECT(value = "object"),

    /** Represents a schema for a `string` type, allowing text values. */
    STRING(value = "string");

    /** Returns the string value representing the OpenAPI type. */
    public operator fun invoke(): String = value

    /** Returns the string value representing the OpenAPI type. */
    override fun toString(): String = value
}
