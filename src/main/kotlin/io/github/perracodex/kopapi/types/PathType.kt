/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.types

/**
 * Defines the allowed types and formats for `path` parameters.
 *
 * Only certain scalars are allowed as `path` parameters,
 * therefore `PathType` is used to define its type.
 *
 * @property apiType The primary [ApiType] representing the OpenAPI type.
 * @property apiFormat An optional [ApiFormat] providing additional formatting details.
 */
public sealed class PathType(
    internal val apiType: ApiType,
    internal val apiFormat: ApiFormat? = null
) {
    /** Represents a boolean type. */
    public data object Boolean : PathType(apiType = ApiType.BOOLEAN)

    /** Represents a date without a time component. */
    public data object Date : PathType(apiType = ApiType.STRING, apiFormat = ApiFormat.DATE)

    /** Represents a date and time. */
    public data object DateTime : PathType(apiType = ApiType.STRING, apiFormat = ApiFormat.DATETIME)

    /** Represents a 32-bit integer. */
    public data object Int32 : PathType(apiType = ApiType.INTEGER, apiFormat = ApiFormat.INT32)

    /** Represents a 64-bit integer. */
    public data object Int64 : PathType(apiType = ApiType.INTEGER, apiFormat = ApiFormat.INT64)

    /** Represents a general string type. */
    public data object String : PathType(apiType = ApiType.STRING)

    /** Represents a universally unique identifier (UUID). */
    public data object Uuid : PathType(apiType = ApiType.STRING, apiFormat = ApiFormat.UUID)

    internal companion object {
        /** The list of all allowed path parameter types. */
        val values: List<PathType> = listOf(Boolean, Date, DateTime, Int32, Int64, String, Uuid)
    }
}
