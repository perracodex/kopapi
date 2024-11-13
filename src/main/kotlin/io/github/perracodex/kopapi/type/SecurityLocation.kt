/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.type

import com.fasterxml.jackson.annotation.JsonValue

/**
 * Represents the possible locations where security scheme payloads can be passed.
 */
internal enum class SecurityLocation(@JsonValue internal val value: String) {
    /** The payload is set in a cookie. */
    COOKIE(value = "cookie"),

    /** The Payload is set an HTTP header. */
    HEADER(value = "header"),

    /** The payload is set in a query parameter. */
    QUERY(value = "query")
}
