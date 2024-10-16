/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.api.builders.security

import io.github.perracodex.kopapi.dsl.api.builders.ApiOperationBuilder
import io.github.perracodex.kopapi.dsl.api.elements.ApiSecurityScheme
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
     * Configures the OAuth2 Implicit flow for the security scheme.
     */
    public fun implicit(configure: OAuthFlowBuilder.() -> Unit) {
        flows[OAuthFlowType.IMPLICIT] = OAuthFlowBuilder().apply(configure)
    }

    /**
     * Configures the OAuth2 Password flow for the security scheme.
     */
    public fun password(configure: OAuthFlowBuilder.() -> Unit) {
        flows[OAuthFlowType.PASSWORD] = OAuthFlowBuilder().apply(configure)
    }

    /**
     * Configures the OAuth2 Client Credentials flow for the security scheme.
     */
    public fun clientCredentials(configure: OAuthFlowBuilder.() -> Unit) {
        flows[OAuthFlowType.CLIENT_CREDENTIALS] = OAuthFlowBuilder().apply(configure)
    }

    /**
     * Configures the OAuth2 Authorization Code flow for the security scheme.
     */
    public fun authorizationCode(configure: OAuthFlowBuilder.() -> Unit) {
        flows[OAuthFlowType.AUTHORIZATION_CODE] = OAuthFlowBuilder().apply(configure)
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
            implicit = flows[OAuthFlowType.IMPLICIT]?.build(),
            password = flows[OAuthFlowType.PASSWORD]?.build(),
            clientCredentials = flows[OAuthFlowType.CLIENT_CREDENTIALS]?.build(),
            authorizationCode = flows[OAuthFlowType.AUTHORIZATION_CODE]?.build()
        )

        return ApiSecurityScheme.OAuth2(
            schemeName = name.trim(),
            description = description.trimOrNull(),
            flows = oauthFlows
        )
    }

    /**
     * Retrieves all scopes defined across all OAuth2 flows.
     *
     * @return A list of all unique scopes defined in the flows.
     */
    internal fun getAllScopes(): List<String> {
        return flows.values.flatMap { it.getScopes() }.distinct()
    }

    /**
     * Enum representing the different types of OAuth2 flows.
     */
    private enum class OAuthFlowType {
        /** The OAuth2 Implicit flow. */
        IMPLICIT,

        /** The OAuth2 Password flow. */
        PASSWORD,

        /** The OAuth2 Client Credentials flow. */
        CLIENT_CREDENTIALS,

        /** The OAuth2 Authorization Code flow. */
        AUTHORIZATION_CODE
    }
}
