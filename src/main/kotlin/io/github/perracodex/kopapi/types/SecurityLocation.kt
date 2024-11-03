/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.types

import com.fasterxml.jackson.annotation.JsonValue

/**
 * Enum representing the possible locations where security payload can be passed.
 *
 * @property value The string value of the location.
 */
internal enum class SecurityLocation(@JsonValue internal val value: String) {
    /** The payload is set in a cookie. */
    COOKIE(value = "cookie"),

    /** The Payload is set an HTTP header. */
    HEADER(value = "header"),

    /** The payload is set in a query parameter. */
    QUERY(value = "query")
}
