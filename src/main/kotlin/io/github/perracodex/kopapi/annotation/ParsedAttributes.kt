/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.annotation

import io.github.perracodex.kopapi.schema.facets.ElementSchema
import io.github.perracodex.kopapi.utils.trimOrNull

/**
 * Represents the parsed attribute constraints from the [Schema] annotation.
 *
 * @property description A brief description of the field.
 * @property format Overrides the default format for the field allowing for custom formats.
 * @property minLength The minimum character length for string fields.
 * @property maxLength The maximum character length for string fields.
 * @property pattern A regular expression pattern that the field must match.
 * @property minimum The minimum allowed value for numeric fields.
 * @property maximum The maximum allowed value for numeric fields.
 * @property exclusiveMinimum The exclusive lower bound for numeric fields.
 * @property exclusiveMaximum The exclusive upper bound for numeric fields.
 * @property multipleOf Specifies that the field’s value must be a multiple of this number.
 * @property minItems Specifies the minimum number of items in an array.
 * @property maxItems Specifies the maximum number of items in an array.
 * @property uniqueItems Specifies that all items in an array must be unique.
 */
internal data class ParsedAttributes(
    val description: String? = null,
    val format: String? = null,
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
                    description = attributes.description.trimOrNull(),
                )

                is ElementSchema.Array -> schema.copy(
                    description = attributes.description.trimOrNull(),
                    minItems = attributes.minItems ?: schema.minItems,
                    maxItems = attributes.maxItems ?: schema.maxItems,
                    uniqueItems = attributes.uniqueItems ?: schema.uniqueItems
                )

                is ElementSchema.Enum -> schema.copy(
                    description = attributes.description.trimOrNull(),
                )

                is ElementSchema.Object -> schema.copy(
                    description = attributes.description.trimOrNull(),
                )

                is ElementSchema.Primitive -> schema.copy(
                    description = attributes.description.trimOrNull(),
                    format = attributes.format.trimOrNull(),
                    minLength = attributes.minLength ?: schema.minLength,
                    maxLength = attributes.maxLength ?: schema.maxLength,
                    pattern = attributes.pattern.trimOrNull(),
                    minimum = attributes.minimum ?: schema.minimum,
                    maximum = attributes.maximum ?: schema.maximum,
                    exclusiveMinimum = attributes.exclusiveMinimum ?: schema.exclusiveMinimum,
                    exclusiveMaximum = attributes.exclusiveMaximum ?: schema.exclusiveMaximum,
                    multipleOf = attributes.multipleOf ?: schema.multipleOf
                )

                is ElementSchema.Reference -> schema.copy(
                    description = attributes.description.trimOrNull(),
                )
            }
        }
    }
}
