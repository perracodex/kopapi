/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.core.dsl.types

/**
 * Enum representing the possible HTTP authentication methods, applicable only to HTTP Security schemes.
 *
 * @property value The string value of the HTTP authentication method.
 */
public enum class AuthenticationMethod(internal val value: String) {
    /** HTTP Basic authentication method. */
    BASIC(value = "basic"),

    /** HTTP Bearer authentication method (commonly used with JWT). */
    BEARER(value = "bearer"),

    /** HTTP Digest authentication method. */
    DIGEST(value = "digest")
}