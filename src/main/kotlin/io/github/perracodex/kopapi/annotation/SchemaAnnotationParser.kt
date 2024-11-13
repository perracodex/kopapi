/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.annotation

import io.github.perracodex.kopapi.dsl.example.element.ApiExampleArray
import io.github.perracodex.kopapi.dsl.example.element.ApiInlineExample
import io.github.perracodex.kopapi.dsl.example.element.IExample
import io.github.perracodex.kopapi.system.Tracer
import io.github.perracodex.kopapi.util.orNull
import io.github.perracodex.kopapi.util.trimOrNull
import java.math.BigDecimal
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

/**
 * Utility class to parse the [Schema] annotation from an annotated element (property or class).
 */
internal object SchemaAnnotationParser {
    private val tracer = Tracer<SchemaAnnotationParser>()

    /**
     * Parses the [Schema] annotation from the given annotated [element].
     *
     * @param element The [KAnnotatedElement] to parse, which may contain an `@Schema` annotation.
     * @return A [SchemaAnnotationAttributes] instance with the parsed constraints,
     * or `null` if the element is not annotated with `@Schema`.
     */
    fun parse(element: KAnnotatedElement): SchemaAnnotationAttributes? {
        return runCatching {
            val classifier: KClassifier = getClassifier(element = element) ?: run {
                tracer.warning("Failed to retrieve classifier for element: ${element::class}")
                return null
            }

            // Retrieve the Attributes annotation from the element.
            val attributes: Schema = element.findAnnotation<Schema>()
                ?: return null

            // Common attributes.
            val description: String? = attributes.description.trimOrNull()
            val defaultValue: String? = attributes.defaultValue.trimOrNull()
            val format: String? = attributes.format.trimOrNull()

            // Examples.
            val example: String? = attributes.example.trimOrNull()
            val examples: List<String>? = attributes.examples.orNull()?.toList()
            val finalExamples: IExample = when {
                !examples.isNullOrEmpty() -> ApiExampleArray(examples = examples.map { ApiInlineExample(value = it) }.toTypedArray())
                example != null -> ApiExampleArray(examples = arrayOf(ApiInlineExample(value = example)))
                else -> ApiExampleArray(examples = emptyArray())
            }

            // String-specific constraints.
            val minLength: Int? = attributes.minLength.takeIf { it >= 0 }
            val maxLength: Int? = attributes.maxLength.takeIf { it >= 0 }
            val pattern: String? = attributes.pattern.trimOrNull()
            val contentEncoding: String? = attributes.contentEncoding.trimOrNull()
            val contentMediaType: String? = attributes.contentMediaType.trimOrNull()

            // Numeric-specific constraints.
            val minimum: Number? = parseNumber(value = attributes.minimum, classifier = classifier)
            val maximum: Number? = parseNumber(value = attributes.maximum, classifier = classifier)
            val exclusiveMinimum: Number? = parseNumber(value = attributes.exclusiveMinimum, classifier = classifier)
            val exclusiveMaximum: Number? = parseNumber(value = attributes.exclusiveMaximum, classifier = classifier)

            // Multiple of a number. Must be positive.
            var multipleOf: Number? = parseNumber(value = attributes.multipleOf, classifier = classifier)
            multipleOf = multipleOf?.takeIf { it.toDouble() > 0 }

            // Array-specific constraints.
            val minItems: Int? = attributes.minItems.takeIf { it >= 0 }
            val maxItems: Int? = attributes.maxItems.takeIf { it >= 0 }
            val uniqueItems: Boolean? = attributes.uniqueItems.takeIf { it }

            // Check if any attributes have been assigned.
            val hasAssignedAttributes: Boolean = listOfNotNull(
                // Common attributes.
                description, defaultValue, format, finalExamples,
                // String-specific constraints.
                minLength, maxLength, pattern, contentEncoding, contentMediaType,
                // Numeric-specific constraints.
                minimum, maximum, exclusiveMinimum, exclusiveMaximum, multipleOf,
                // Array-specific constraints.
                minItems, maxItems, uniqueItems
            ).isNotEmpty()
            if (!hasAssignedAttributes) {
                return null
            }

            // Return the parsed attributes.
            return SchemaAnnotationAttributes(
                description = description,
                defaultValue = defaultValue,
                format = format,
                minLength = minLength,
                maxLength = maxLength,
                pattern = pattern,
                contentEncoding = contentEncoding,
                contentMediaType = contentMediaType,
                minimum = minimum,
                maximum = maximum,
                exclusiveMinimum = exclusiveMinimum,
                exclusiveMaximum = exclusiveMaximum,
                multipleOf = multipleOf,
                minItems = minItems,
                maxItems = maxItems,
                uniqueItems = uniqueItems,
                examples = finalExamples
            )
        }.onFailure {
            tracer.error("Failed to parse `@Schema` annotation for element ${element::class}: ${it.message}")
        }.getOrNull()
    }

    /**
     * Retrieves the classifier from the given [KAnnotatedElement].
     *
     * @param element The [KAnnotatedElement] to retrieve the classifier from.
     * @return The [KClassifier] of the element, or `null` if the classifier could not be retrieved.
     */
    private fun getClassifier(element: KAnnotatedElement): KClassifier? {
        return when (element) {
            is KProperty<*> -> element.returnType.classifier
            is KClass<*> -> element
            else -> {
                tracer.warning("Unsupported KAnnotatedElement type: ${element::class}")
                return null
            }
        }
    }

    /**
     * Tries to parse a string as a numeric value, first using the expected type from the classifier,
     * then falling back to generic parsing.
     *
     * @param value The string representation of the number to parse.
     * @param classifier The expected type (e.g., Int::class, Double::class).
     * @return A `Number` in the specified or inferred type, or `null` if the value is empty or cannot be parsed.
     */
    private fun parseNumber(value: String, classifier: KClassifier?): Number? {
        // Return null if the value is empty, meaning "not set".
        if (value.isBlank()) {
            return null
        }

        // Try parsing based on the classifier first
        val parsedNumber: Number? = when (classifier) {
            Int::class, UInt::class,
            Short::class, UShort::class,
            Byte::class, UByte::class,
            Char::class -> value.toIntOrNull()

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
