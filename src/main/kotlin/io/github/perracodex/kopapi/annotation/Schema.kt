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
 *    val age: Int
 *
 *    @Schema(description = "The list of hobbies.", minItems = 1, maxItems = 5, uniqueItems = true)
 *    val hobbies: List<String>
 *
 *    @Schema(description = "Choice of colors.", defaultValue = "RED")
 *    val color: Color
 *
 *    @Schema(description = "The user's email address.", pattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
 *    val email: String
 *
 *    @Schema(description = "The person's rating.", exclusiveMinimum = "0", exclusiveMaximum = "5")
 *    val rating: Double
 *
 *    @Schema(description = "The total amount due, in whole dollars.", multipleOf = "5")
 *    val amountDue: Int
 *
 *    // ...
 * )
 * ```
 *
 * #### Attribute Applicability
 *
 * Depending on the type of the field being annotated, only the relevant attributes are applicable:
 * - **String Fields**: `minLength`, `maxLength`, `pattern`
 * - **Numeric Fields**: `minimum`, `maximum`, `exclusiveMinimum`, `exclusiveMaximum`, `multipleOf`
 * - **Array Fields**: `minItems`, `maxItems`, `uniqueItems`
 *
 * Non-relevant attributes to a type are ignored.
 *
 * @property description A brief description of the field.
 * @property defaultValue A default value for the field.
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
@MustBeDocumented
public annotation class Schema(
    val description: String = "",
    val defaultValue: String = "",
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
    val uniqueItems: Boolean = false
)
