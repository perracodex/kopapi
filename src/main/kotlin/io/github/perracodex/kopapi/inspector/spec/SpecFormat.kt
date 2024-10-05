/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.spec

/**
 * Represents the formats used in OpenAPI specifications.
 *
 * @property value The string value of the format.
 */
internal enum class SpecFormat(val value: String) {
    BYTE(value = "byte"),
    DATE(value = "date"),
    DATETIME(value = "date-time"),
    DOUBLE(value = "double"),
    FLOAT(value = "float"),
    INT32(value = "int32"),
    INT64(value = "int64"),
    TIME(value = "time"),
    URI(value = "url"),
    UUID(value = "uuid");

    operator fun invoke(): String = value

    override fun toString(): String = value
}
