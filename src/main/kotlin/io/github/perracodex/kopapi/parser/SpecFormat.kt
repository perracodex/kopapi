/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.parser

/**
 * Mappings for OpenAPI format values.
 *
 * @property value The string value of the format.
 */
internal sealed class SpecFormat(val value: String) {
    data object BINARY : SpecFormat(value = "binary")
    data object BYTE : SpecFormat(value = "byte")
    data object DATE : SpecFormat(value = "date")
    data object DATETIME : SpecFormat(value = "date-time")
    data object DOUBLE : SpecFormat(value = "double")
    data object FLOAT : SpecFormat(value = "float")
    data object INT32 : SpecFormat(value = "int32")
    data object INT64 : SpecFormat(value = "int64")
    data object TIME : SpecFormat(value = "time")
    data object URI : SpecFormat(value = "url")
    data object UUID : SpecFormat(value = "uuid")

    operator fun invoke(): String = value

    override fun toString(): String = value
}
