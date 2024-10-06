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
 * @property specFormat The format of the type, if any. For example, `int32` or `int64`.
 *
 * @see [CustomTypeRegistry]
 * @see [KopapiConfig.customType]
 * @see [CustomTypeBuilder]
 */
internal data class CustomType internal constructor(
    val type: KType,
    val specType: String,
    val specFormat: String? = null,
) {
    init {
        require(specType.isNotBlank()) { "Custom type must not be empty." }
        require(type.classifier != Any::class) { "Custom type cannot be of type 'Any'. Define an explicit type." }
        require(type.classifier != Unit::class) { "Custom type cannot be of type 'Unit'. Define an explicit type." }
    }
}
