/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.custom

import io.github.perracodex.kopapi.dsl.plugin.builders.CustomTypeBuilder
import io.github.perracodex.kopapi.plugin.KopapiConfig
import io.github.perracodex.kopapi.types.ApiType
import kotlin.reflect.KType

/**
 * Represents a user defined `custom type` to be used when generating the OpenAPI schema,
 * allowing to inject new non-handled types, or define custom specifications for existing standard types.
 *
 * No restrictions or checks are enforced on the combination of properties,
 * allowing for flexible custom definitions.
 *
 * @property type The [KType] of the parameter, specifying the Kotlin type.
 * @property apiType The type map to be used in the OpenAPI schema. For example, `string` or `integer`.
 * @property apiFormat Used for defining expected api format (e.g., "date", "email").
 * @property description A brief description of the type, or `null` if not specified.
 * @property minLength Minimum length for string types.
 * @property maxLength Maximum length for string types.
 * @property pattern A regular expression pattern that string types must match, or `null` if not specified.
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
    val apiFormat: String?,
    val description: String?,
    val minLength: Int?,
    val maxLength: Int?,
    val pattern: String?,
    val minimum: Number?,
    val maximum: Number?,
    val exclusiveMinimum: Number?,
    val exclusiveMaximum: Number?,
    val multipleOf: Number?
)
