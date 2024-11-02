/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.annotation

/**
 * Annotation to define attributes for a field, aligned with OpenAPI 3.1 schema specification.
 *
 * #### Sample Usage
 * ```kotlin
 * data class Person(
 *    @Schema(description = "The first name of the person.", minLength = 3, maxLength = 50)
 *    val firstName: String,
 *
 *    @Schema(description = "The age of the person.", minimum = "1", maximum = "120")
 *    val age: String
 *
 *    // ...
 * )
 * ```
 *
 * #### Property Notes:
 * - **Default Values**: If a property is left as the default, it is considered "not set" and will be ignored.
 * - **Numeric Properties** (`minimum`, `maximum`, `exclusiveMinimum`, `exclusiveMaximum`, `multipleOf`):
 *   - These properties must be valid numeric strings if set.
 *   - Parsing will be based on the annotated field's type (e.g., `Int`, `Double`).
 *
 * #### Attribute Applicability
 *
 * Depending on the type of the field being annotated, only the relevant attributes are applicable:
 * - **String Fields**: `minLength`, `maxLength`, `pattern`
 * - **Numeric Fields**: `minimum`, `maximum`, `exclusiveMinimum`, `exclusiveMaximum`, `multipleOf`
 * - **Array Fields**: `minItems`, `maxItems`, `uniqueItems`
 *
 * Non-relevant attributes to a type are ignored. This exclusivity ensures that only pertinent constraints
 * are applied based on the field's data type.
 *
 * @property description A brief description of the field.
 * @property format Overrides the default format for the field allowing for custom formats.
 * @property minLength Specifies the minimum character length for string types.
 * @property maxLength Specifies the maximum character length for string types.
 * @property pattern A regular expression pattern that the field must match.
 * @property minimum The minimum allowed value for numeric types, as a string.
 * @property maximum The maximum allowed value for numeric types, as a string.
 * @property exclusiveMinimum Defines an exclusive lower bound for numeric types, as a string.
 * @property exclusiveMaximum Defines an exclusive upper bound for numeric types, as a string.
 * @property multipleOf Constrains the field value to be a multiple of this number, as a string.
 * @property minItems Specifies the minimum number of items in an array.
 * @property maxItems Specifies the maximum number of items in an array.
 * @property uniqueItems Specifies that all items in an array must be unique.
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
public annotation class Schema(
    val description: String = "",
    val format: String = "",
    val minLength: Int = -1,
    val maxLength: Int = -1,
    val pattern: String = "",
    val minimum: String = "",
    val maximum: String = "",
    val exclusiveMinimum: String = "",
    val exclusiveMaximum: String = "",
    val multipleOf: String = "",
    val minItems: Int = -1,
    val maxItems: Int = -1,
    val uniqueItems: Boolean = false,
)
