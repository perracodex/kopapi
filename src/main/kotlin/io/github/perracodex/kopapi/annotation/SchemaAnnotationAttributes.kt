/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.annotation

import io.github.perracodex.kopapi.dsl.common.example.IExample

/**
 * Represents the parsed attribute constraints from the [Schema] annotation.
 *
 * @property description A brief description of the field.
 * @property defaultValue A default value for the field.
 * @property format Overrides the default format for the field allowing for custom formats.
 * @property minLength The minimum character length for string fields.
 * @property maxLength The maximum character length for string fields.
 * @property pattern A regular expression pattern that the field must match.
 * @property contentEncoding May be used to specify the Content-Encoding for the schema.
 * @property contentMediaType May be used to specify the Media-Type for the schema.
 * @property minimum The minimum allowed value for numeric fields.
 * @property maximum The maximum allowed value for numeric fields.
 * @property exclusiveMinimum The exclusive lower bound for numeric fields.
 * @property exclusiveMaximum The exclusive upper bound for numeric fields.
 * @property multipleOf Specifies that the field’s value must be a multiple of this number.
 * @property minItems Specifies the minimum number of items in an array.
 * @property maxItems Specifies the maximum number of items in an array.
 * @property uniqueItems Specifies that all items in an array must be unique.
 * @property examples Examples to be used for documentation purposes.
 */
internal class SchemaAnnotationAttributes(
    val description: String?,
    val defaultValue: String?,
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
