/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.common.security

import io.github.perracodex.kopapi.dsl.operation.builders.ApiOperationBuilder
import io.github.perracodex.kopapi.dsl.operation.elements.ApiSecurityScheme
import io.github.perracodex.kopapi.types.OAuthFlowType
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull

/**
 * Builds an OAuth2 security scheme in an API endpoint's metadata.
 *
 * @property description A description of the security scheme.
 *
 * @see [OAuthFlowBuilder]
 * @see [ApiOperationBuilder.oauth2Security]
 * @see [ApiKeySecurityBuilder]
 * @see [HttpSecurityBuilder]
 * @see [OpenIdConnectSecurityBuilder]
 * @see [MutualTLSSecurityBuilder]
 */
public class OAuth2SecurityBuilder {
    public var description: String by MultilineString()

    /**
     * A map of OAuth2 flow types to their corresponding builders.
     * storing the configuration for each OAuth2 flow type that is defined for the security scheme.
     */
    private val flows: MutableMap<OAuthFlowType, OAuthFlowBuilder> = mutableMapOf()

    /**
     * Configures the OAuth2 `Authorization Code` flow for the security scheme.
     *
     * Used by confidential clients (e.g., server-side apps) to obtain tokens through a two-step process.
     * First, an authorization code is acquired, then exchanged for a token.
     */
    public fun authorizationCode(configure: OAuthFlowBuilder.() -> Unit) {
        flows[OAuthFlowType.AUTHORIZATION_CODE] = OAuthFlowBuilder().apply(configure)
    }

    /**
     * Configures the OAuth2 `Client Credentials` flow for the security scheme.
     *
     * For server-to-server communication, where a client can directly
     * obtain an access token using its credentials.
     */
    public fun clientCredentials(configure: OAuthFlowBuilder.() -> Unit) {
        flows[OAuthFlowType.CLIENT_CREDENTIALS] = OAuthFlowBuilder().apply(configure)
    }

    /**
     * Configures the OAuth2 `Implicit` flow for the security scheme.
     *
     * Primarily for single-page applications (browser-based clients) where tokens are obtained directly
     * from the authorization URL without requiring the client secret.
     */
    public fun implicit(configure: OAuthFlowBuilder.() -> Unit) {
        flows[OAuthFlowType.IMPLICIT] = OAuthFlowBuilder().apply(configure)
    }

    /**
     * Configures the OAuth2 `Password` flow for the security scheme.
     *
     * Allows exchanging user credentials (username/password) for tokens,
     * typically used for first-party applications.
     */
    public fun password(configure: OAuthFlowBuilder.() -> Unit) {
        flows[OAuthFlowType.PASSWORD] = OAuthFlowBuilder().apply(configure)
    }

    /**
     * Builds an [ApiSecurityScheme] instance from the current builder state.
     *
     * @param name The name of the security scheme.
     * @return The constructed [ApiSecurityScheme] instance.
     */
    @PublishedApi
    internal fun build(name: String): ApiSecurityScheme {
        val oauthFlows = ApiSecurityScheme.OAuth2.OAuthFlows(
            authorizationCode = flows[OAuthFlowType.AUTHORIZATION_CODE]?.build(),
            clientCredentials = flows[OAuthFlowType.CLIENT_CREDENTIALS]?.build(),
            implicit = flows[OAuthFlowType.IMPLICIT]?.build(),
            password = flows[OAuthFlowType.PASSWORD]?.build()
        )

        return ApiSecurityScheme.OAuth2(
            schemeName = name.trim(),
            description = description.trimOrNull(),
            flows = oauthFlows
        )
    }
}
