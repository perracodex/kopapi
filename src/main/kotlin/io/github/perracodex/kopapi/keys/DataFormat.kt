/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.keys

import io.github.perracodex.kopapi.inspector.schema.factory.SchemaFactory

/**
 * Represents the formats used in OpenAPI specifications for primitive types.
 *
 * @property value The string value of the format as defined by the OpenAPI specification.
 *
 * @see [SchemaFactory]
 * @see [DataType]
 */
internal enum class DataFormat(val value: String) {
    BYTE(value = "byte"),
    DATE(value = "date"),
    DATETIME(value = "date-time"),
    DOUBLE(value = "double"),
    FLOAT(value = "float"),
    INT32(value = "int32"),
    INT64(value = "int64"),
    TIME(value = "time"),
    URI(value = "uri"),
    UUID(value = "uuid");

    operator fun invoke(): String = value

    override fun toString(): String = value
}
