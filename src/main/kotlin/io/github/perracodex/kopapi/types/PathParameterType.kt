/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.types

/**
 * Enum that defines the allowed types and formats for path parameters in OpenAPI 3.1.
 *
 * @property apiType The primary [ApiType] representing the OpenAPI type.
 * @property apiFormat An optional [ApiFormat] providing additional formatting details.
 */
public enum class PathParameterType(
    internal val apiType: ApiType,
    internal val apiFormat: ApiFormat? = null
) {
    /** Represents a boolean type. */
    BOOLEAN(ApiType.BOOLEAN),

    /** Represents a date without a time component. */
    DATE(ApiType.STRING, ApiFormat.DATE),

    /** Represents a date and time. */
    DATE_TIME(ApiType.STRING, ApiFormat.DATETIME),

    /** Represents a 32-bit integer. */
    INT32(ApiType.INTEGER, ApiFormat.INT32),

    /** Represents a 64-bit integer. */
    INT64(ApiType.INTEGER, ApiFormat.INT64),

    /** Represents a general string type. */
    STRING(ApiType.STRING),

    /** Represents a universally unique identifier (UUID). */
    UUID(ApiType.STRING, ApiFormat.UUID);
}
