/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.custom

import io.github.perracodex.kopapi.plugin.KopapiConfig
import io.github.perracodex.kopapi.plugin.builders.CustomTypeBuilder
import kotlin.reflect.KType

/**
 * Represents a user defined `custom type` to be used when generating the OpenAPI schema,
 * allowing to inject new non-handled types, or define custom specifications for existing standard types.
 *
 * @property type The [KType] of the parameter, specifying the Kotlin type.
 * @property specType The type map to be used in the OpenAPI schema. For example, `string` or `integer`.
 * @property specFormat Used for defining expected data formats (e.g., "date", "email").
 * @property minLength Minimum length for string values.
 * @property maxLength Maximum length for string values.
 * @property minimum Minimum value for numeric types. Defines the inclusive lower bound.
 * @property maximum Maximum value for numeric types. Defines the inclusive upper bound.
 * @property exclusiveMinimum Exclusive lower bound for numeric types. The value is strictly greater.
 * @property exclusiveMaximum Exclusive upper bound for numeric types. The value is strictly less.
 * @property multipleOf Factor that constrains the value to be a multiple of a number.
 * @property additional Map for specifying any additional custom properties not covered by the above fields
 *
 * @see [CustomTypeRegistry]
 * @see [KopapiConfig.customType]
 * @see [CustomTypeBuilder]
 */
internal data class CustomType internal constructor(
    val type: KType,
    val specType: String,
    val specFormat: String? = null,
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val minimum: Number? = null,
    val maximum: Number? = null,
    val exclusiveMinimum: Number? = null,
    val exclusiveMaximum: Number? = null,
    val multipleOf: Number? = null,
    val additional: Map<String, String>? = null
) {
    init {
        require(specType.isNotBlank()) { "Custom type must not be empty." }
        require(type.classifier != Any::class) { "Custom type cannot be of type 'Any'. Define an explicit type." }
        require(type.classifier != Unit::class) { "Custom type cannot be of type 'Unit'. Define an explicit type." }

        // Validate length constraints.
        minLength?.let { require(it >= 0) { "Minimum length must be greater than or equal to zero." } }
        maxLength?.let { require(it >= 0) { "Maximum length must be greater than or equal to zero." } }
        minLength?.let {
            require(maxLength == null || minLength <= maxLength) {
                "Minimum length must be less than or equal to maximum length."
            }
        }

        // Validate value constraints.
        minimum?.let {
            require(maximum == null || minimum.toDouble() <= maximum.toDouble()) {
                "Minimum value must be less than or equal to maximum value."
            }
        }
        exclusiveMinimum?.let {
            require(minimum == null || exclusiveMinimum.toDouble() > minimum.toDouble()) {
                "Exclusive minimum must be strictly greater than the minimum value."
            }
        }
        exclusiveMaximum?.let {
            require(maximum == null || exclusiveMaximum.toDouble() < maximum.toDouble()) {
                "Exclusive maximum must be strictly less than the maximum value."
            }
        }

        // Validate multipleOf constraint
        multipleOf?.let {
            require(multipleOf.toDouble() > 0) { "multipleOf must be greater than zero." }
        }
    }
}
