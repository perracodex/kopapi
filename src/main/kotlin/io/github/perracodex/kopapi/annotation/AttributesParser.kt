/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.annotation

import io.github.perracodex.kopapi.system.Tracer
import io.github.perracodex.kopapi.utils.trimOrNull
import java.math.BigDecimal
import kotlin.reflect.KClassifier
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

/**
 * Utility class to parse the [Schema] annotation from a property and return a [ParsedAttributes] object.
 */
internal object AttributesParser {
    private val tracer = Tracer<AttributesParser>()

    /**
     * Parses the [Schema] annotation from the given property and returns a [ParsedAttributes] object.
     *
     * @param property The [KProperty] to parse, which may contain an `Attributes` annotation.
     * @return A `ParsedAttributes` instance with the parsed constraints,
     * or `null` if the property is not annotated with `Attributes`.
     */
    fun parse(property: KProperty<*>): ParsedAttributes? {
        return runCatching {
            // Retrieve the Attributes annotation from the property.
            val attributes: Schema = property.findAnnotation<Schema>() ?: return null

            // Common attributes.
            val description: String? = attributes.description.trimOrNull()
            val format: String? = attributes.format.trimOrNull()

            // String-specific constraints.
            val minLength: Int? = attributes.minLength.takeIf { it >= 0 }
            val maxLength: Int? = attributes.maxLength.takeIf { it >= 0 }
            val pattern: String? = attributes.pattern.trimOrNull()

            // Numeric-specific constraints.
            val minimum: Number? = parseNumber(value = attributes.minimum, classifier = property.returnType.classifier)
            val maximum: Number? = parseNumber(value = attributes.maximum, classifier = property.returnType.classifier)
            val exclusiveMinimum: Number? = parseNumber(value = attributes.exclusiveMinimum, classifier = property.returnType.classifier)
            val exclusiveMaximum: Number? = parseNumber(value = attributes.exclusiveMaximum, classifier = property.returnType.classifier)
            val multipleOf: Number? = parseNumber(value = attributes.multipleOf, classifier = property.returnType.classifier)

            // Array-specific constraints.
            val minItems: Int? = attributes.minItems.takeIf { it >= 0 }
            val maxItems: Int? = attributes.maxItems.takeIf { it >= 0 }
            val uniqueItems: Boolean? = attributes.uniqueItems.takeIf { it }

            // If all parsed values are null, return null.
            if (description == null && minLength == null && maxLength == null && pattern == null &&
                minimum == null && maximum == null && exclusiveMinimum == null &&
                exclusiveMaximum == null && multipleOf == null &&
                minItems == null && maxItems == null && uniqueItems == null
            ) {
                return null
            }

            // Return the parsed attributes for this property.
            return ParsedAttributes(
                description = description,
                format = format,
                minLength = minLength,
                maxLength = maxLength,
                pattern = pattern,
                minimum = minimum,
                maximum = maximum,
                exclusiveMinimum = exclusiveMinimum,
                exclusiveMaximum = exclusiveMaximum,
                multipleOf = multipleOf,
                minItems = minItems,
                maxItems = maxItems,
                uniqueItems = uniqueItems
            )
        }.onFailure {
            tracer.error("Failed to parse `Attributes` annotation for property ${property.name}: ${it.message}")
        }.getOrNull()
    }

    /**
     * Tries to parse a string as a numeric value, first using the expected type from the classifier,
     * then falling back to generic parsing.
     *
     * @param value The string representation of the number to parse.
     * @param classifier The expected type of the property (e.g., Int::class, Double::class).
     * @return A `Number` in the specified or inferred type, or `null` if the value is empty or cannot be parsed.
     */
    private fun parseNumber(value: String, classifier: KClassifier?): Number? {
        // Return null if the value is empty, meaning "not set".
        if (value.isBlank()) {
            return null
        }

        // Try parsing based on the classifier first
        val parsedNumber: Number? = when (classifier) {
            Int::class, UInt::class, Short::class, UShort::class, Byte::class, UByte::class -> value.toIntOrNull()
            Long::class, ULong::class -> value.toLongOrNull()
            Float::class -> value.toFloatOrNull()
            Double::class -> value.toDoubleOrNull()
            BigDecimal::class -> value.toBigDecimalOrNull()
            else -> null // If classifier isn't a specific numeric type, fall back to generic parsing.
        }

        // If parsing with the classifier fails, attempt general parsing (Int, Long, Float, Double)
        return parsedNumber
            ?: value.toIntOrNull()
            ?: value.toLongOrNull()
            ?: value.toFloatOrNull()
            ?: value.toDoubleOrNull()
            ?: value.toBigDecimalOrNull()
    }
}
