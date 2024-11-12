/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.schema.elements

import io.github.perracodex.kopapi.dsl.examples.elements.IExample

/**
 * Represents a schema for primitive types (e.g., `string`, `integer`, etc.).
 *
 * @property format Optional format to further define the api type (e.g., `date-time`, `uuid`).
 * @property minLength The minimum character length for string types.
 * @property maxLength The maximum character length for string types.
 * @property pattern A regular expression pattern that the type must match.
 * @property contentEncoding May be used to specify the Content-Encoding for the schema.
 * @property contentMediaType May be used to specify the Media-Type for the schema.
 * @property minimum The minimum allowed value for numeric types.
 * @property maximum The maximum allowed value for numeric types.
 * @property exclusiveMinimum The exclusive lower bound for numeric types.
 * @property exclusiveMaximum The exclusive upper bound for numeric types.
 * @property multipleOf Specifies that the type’s value must be a multiple of this number.
 * @property minItems The minimum number of items in an array type.
 * @property maxItems The maximum number of items in an array type.
 * @property uniqueItems Determines if all items in an array type must be unique.
 * @property examples Examples be used for documentation purposes.
 */
internal data class ApiSchemaAttributes(
    val format: String?,
    val minLength: Int?,
    val maxLength: Int?,
    val pattern: String?,
    val contentEncoding: String?,
    val contentMediaType: String?,
    val minimum: Number?,
    val maximum: Number?,
    val exclusiveMinimum: Number?,
    val exclusiveMaximum: Number?,
    val multipleOf: Number?,
    val minItems: Int?,
    val maxItems: Int?,
    val uniqueItems: Boolean?,
    val examples: IExample?
)
