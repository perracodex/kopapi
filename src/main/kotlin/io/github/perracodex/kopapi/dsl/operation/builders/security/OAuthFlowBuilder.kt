/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.security

import io.github.perracodex.kopapi.dsl.operation.elements.ApiSecurityScheme
import io.github.perracodex.kopapi.system.KopapiException

/**
 * Builder for configuring an OAuth2 flow.
 *
 * @property authorizationUrl The authorization URL to be used for this flow (required for certain flows).
 * @property tokenUrl The token URL to be used for this flow (required for certain flows).
 * @property refreshUrl The refresh URL to be used for obtaining refresh tokens (optional).
 * @property scopes The scopes defined for this flow.
 */
public class OAuthFlowBuilder {
    public var authorizationUrl: String? = null
    public var tokenUrl: String? = null
    public var refreshUrl: String? = null
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
        if (scopes.isEmpty()) {
            throw KopapiException("At least one scope must be defined for OAuth2 flow.")
        }
        return ApiSecurityScheme.OAuth2.OAuthFlow(
            authorizationUrl = authorizationUrl,
            tokenUrl = tokenUrl,
            refreshUrl = refreshUrl,
            scopes = scopes
        )
    }

    /**
     * Retrieves all scope names defined in this flow.
     *
     * @return A list of scope names.
     */
    internal fun getScopes(): List<String> {
        return scopes.keys.toList()
    }
}
