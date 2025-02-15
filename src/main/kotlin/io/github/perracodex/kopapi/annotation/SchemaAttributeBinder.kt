/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.annotation

import io.github.perracodex.kopapi.schema.facet.ElementSchema
import io.github.perracodex.kopapi.type.ApiType
import io.github.perracodex.kopapi.util.orNull
import io.github.perracodex.kopapi.util.trimOrNull

/**
 * Utility class for binding parsed annotated attributes to [ElementSchema] instances.
 */
internal object SchemaAttributeBinder {
    /**
     * Creates a new [ElementSchema] instance applying the given [attributes].
     *
     * @param attributes The [SchemaAnnotationAttributes] to copy into the schema.
     * @return A new [ElementSchema] with the attributes copied, if applicable, or the original schema.
     */
    fun bind(schema: ElementSchema, attributes: SchemaAnnotationAttributes): ElementSchema {
        return when (schema) {
            is ElementSchema.AdditionalProperties -> schema.copy(
                description = attributes.description.trimOrNull(),
                examples = attributes.examples ?: schema.examples,
                defaultValue = parseDefaultValue(defaultValue = attributes.defaultValue, apiType = ApiType.STRING)
            )

            is ElementSchema.Array -> {
                // Update item format if provided, applicable only to top-level primitive items.
                // Nested arrays retain their original format.
                val updatedItems: ElementSchema = attributes.format.trimOrNull()?.let { format ->
                    (schema.items as? ElementSchema.Primitive)?.copy(format = format)
                } ?: schema.items

                schema.copy(
                    description = attributes.description.trimOrNull(),
                    minItems = attributes.minItems ?: schema.minItems,
                    maxItems = attributes.maxItems ?: schema.maxItems,
                    uniqueItems = attributes.uniqueItems ?: schema.uniqueItems,
                    items = updatedItems,
                    examples = attributes.examples ?: schema.examples,
                    defaultValue = parseDefaultValue(
                        defaultValue = attributes.defaultValue,
                        apiType = ApiType.ARRAY,
                        elementType = schema.items.schemaType
                    )
                )
            }

            is ElementSchema.Enum -> schema.copy(
                description = attributes.description.trimOrNull(),
                examples = attributes.examples ?: schema.examples,
                defaultValue = parseDefaultValue(defaultValue = attributes.defaultValue, apiType = schema.schemaType)
            )

            is ElementSchema.ObjectDescriptor -> schema.copy(
                description = attributes.description.trimOrNull(),
                examples = attributes.examples ?: schema.examples,
                defaultValue = parseDefaultValue(defaultValue = attributes.defaultValue, apiType = schema.schemaType)
            )

            is ElementSchema.Primitive -> schema.copy(
                description = attributes.description.trimOrNull(),
                format = attributes.format.trimOrNull(),
                minLength = attributes.minLength ?: schema.minLength,
                maxLength = attributes.maxLength ?: schema.maxLength,
                pattern = attributes.pattern.trimOrNull(),
                contentEncoding = attributes.contentEncoding.trimOrNull(),
                contentMediaType = attributes.contentMediaType.trimOrNull(),
                minimum = attributes.minimum ?: schema.minimum,
                maximum = attributes.maximum ?: schema.maximum,
                exclusiveMinimum = attributes.exclusiveMinimum ?: schema.exclusiveMinimum,
                exclusiveMaximum = attributes.exclusiveMaximum ?: schema.exclusiveMaximum,
                multipleOf = attributes.multipleOf ?: schema.multipleOf,
                examples = attributes.examples ?: schema.examples,
                defaultValue = parseDefaultValue(defaultValue = attributes.defaultValue, apiType = schema.schemaType)
            )

            is ElementSchema.Reference -> {
                schema.copy(
                    description = attributes.description.trimOrNull(),
                    examples = attributes.examples ?: schema.examples,
                    defaultValue = parseDefaultValue(defaultValue = attributes.defaultValue, apiType = ApiType.STRING)
                )
            }
        }
    }

    /**
     * Parses a default value string based on the specified `apiType` and `elementType`.
     *
     * For arrays, it uses `elementType` to determine the type of each element in the parsed list.
     *
     * `Object` and `Reference` elements should be passed as `ApiType.STRING`, as no
     * so either the attribute default is returned or the default value of the schema, if any.
     *
     * @param defaultValue The default value string to parse.
     * @param apiType The primary `ApiType` of the value, indicating how it should be parsed.
     * @param elementType The `ApiType` of each element if `apiType` is `ARRAY`; ignored for other types.
     * @return The parsed value as the appropriate type, or the input `defaultValue` if parsing is not possible.
     */
    private fun parseDefaultValue(
        defaultValue: String?,
        apiType: ApiType,
        elementType: ApiType? = null
    ): Any? {
        if (defaultValue.isNullOrBlank()) {
            return defaultValue
        }

        return when (apiType) {
            ApiType.STRING -> defaultValue
            ApiType.INTEGER -> defaultValue.toIntOrNull()
            ApiType.NUMBER -> defaultValue.toDoubleOrNull()
            ApiType.BOOLEAN -> defaultValue.toBooleanStrictOrNull()
            ApiType.ARRAY -> parseArray(defaultValue = defaultValue, elementType = elementType)
            else -> defaultValue
        } ?: defaultValue
    }

    /**
     * Parses a string representation of an array and converts each element to the specified `elementType`.
     *
     * @param defaultValue The array string to parse.
     * @param elementType The `ApiType` of each element to convert to.
     * @return A list of parsed elements or `null` if parsing fails or `elementType` is unsupported.
     */
    private fun parseArray(defaultValue: String, elementType: ApiType?): List<Any>? {
        if (elementType == null) {
            return null
        }

        return defaultValue.trim()
            .removeSurrounding(prefix = "[", suffix = "]")
            .removeSurrounding(prefix = "(", suffix = ")")
            .removeSurrounding(prefix = "{", suffix = "}")
            .split(",")
            .mapNotNull { element ->
                val trimmedElement: String = element.trim()
                when (elementType) {
                    ApiType.STRING -> trimmedElement
                    ApiType.INTEGER -> trimmedElement.toIntOrNull()
                    ApiType.NUMBER -> trimmedElement.toDoubleOrNull()
                    ApiType.BOOLEAN -> trimmedElement.toBooleanStrictOrNull()
                    else -> null
                }
            }.orNull()
    }
}
