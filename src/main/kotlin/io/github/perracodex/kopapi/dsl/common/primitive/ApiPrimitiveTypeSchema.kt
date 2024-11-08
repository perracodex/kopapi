/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.common.primitive

/**
 * Represents a schema for primitive types (e.g., `string`, `integer`, etc.).
 *
 * @property format Optional format to further define the api type (e.g., `date-time`, `uuid`).
 * @property minLength The minimum character length for string fields.
 * @property maxLength The maximum character length for string fields.
 * @property pattern A regular expression pattern that the field must match.
 * @property minimum The minimum allowed value for numeric fields.
 * @property maximum The maximum allowed value for numeric fields.
 * @property exclusiveMinimum The exclusive lower bound for numeric fields.
 * @property exclusiveMaximum The exclusive upper bound for numeric fields.
 * @property multipleOf Specifies that the field’s value must be a multiple of this number.
 */
internal data class ApiPrimitiveTypeSchema(
    val format: String?,
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val pattern: String? = null,
    val minimum: Number? = null,
    val maximum: Number? = null,
    val exclusiveMinimum: Number? = null,
    val exclusiveMaximum: Number? = null,
    val multipleOf: Number? = null
)
