/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.parser

/**
 * Mappings for OpenAPI specification types.
 *
 * @property value The string value of the type.
 */
internal sealed class SpecType(val value: String) {
    data object INTEGER : SpecType(value = "integer")
    data object NUMBER : SpecType(value = "number")
    data object STRING : SpecType(value = "string")
    data object BOOLEAN : SpecType(value = "boolean")
    data object ARRAY : SpecType(value = "array")
    data object OBJECT : SpecType(value = "object")

    operator fun invoke(): String = value

    override fun toString(): String = value
}
