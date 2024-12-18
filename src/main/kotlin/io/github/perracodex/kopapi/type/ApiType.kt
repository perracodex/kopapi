/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.type

import com.fasterxml.jackson.annotation.JsonValue

/**
 * Represents the api types used in OpenAPI specifications.
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
