/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.security.builder

import io.github.perracodex.kopapi.dsl.marker.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.builder.operation.ApiOperationBuilder
import io.github.perracodex.kopapi.dsl.operation.element.ApiSecurityScheme
import io.github.perracodex.kopapi.type.OAuthFlowType
import io.github.perracodex.kopapi.util.sanitize
import io.github.perracodex.kopapi.util.string.MultilineString
import io.github.perracodex.kopapi.util.trimOrNull

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
@KopapiDsl
public class OAuth2SecurityBuilder internal constructor() {
    public var description: String by MultilineString()

    /**
     * A map of OAuth2 flow types to their corresponding builders.
     * storing the configuration for each OAuth2 flow type that is defined for the security scheme.
     */
    private val flows: MutableMap<OAuthFlowType, OAuthFlowBuilder> = mutableMapOf()

    /**
     * Configures the OAuth2 `Authorization Code` flow for the security scheme.
     *
     * #### Usage
     * ```
     * authorizationCode {
     *      authorizationUrl = "https://example.com/oauth2/authorize"
     *      tokenUrl = "https://example.com/oauth2/token"
     *      refreshUrl = "https://example.com/oauth2/refresh"
     *      scope("read:pets", "read your pets")
     *      scope("write:pets", "modify pets in your account")
     * }
     * ```
     *
     * Used by confidential clients (e.g., server-side apps) to obtain tokens through a two-step process.
     * First, an authorization code is acquired, then exchanged for a token.
     *
     * @receiver [OAuthFlowBuilder] The builder used to configure the OAuth2 flow.
     */
    public fun authorizationCode(builder: OAuthFlowBuilder.() -> Unit) {
        flows[OAuthFlowType.AUTHORIZATION_CODE] = OAuthFlowBuilder().apply(builder)
    }

    /**
     * Configures the OAuth2 `Client Credentials` flow for the security scheme.
     *
     * #### Usage
     * ```
     * clientCredentials {
     *      authorizationUrl = "https://example.com/auth"
     *      tokenUrl = "https://example.com/token"
     *      refreshUrl = "https://example.com/refresh"
     *      scope(name = "admin:tools", description = "Administrate Tools")
     * }
     * ```
     *
     * For server-to-server communication, where a client can directly
     * obtain an access token using its credentials.
     *
     * @receiver [OAuthFlowBuilder] The builder used to configure the OAuth2 flow.
     */
    public fun clientCredentials(builder: OAuthFlowBuilder.() -> Unit) {
        flows[OAuthFlowType.CLIENT_CREDENTIALS] = OAuthFlowBuilder().apply(builder)
    }

    /**
     * Configures the OAuth2 `Implicit` flow for the security scheme.
     *
     * #### Usage
     * ```
     * implicit {
     *      authorizationUrl = "https://example.com/auth"
     *      tokenUrl = "https://example.com/token"
     *      refreshUrl = "https://example.com/refresh"
     *      scope(name = "view:projects", description = "View Projects")
     * }
     * ```
     *
     * Primarily for single-page applications (browser-based clients) where tokens are obtained directly
     * from the authorization URL without requiring the client secret.
     *
     * @receiver [OAuthFlowBuilder] The builder used to configure the OAuth2 flow.
     */
    public fun implicit(builder: OAuthFlowBuilder.() -> Unit) {
        flows[OAuthFlowType.IMPLICIT] = OAuthFlowBuilder().apply(builder)
    }

    /**
     * Configures the OAuth2 `Password` flow for the security scheme.
     *
     * #### Usage
     * ```
     * password {
     *      authorizationUrl = "https://example.com/auth"
     *      tokenUrl = "https://example.com/token"
     *      refreshUrl = "https://example.com/refresh"
     *      scope(name = "read:reports", description = "Read Reports")
     * }
     * ```
     *
     * Allows exchanging user credentials (username/password) for tokens,
     * typically used for first-party applications.
     *
     * @receiver [OAuthFlowBuilder] The builder used to configure the OAuth2 flow.
     */
    public fun password(builder: OAuthFlowBuilder.() -> Unit) {
        flows[OAuthFlowType.PASSWORD] = OAuthFlowBuilder().apply(builder)
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
            schemeName = name.sanitize(),
            description = description.trimOrNull(),
            flows = oauthFlows
        )
    }
}
