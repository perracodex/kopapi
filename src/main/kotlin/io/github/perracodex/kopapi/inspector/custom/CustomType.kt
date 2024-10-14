/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.custom

import io.github.perracodex.kopapi.dsl.plugin.builders.CustomTypeBuilder
import io.github.perracodex.kopapi.inspector.utils.SchemaConstraints
import io.github.perracodex.kopapi.keys.ApiType
import io.github.perracodex.kopapi.plugin.KopapiConfig
import kotlin.reflect.KType

/**
 * Represents a user defined `custom type` to be used when generating the OpenAPI schema,
 * allowing to inject new non-handled types, or define custom specifications for existing standard types.
 *
 * #### Constraint Fields
 * The constraints defined in this class (such as `minLength`, `maximum`, etc.)
 * are only applicable to specific `apiType` values:
 * - For `STRING` types: `minLength` and `maxLength` are applicable.
 * - For `NUMBER` and `INTEGER` types: `minimum`, `maximum`, `exclusiveMinimum`, `exclusiveMaximum`, and `multipleOf` are applicable.
 *
 * Any attempt to apply constraints not relevant to the specified `apiType`
 * will result in a validation error during object construction.
 *
 * @property type The [KType] of the parameter, specifying the Kotlin type.
 * @property apiType The type map to be used in the OpenAPI schema. For example, `string` or `integer`.
 * @property apiFormat Used for defining expected api format (e.g., "date", "email").
 * @property minLength Minimum length for string types.
 * @property maxLength Maximum length for string types.
 * @property minimum Minimum value for numeric types. Defines the inclusive lower bound.
 * @property maximum Maximum value for numeric types. Defines the inclusive upper bound.
 * @property exclusiveMinimum Exclusive lower bound for numeric types. The value is strictly greater.
 * @property exclusiveMaximum Exclusive upper bound for numeric types. The value is strictly less.
 * @property multipleOf Factor that constrains the value to be a multiple of a number.
 *
 * @see [CustomTypeRegistry]
 * @see [KopapiConfig.addType]
 * @see [CustomTypeBuilder]
 */
internal data class CustomType internal constructor(
    val type: KType,
    val apiType: ApiType,
    val apiFormat: String? = null,
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val minimum: Number? = null,
    val maximum: Number? = null,
    val exclusiveMinimum: Number? = null,
    val exclusiveMaximum: Number? = null,
    val multipleOf: Number? = null
) {
    init {
        SchemaConstraints.validate(
            apiType = apiType,
            minLength = minLength,
            maxLength = maxLength,
            minimum = minimum,
            maximum = maximum,
            exclusiveMinimum = exclusiveMinimum,
            exclusiveMaximum = exclusiveMaximum,
            multipleOf = multipleOf
        )
    }
}
