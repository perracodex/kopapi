/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.types

/**
 * Represents the different types of `OAuth2` flows.
 */
internal enum class OAuthFlowType(internal val value: String) {
    /** The OAuth2 Implicit flow. */
    IMPLICIT(value = "implicit"),

    /** The OAuth2 Password flow. */
    PASSWORD(value = "password"),

    /** The OAuth2 Client Credentials flow. */
    CLIENT_CREDENTIALS(value = "clientCredentials"),

    /** The OAuth2 Authorization Code flow. */
    AUTHORIZATION_CODE(value = "authorizationCode");

    /** Returns the string value representing the OpenAPI type. */
    operator fun invoke(): String = value

    /** Returns the string value representing the OpenAPI type. */
    override fun toString(): String = value
}
