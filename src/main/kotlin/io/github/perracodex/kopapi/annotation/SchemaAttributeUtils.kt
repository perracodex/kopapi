/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.annotation

import io.github.perracodex.kopapi.dsl.common.schema.ApiSchemaAttributes
import io.github.perracodex.kopapi.schema.facets.ElementSchema

/**
 * Provides utility functions for working with schema attributes.
 */
internal object SchemaAttributeUtils {
    /**
     * Copies the primitive attributes from the given [attributes] to the given [schema].
     *
     * If the schema is not a primitive type, the original schema is returned.
     *
     * @param schema The base schema to copy the attributes to.
     * @param attributes The primitive attributes to copy.
     * @return A new `ElementSchema` instance with the copied attributes.
     */
    fun copySchemaAttributes(
        schema: ElementSchema,
        attributes: ApiSchemaAttributes
    ): ElementSchema {
        return when (schema) {
            is ElementSchema.AdditionalProperties ->
                schema.copy(
                    examples = attributes.examples ?: schema.examples
                )

            is ElementSchema.Array ->
                schema.copy(
                    minItems = attributes.minItems ?: schema.minItems,
                    maxItems = attributes.maxItems ?: schema.maxItems,
                    uniqueItems = attributes.uniqueItems ?: schema.uniqueItems,
                    examples = attributes.examples ?: schema.examples
                )

            is ElementSchema.Enum ->
                schema.copy(
                    examples = attributes.examples ?: schema.examples
                )

            is ElementSchema.ObjectDescriptor ->
                schema.copy(
                    examples = attributes.examples ?: schema.examples
                )

            is ElementSchema.Primitive ->
                schema.copy(
                    format = attributes.format ?: schema.format,
                    minLength = attributes.minLength ?: schema.minLength,
                    maxLength = attributes.maxLength ?: schema.maxLength,
                    pattern = attributes.pattern ?: schema.pattern,
                    contentEncoding = attributes.contentEncoding ?: schema.contentEncoding,
                    contentMediaType = attributes.contentMediaType ?: schema.contentMediaType,
                    minimum = attributes.minimum ?: schema.minimum,
                    maximum = attributes.maximum ?: schema.maximum,
                    exclusiveMinimum = attributes.exclusiveMinimum ?: schema.exclusiveMinimum,
                    exclusiveMaximum = attributes.exclusiveMaximum ?: schema.exclusiveMaximum,
                    multipleOf = attributes.multipleOf ?: schema.multipleOf,
                    examples = attributes.examples ?: schema.examples
                )

            is ElementSchema.Reference ->
                schema.copy(
                    examples = attributes.examples ?: schema.examples
                )
        }
    }
}