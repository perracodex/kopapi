/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.attribute

import io.github.perracodex.kopapi.schema.facets.ElementSchema

/**
 * Represents the parsed attribute constraints from the [Attributes] annotation.
 *
 * @property description A brief description of the field, or `null` if not specified.
 * @property minLength The minimum character length for string fields, or `null` if not specified.
 * @property maxLength The maximum character length for string fields, or `null` if not specified.
 * @property pattern A regular expression pattern that the field must match, or `null` if not specified.
 * @property minimum The minimum allowed value for numeric fields, or `null` if not specified.
 * @property maximum The maximum allowed value for numeric fields, or `null` if not specified.
 * @property exclusiveMinimum The exclusive lower bound for numeric fields, or `null` if not specified.
 * @property exclusiveMaximum The exclusive upper bound for numeric fields, or `null` if not specified.
 * @property multipleOf Specifies that the field’s value must be a multiple of this number, or `null` if not specified.
 * @property minItems Specifies the minimum number of items in an array. Ignored if default (-1).
 * @property maxItems Specifies the maximum number of items in an array. Ignored if default (-1).
 * @property uniqueItems Specifies that all items in an array must be unique. Defaults to `false`.
 */
internal data class ParsedAttributes(
    val description: String? = null,
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
) {
    companion object {
        /**
         * Copies the attributes from the given [attributes] to the given [ElementSchema].
         *
         * @param attributes The [ParsedAttributes] to copy from.
         * @return A new [ElementSchema] with the attributes copied, if applicable, or the original schema.
         */
        fun ofElementSchema(schema: ElementSchema, attributes: ParsedAttributes): ElementSchema {
            return when (schema) {
                is ElementSchema.AdditionalProperties -> schema.copy(
                    description = attributes.description ?: schema.description,
                )

                is ElementSchema.Array -> schema.copy(
                    description = attributes.description ?: schema.description,
                    minItems = attributes.minItems ?: schema.minItems,
                    maxItems = attributes.maxItems ?: schema.maxItems,
                    uniqueItems = attributes.uniqueItems ?: schema.uniqueItems
                )

                is ElementSchema.Enum -> schema.copy(
                    description = attributes.description ?: schema.description,
                )

                is ElementSchema.Object -> schema.copy(
                    description = attributes.description ?: schema.description,
                )

                is ElementSchema.Primitive -> schema.copy(
                    description = attributes.description ?: schema.description,
                    minLength = attributes.minLength ?: schema.minLength,
                    maxLength = attributes.maxLength ?: schema.maxLength,
                    pattern = attributes.pattern ?: schema.pattern,
                    minimum = attributes.minimum ?: schema.minimum,
                    maximum = attributes.maximum ?: schema.maximum,
                    exclusiveMinimum = attributes.exclusiveMinimum ?: schema.exclusiveMinimum,
                    exclusiveMaximum = attributes.exclusiveMaximum ?: schema.exclusiveMaximum,
                    multipleOf = attributes.multipleOf ?: schema.multipleOf
                )

                is ElementSchema.Reference -> schema.copy(
                    description = attributes.description ?: schema.description,
                )
            }
        }
    }
}
