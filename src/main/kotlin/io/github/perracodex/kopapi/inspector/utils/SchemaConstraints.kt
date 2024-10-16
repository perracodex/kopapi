/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.utils

import io.github.perracodex.kopapi.keys.ApiType
import io.github.perracodex.kopapi.system.KopapiException

/**
 * Provides validation logic for schema constraints based on the [ApiType].
 */
@PublishedApi
internal object SchemaConstraints {

    /**
     * Validates the constraints for a given [ApiType].
     * Ensures that only valid constraints for the specified [ApiType] are applied.
     *
     * ### Constraint Applicability:
     * - **For `STRING` types**: `minLength` and `maxLength` are applicable.
     * - **For `NUMBER` and `INTEGER` types**: `minimum`, `maximum`,
     *   `exclusiveMinimum`, `exclusiveMaximum`, and `multipleOf` are applicable.
     * - For all other types (such as `ARRAY`, `BOOLEAN`, `OBJECT`, etc.),
     *   no string or number-specific constraints are allowed.
     *
     * Any attempt to apply constraints that do not correspond to the [apiType] will result in an exception.
     *
     * @param apiType The type map to be used in the OpenAPI schema. For example, `string` or `integer`.
     * @param minLength Minimum length for string types.
     * @param maxLength Maximum length for string types.
     * @param minimum Minimum value for numeric types. Defines the inclusive lower bound.
     * @param maximum Maximum value for numeric types. Defines the inclusive upper bound.
     * @param exclusiveMinimum Exclusive lower bound for numeric types. The value is strictly greater.
     * @param exclusiveMaximum Exclusive upper bound for numeric types. The value is strictly less.
     * @param multipleOf Factor that constrains the value to be a multiple of a number.
     *
     * @throws KopapiException If constraints not applicable to the provided [apiType] are specified,
     * or if any values are invalid (e.g., negative lengths, conflicting constraints).
     */
    fun validate(
        apiType: ApiType,
        minLength: Int? = null,
        maxLength: Int? = null,
        minimum: Number? = null,
        maximum: Number? = null,
        exclusiveMinimum: Number? = null,
        exclusiveMaximum: Number? = null,
        multipleOf: Number? = null
    ) {
        when (apiType) {
            ApiType.STRING -> {
                verifyStringConstraints(
                    minLength = minLength,
                    maxLength = maxLength,
                    minimum = minimum,
                    maximum = maximum,
                    exclusiveMinimum = exclusiveMinimum,
                    exclusiveMaximum = exclusiveMaximum,
                    multipleOf = multipleOf
                )
            }

            ApiType.NUMBER, ApiType.INTEGER -> {
                verifyNumericConstraints(
                    minLength = minLength,
                    maxLength = maxLength,
                    minimum = minimum,
                    maximum = maximum,
                    exclusiveMinimum = exclusiveMinimum,
                    exclusiveMaximum = exclusiveMaximum,
                    multipleOf = multipleOf
                )
            }

            else -> {
                // Ensure no string- or number-specific constraints are applied to other types (like arrays, booleans, etc.)
                if (
                    listOf(
                        minLength,
                        maxLength,
                        minimum,
                        maximum,
                        exclusiveMinimum,
                        exclusiveMaximum,
                        multipleOf
                    ).any { it != null }
                ) {
                    throw KopapiException("Constraints (minLength, maxLength, minimum, maximum, etc.) can only be used with strings or numbers.")
                }
            }
        }
    }

    private fun verifyStringConstraints(
        minLength: Int? = null,
        maxLength: Int? = null,
        minimum: Number? = null,
        maximum: Number? = null,
        exclusiveMinimum: Number? = null,
        exclusiveMaximum: Number? = null,
        multipleOf: Number? = null
    ) {
        // Validate string-specific constraints.
        minLength?.let {
            if (it < 0) {
                throw KopapiException("Minimum length must be greater than or equal to zero.")
            }
        }
        maxLength?.let {
            if (it < 0) {
                throw KopapiException("Maximum length must be greater than or equal to zero.")
            }
        }
        minLength?.let {
            if (maxLength != null && minLength > maxLength) {
                throw KopapiException("Minimum length must be less than or equal to maximum length.")
            }
        }

        // Ensure no number-specific constraints are applied.
        if (listOf(minimum, maximum, exclusiveMinimum, exclusiveMaximum, multipleOf).any { it != null }) {
            throw KopapiException("Number constraints (minimum, maximum, multipleOf, etc.) cannot be applied to strings.")
        }
    }

    private fun verifyNumericConstraints(
        minLength: Int? = null,
        maxLength: Int? = null,
        minimum: Number? = null,
        maximum: Number? = null,
        exclusiveMinimum: Number? = null,
        exclusiveMaximum: Number? = null,
        multipleOf: Number? = null
    ) {
        minimum?.let {
            if (maximum != null && minimum.toDouble() > maximum.toDouble()) {
                throw KopapiException("'minimum' value must be less than or equal to 'maximum' value.")
            }
        }
        exclusiveMinimum?.let {
            if (minimum != null && exclusiveMinimum.toDouble() <= minimum.toDouble()) {
                throw KopapiException("'exclusiveMinimum' must be strictly greater than the 'minimum' value.")
            }
        }
        exclusiveMaximum?.let {
            if (maximum != null && exclusiveMaximum.toDouble() >= maximum.toDouble()) {
                throw KopapiException("'exclusiveMaximum' must be strictly less than the 'maximum' value.")
            }
        }
        multipleOf?.let {
            if (multipleOf.toDouble() <= 0.0) {
                throw KopapiException("'multipleOf' must be greater than zero.")
            }
        }

        // Ensure no string-specific constraints are applied.
        if (listOf(minLength, maxLength).any { it != null }) {
            throw KopapiException("String constraints (minLength, maxLength) cannot be applied to numbers.")
        }
    }
}
