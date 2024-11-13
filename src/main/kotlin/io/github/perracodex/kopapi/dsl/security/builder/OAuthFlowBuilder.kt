/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.security.builder

import io.github.perracodex.kopapi.dsl.marker.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.element.ApiSecurityScheme
import io.github.perracodex.kopapi.util.trimOrNull

/**
 * Builder for configuring an OAuth2 flow.
 *
 * @property authorizationUrl The authorization URL to be used for this flow (required for certain flows).
 * @property tokenUrl The token URL to be used for this flow (required for certain flows).
 * @property refreshUrl The refresh URL to be used for obtaining refresh tokens (optional).
 *
 * @see [OAuth2SecurityBuilder.authorizationCode]
 * @see [OAuth2SecurityBuilder.clientCredentials]
 * @see [OAuth2SecurityBuilder.implicit]
 * @see [OAuth2SecurityBuilder.password]
 */
@KopapiDsl
public class OAuthFlowBuilder internal constructor() {
    public var authorizationUrl: String? = null
    public var tokenUrl: String? = null
    public var refreshUrl: String? = null

    /** Hold constructed scopes for the OAuth2 flow. */
    private val scopes: MutableMap<String, String> = mutableMapOf()

    /**
     * Adds a scope to the OAuth2 flow.
     *
     * @param name The name of the scope.
     * @param description A description of the scope.
     */
    public fun scope(name: String, description: String) {
        scopes[name] = description
    }

    /**
     * Builds an [ApiSecurityScheme.OAuth2.OAuthFlow] instance from the current builder state.
     *
     * @return The constructed [ApiSecurityScheme.OAuth2.OAuthFlow] instance.
     */
    @PublishedApi
    internal fun build(): ApiSecurityScheme.OAuth2.OAuthFlow {
        return ApiSecurityScheme.OAuth2.OAuthFlow(
            authorizationUrl = authorizationUrl.trimOrNull(),
            tokenUrl = tokenUrl.trimOrNull(),
            refreshUrl = refreshUrl.trimOrNull(),
            scopes = scopes
        )
    }
}
