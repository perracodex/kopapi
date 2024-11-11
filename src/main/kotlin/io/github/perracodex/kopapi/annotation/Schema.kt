/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.annotation

/**
 * Annotation to define attributes for a field, aligned with OpenAPI 3.1 schema specification.
 *
 * #### Usage
 * ```kotlin
 * @Schema(description = "Represents a person.")
 * data class Person(
 *
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
 * - Depending on the type of the field being annotated, only the relevant attributes are applicable:
 *      - **String Fields**: `minLength`, `maxLength`, `pattern`
 *      - **Numeric Fields**: `minimum`, `maximum`, `exclusiveMinimum`, `exclusiveMaximum`, `multipleOf`
 *      - **Array Fields**: `minItems`, `maxItems`, `uniqueItems`
 *
 * - Non-relevant attributes to a type are ignored.
 * - The class itself can also be annotated, but only the `description` attribute is applicable.
 *
 * #### Examples Attribute
 * - `example` and the list `examples` should not be used together. Only one should be defined.
 * - If both are sey, then the list of `examples` will take precedence and the single `example` will be ignored.
 *
 * @property description A brief description of the field. (Can be used also with the class)
 * @property defaultValue A default value for the field.
 * @property format Overrides the default format for the field allowing for custom formats.
 * @property minLength Specifies the minimum character length for string types.
 * @property maxLength Specifies the maximum character length for string types.
 * @property pattern A regular expression pattern that the field must match.
 * @property contentEncoding May be used to specify the Content-Encoding for the schema.
 * @property contentMediaType May be used to specify the Media-Type for the schema.
 * @property minimum The minimum allowed value for numeric types. As a string.
 * @property maximum The maximum allowed value for numeric types, As a string.
 * @property exclusiveMinimum Defines an exclusive lower bound for numeric types. As a string.
 * @property exclusiveMaximum Defines an exclusive upper bound for numeric types. As a string.
 * @property multipleOf Constrains the field value to be a multiple of this number. As a string.
 * @property minItems Specifies the minimum number of items in an array.
 * @property maxItems Specifies the maximum number of items in an array.
 * @property uniqueItems Specifies that all items in an array must be unique.
 * @property example An example to be used for documentation purposes.
 * @property examples An array of examples to be used for documentation purposes.
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
    val contentEncoding: String = "",
    val contentMediaType: String = "",
    val minimum: String = "",
    val maximum: String = "",
    val exclusiveMinimum: String = "",
    val exclusiveMaximum: String = "",
    val multipleOf: String = "",
    val minItems: Int = -1,
    val maxItems: Int = -1,
    val uniqueItems: Boolean = false,
    val example: String = "",
    val examples: Array<String> = []
)
