/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.common.schema

/**
 * Represents a schema for primitive types (e.g., `string`, `integer`, etc.).
 *
 * @property format Optional format to further define the api type (e.g., `date-time`, `uuid`).
 * @property minLength The minimum character length for string types.
 * @property maxLength The maximum character length for string types.
 * @property pattern A regular expression pattern that the type must match.
 * @property minimum The minimum allowed value for numeric types.
 * @property maximum The maximum allowed value for numeric types.
 * @property exclusiveMinimum The exclusive lower bound for numeric types.
 * @property exclusiveMaximum The exclusive upper bound for numeric types.
 * @property multipleOf Specifies that the type’s value must be a multiple of this number.
 * @property minItems The minimum number of items in an array type.
 * @property maxItems The maximum number of items in an array type.
 * @property uniqueItems Determines if all items in an array type must be unique.
 */
internal data class ApiSchemaAttributes(
    val format: String?,
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val pattern: String? = null,
    val minimum: Number? = null,
    val maximum: Number? = null,
    val exclusiveMinimum: Number? = null,
    val exclusiveMaximum: Number? = null,
    val multipleOf: Number? = null,
    val minItems: Int? = null,
    val maxItems: Int? = null,
    val uniqueItems: Boolean? = null
)
