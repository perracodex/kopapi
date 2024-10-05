/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.parser


/**
 * Mappings for OpenAPI specification keys.
 *
 * @property value The string value of the key.
 */
internal sealed class SpecKey(val value: String) {
    object ENUM : SpecKey(value = "enum")
    object FORMAT : SpecKey(value = "format")
    object TYPE : SpecKey(value = "type")
    object ITEMS : SpecKey(value = "items")
    object PROPERTIES : SpecKey(value = "properties")
    object REQUIRED : SpecKey(value = "required")

    operator fun invoke(): String = value

    override fun toString(): String = value
}
