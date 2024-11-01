/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.attribute

/**
 * Represents the parsed attribute constraints from the [Attributes] annotation.
 *
 * @property description A brief description of the field, or `null` if not specified.
 * @property minLength The minimum character length for string fields, or `null` if not specified.
 * @property maxLength The maximum character length for string fields, or `null` if not specified.
 * @property minimum The minimum allowed value for numeric fields, or `null` if not specified.
 * @property maximum The maximum allowed value for numeric fields, or `null` if not specified.
 * @property exclusiveMinimum The exclusive lower bound for numeric fields, or `null` if not specified.
 * @property exclusiveMaximum The exclusive upper bound for numeric fields, or `null` if not specified.
 * @property multipleOf Specifies that the field’s value must be a multiple of this number, or `null` if not specified.
 */
internal data class ParsedAttributes(
    val description: String?,
    val minLength: Int?,
    val maxLength: Int?,
    val minimum: Number?,
    val maximum: Number?,
    val exclusiveMinimum: Number?,
    val exclusiveMaximum: Number?,
    val multipleOf: Number?
)
