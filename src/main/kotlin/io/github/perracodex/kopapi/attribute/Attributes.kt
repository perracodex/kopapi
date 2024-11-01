/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.attribute

/**
 * Annotation to define attributes for a field, mimicking OpenAPI 3.1 schema constraints.
 *
 * #### Sample Usage
 * ```kotlin
 * data class Person(
 *    @Attributes(description = "The first name of the person.", minLength = 3, maxLength = 50)
 *    val firstName: String,
 *    @Attributes(description = "The age of the person.", minimum = "1", maximum = "120")
 *    val age: String
 *    // ...
 * )
 * ```
 *
 *
 * Common Notes:
 * - **Default Values**: If a property is left as the default, it is considered "not set" and will be ignored.
 * - **Numeric Properties** (`minimum`, `maximum`, `exclusiveMinimum`, `exclusiveMaximum`, `multipleOf`):
 *   - These properties must be valid numeric strings if set.
 *   - Parsing will be based on the annotated field's type (e.g., `Int`, `Double`).
 *
 * @property description A brief description of the field. Defaults to an empty string (ignored if not set).
 * @property minLength Specifies the minimum character length for string types. Ignored if default (-1).
 * @property maxLength Specifies the maximum character length for string types. Ignored if default (-1).
 * @property minimum The minimum allowed value for numeric types, as a string.
 * @property maximum The maximum allowed value for numeric types, as a string.
 * @property exclusiveMinimum Defines an exclusive lower bound for numeric types, as a string.
 * @property exclusiveMaximum Defines an exclusive upper bound for numeric types, as a string.
 * @property multipleOf Constrains the field value to be a multiple of this number, as a string.
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
public annotation class Attributes(
    val description: String = "",
    val minLength: Int = -1,
    val maxLength: Int = -1,
    val minimum: String = "",
    val maximum: String = "",
    val exclusiveMinimum: String = "",
    val exclusiveMaximum: String = "",
    val multipleOf: String = ""
)
