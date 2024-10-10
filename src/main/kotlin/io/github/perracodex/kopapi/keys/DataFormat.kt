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
public enum class DataFormat(internal val value: String) {
    /** Represents a schema for a `byte` type, allowing base64-encoded binary data. */
    BYTE(value = "byte"),

    /** Represents a schema for a `date` type, allowing date values. */
    DATE(value = "date"),

    /** Represents a schema for a `date-time` type, allowing date-time values. */
    DATETIME(value = "date-time"),

    /** Represents a schema for a `double` type, allowing double-precision floating-point values. */
    DOUBLE(value = "double"),

    /** Represents a schema for a `float` type, allowing single-precision floating-point values. */
    FLOAT(value = "float"),

    /** Represents a schema for an `int32` type, allowing 32-bit integer values. */
    INT32(value = "int32"),

    /** Represents a schema for an `int64` type, allowing 64-bit integer values. */
    INT64(value = "int64"),

    /** Represents a schema for a `time` type, allowing time values. */
    TIME(value = "time"),

    /** Represents a schema for a `url` type, allowing URL values. */
    URI(value = "uri"),

    /** Represents a schema for a `uuid` type, allowing UUID values. */
    UUID(value = "uuid");

    /** Returns the string value representing the OpenAPI format. */
    public operator fun invoke(): String = value

    override fun toString(): String = value
}
