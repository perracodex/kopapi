/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.common.primitive

import io.github.perracodex.kopapi.dsl.markers.KopapiDsl

/**
 * Builds a schema for primitive types (e.g., `string`, `integer`, etc.).
 * Only meaningful for primitive types.
 *
 * @property format An optional format to further define the api type (e.g., `date-time`, `uuid`).
 * @property minLength The minimum character length for string fields.
 * @property maxLength The maximum character length for string fields.
 * @property pattern A regular expression pattern that the field must match.
 * @property minimum The minimum allowed value for numeric fields.
 * @property maximum The maximum allowed value for numeric fields.
 * @property exclusiveMinimum The exclusive lower bound for numeric fields.
 * @property exclusiveMaximum The exclusive upper bound for numeric fields.
 * @property multipleOf Specifies that the field’s value must be a multiple of this number.
 */
@KopapiDsl
public class PrimitiveTypeSchemaBuilder internal constructor(
    public var format: String? = null,
    public var minLength: Int? = null,
    public var maxLength: Int? = null,
    public var pattern: String? = null,
    public var minimum: Number? = null,
    public var maximum: Number? = null,
    public var exclusiveMinimum: Number? = null,
    public var exclusiveMaximum: Number? = null,
    public var multipleOf: Number? = null,
) {
    /**
     * Builds an [ApiPrimitiveTypeSchema] instance from the current builder state.
     *
     * @return The constructed [ApiPrimitiveTypeSchema] instance, or `null` if no attributes were set.
     */
    internal fun build(): ApiPrimitiveTypeSchema? {
        return ApiPrimitiveTypeSchema(
            format = format,
            minLength = minLength,
            maxLength = maxLength,
            pattern = pattern,
            minimum = minimum,
            maximum = maximum,
            exclusiveMinimum = exclusiveMinimum,
            exclusiveMaximum = exclusiveMaximum,
            multipleOf = multipleOf
        ).takeIf {
            listOfNotNull(
                format,
                minLength,
                maxLength,
                pattern,
                minimum,
                maximum,
                exclusiveMinimum,
                exclusiveMaximum,
                multipleOf
            ).isNotEmpty()
        }
    }
}
