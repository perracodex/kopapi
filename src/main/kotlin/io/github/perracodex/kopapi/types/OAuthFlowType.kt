/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.types

import io.github.perracodex.kopapi.dsl.operation.elements.ApiSecurityScheme

/**
 * Enum representing the different types of OAuth2 flows.
 */
internal enum class OAuthFlowType(val value: String) {
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

    /**
     * Retrieves the corresponding OAuthFlow from the given OAuthFlows based on the flow type.
     *
     * @param flows The OAuthFlows object containing all possible flows.
     * @return The OAuthFlow corresponding to this flow type, or null if not defined.
     */
    fun getFlow(flows: ApiSecurityScheme.OAuth2.OAuthFlows): ApiSecurityScheme.OAuth2.OAuthFlow? {
        return when (this) {
            IMPLICIT -> flows.implicit
            PASSWORD -> flows.password
            CLIENT_CREDENTIALS -> flows.clientCredentials
            AUTHORIZATION_CODE -> flows.authorizationCode
        }
    }
}
