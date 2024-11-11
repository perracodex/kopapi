/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.common.schema

import io.github.perracodex.kopapi.dsl.common.example.IExample
import io.github.perracodex.kopapi.dsl.common.example.builders.SchemaExampleBuilder
import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.utils.trimOrNull

/**
 * Builds a schema for primitive types (e.g., `string`, `integer`, etc.), including arrays.
 *
 * Not applicable to complex object types.
 * For complex objects, use instead the `@schema` annotation directly on the class type.
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
 *
 */
@KopapiDsl
public class SchemaAttributeBuilder internal constructor() {
    public var format: String? = null
    public var minLength: Int? = null
    public var maxLength: Int? = null
    public var pattern: String? = null
    public var contentEncoding: String? = null
    public var contentMediaType: String? = null
    public var minimum: Number? = null
    public var maximum: Number? = null
    public var exclusiveMinimum: Number? = null
    public var exclusiveMaximum: Number? = null
    public var multipleOf: Number? = null
    public var minItems: Int? = null
    public var maxItems: Int? = null
    public var uniqueItems: Boolean? = null

    /** Holds the examples associated with the schema. */
    private var examples: IExample? = null

    /**
     * Adds an example to the schema.
     *
     * #### Usage
     * ```
     * schema {
     *     examples {
     *         example("John Doe")
     *         example(mapOf("role" to "Developer", "age" to 35))
     *         example(listOf("Developer", "Project Manager"))
     *     }
     * }
     * ```
     *
     * @receiver [SchemaExampleBuilder] The builder used to configure the schema examples.
     *
     * @see [SchemaExampleBuilder.example]
     */
    public fun examples(builder: SchemaExampleBuilder.() -> Unit) {
        examples = SchemaExampleBuilder().apply(builder).build(existingExamples = examples)
    }

    /**
     * Builds an [ApiSchemaAttributes] instance from the current builder state.
     *
     * @return The constructed [ApiSchemaAttributes] instance, or `null` if no attributes were set.
     */
    internal fun build(): ApiSchemaAttributes? {
        return ApiSchemaAttributes(
            format = format.trimOrNull(),
            minLength = minLength?.takeIf { it >= 0 },
            maxLength = maxLength?.takeIf { it >= 0 },
            pattern = pattern.trimOrNull(),
            contentEncoding = contentEncoding.trimOrNull(),
            contentMediaType = contentMediaType.trimOrNull(),
            minimum = minimum,
            maximum = maximum,
            exclusiveMinimum = exclusiveMinimum,
            exclusiveMaximum = exclusiveMaximum,
            multipleOf = multipleOf?.takeIf { it.toDouble() > 0 }, // Ensure multipleOf is positive.
            minItems = minItems?.takeIf { it >= 0 },
            maxItems = maxItems?.takeIf { it >= 0 },
            uniqueItems = uniqueItems?.takeIf { it },
            examples = examples
        ).takeIf {
            listOfNotNull(
                format,
                minLength,
                maxLength,
                pattern,
                contentEncoding,
                contentMediaType,
                minimum,
                maximum,
                exclusiveMinimum,
                exclusiveMaximum,
                multipleOf,
                minItems,
                maxItems,
                uniqueItems,
                examples
            ).isNotEmpty()
        }
    }
}
