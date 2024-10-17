/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.common

import io.github.perracodex.kopapi.dsl.operation.builders.security.*
import io.github.perracodex.kopapi.dsl.operation.elements.ApiSecurityScheme
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.types.AuthenticationMethod
import io.github.perracodex.kopapi.types.SecurityLocation
import io.ktor.http.*

/**
 * Abstract base class to handle security scheme configurations.
 */
public abstract class SecuritySchemeConfigurable {
    /**
     * Set of security schemes detailing with authentication.
     */
    @PublishedApi
    internal val securitySchemes: LinkedHashSet<ApiSecurityScheme> = linkedSetOf()

    /**
     * Flag to indicate that no security is required for the API operation.
     * Once set to `true`, all security schemes are ignored.
     */
    internal var noSecurity: Boolean = false

    /**
     * Adds an HTTP security scheme to the API metadata (e.g., Basic, Bearer).
     *
     * #### Sample Usage
     * ```
     * httpSecurity(name = "BasicAuth", method = AuthenticationMethod.BASIC) {
     *     description = "Basic Authentication"
     * }
     * ```
     *
     * @param name The unique name of the security scheme.
     * @param method The [AuthenticationMethod] of the security scheme.
     * @param configure A lambda receiver for configuring the [HttpSecurityBuilder].
     *
     * @see [apiKeySecurity]
     * @see [mutualTLSSecurity]
     * @see [oauth2Security]
     * @see [openIdConnectSecurity]
     */
    public fun httpSecurity(
        name: String,
        method: AuthenticationMethod,
        configure: HttpSecurityBuilder.() -> Unit = {}
    ) {
        val builder: HttpSecurityBuilder = HttpSecurityBuilder().apply(configure)
        val scheme: ApiSecurityScheme = builder.build(name = name, method = method)
        addSecurityScheme(scheme = scheme)
    }

    /**
     * Adds an API key security scheme to the API metadata.
     *
     * #### Sample Usage
     * ```
     * apiKeySecurity(
     *      name = "API Key",
     *      apiKeyName = "X-API-Key",
     *      location = APIKeyLocation.HEADER
     * ) {
     *     description = "API Key Authentication via header."
     * }
     * ```
     *
     * @param name The unique name of the security scheme.
     * @param apiKeyName The name of the header, query parameter, or cookie parameter where the API key is passed.
     * @param location The [SecurityLocation] where the API key is passed.
     * @param configure A lambda receiver for configuring the [ApiKeySecurityBuilder].
     *
     * @see [httpSecurity]
     * @see [mutualTLSSecurity]
     * @see [oauth2Security]
     * @see [openIdConnectSecurity]
     */
    public fun apiKeySecurity(
        name: String,
        apiKeyName: String,
        location: SecurityLocation,
        configure: ApiKeySecurityBuilder.() -> Unit = {}
    ) {
        val builder: ApiKeySecurityBuilder = ApiKeySecurityBuilder().apply(configure)
        val scheme: ApiSecurityScheme = builder.build(name = name, apiKeyName = apiKeyName, location = location)
        addSecurityScheme(scheme = scheme)
    }

    /**
     * Adds an OAuth2 security scheme to the API metadata.
     *
     * #### Sample Usage
     * ```
     * oauth2Security(name = "OAuth2") {
     *     description = "OAuth2 Authentication."
     *
     *     authorizationCode {
     *         authorizationUrl = "https://example.com/auth"
     *         tokenUrl = "https://example.com/token"
     *         refreshUrl = "https://example.com/refresh"
     *         scope(name = "read:employees", description = "Read Data")
     *         scope(name = "write:employees", description = "Modify Data")
     *     }
     *
     *     implicit {
     *         authorizationUrl = "https://example.com/auth"
     *         scope(name = "view:projects", description = "View Projects")
     *     }
     *
     *     password {
     *         tokenUrl = "https://example.com/token"
     *         scope(name = "access:reports", description = "Access Reports")
     *     }
     *
     *     clientCredentials {
     *         tokenUrl = "https://example.com/token"
     *         scope(name = "admin:tools", description = "Administrate Tools")
     *     }
     * }
     * ```
     *
     * @param name The unique name of the security scheme.
     * @param configure A lambda receiver for configuring the [OAuth2SecurityBuilder].
     *
     * @see [OAuth2SecurityBuilder]
     * @see [OAuthFlowBuilder]
     * @see [apiKeySecurity]
     * @see [httpSecurity]
     * @see [mutualTLSSecurity]
     * @see [openIdConnectSecurity]
     */
    public fun oauth2Security(
        name: String,
        configure: OAuth2SecurityBuilder.() -> Unit = {}
    ) {
        val builder: OAuth2SecurityBuilder = OAuth2SecurityBuilder().apply(configure)
        val scheme: ApiSecurityScheme = builder.build(name = name)

        // Collect all scopes from all flows.
        val allScopes: List<String> = builder.getAllScopes()
        if (allScopes.isEmpty()) {
            throw KopapiException("OAuth2 security scheme '$name' must have at least one scope defined in its flows.")
        }

        addSecurityScheme(scheme = scheme)
    }

    /**
     * Adds an OpenID Connect security scheme to the API metadata.
     *
     * #### Sample Usage
     * ```
     * openIdConnectSecurity(name = "OpenID") {
     *     description = "OpenID Connect Authentication."
     *     url = Url("https://example.com/.well-known/openid-configuration")
     * }
     * ```
     *
     * @param name The unique name of the security scheme.
     * @param url The [Url] for the OpenID Connect configuration.
     * @param configure A lambda receiver for configuring the [OpenIdConnectSecurityBuilder].
     *
     * @see [apiKeySecurity]
     * @see [httpSecurity]
     * @see [mutualTLSSecurity]
     * @see [oauth2Security]
     */
    public fun openIdConnectSecurity(
        name: String,
        url: Url,
        configure: OpenIdConnectSecurityBuilder.() -> Unit = {}
    ) {
        val builder: OpenIdConnectSecurityBuilder = OpenIdConnectSecurityBuilder().apply(configure)
        val scheme: ApiSecurityScheme = builder.build(name = name, url = url)
        addSecurityScheme(scheme = scheme)
    }

    /**
     * Adds a Mutual TLS security scheme to the API metadata.
     *
     * #### Sample Usage
     * ```
     * mutualTLSSecurity(name = "MutualTLS") {
     *     description = "Mutual TLS Authentication."
     * }
     * ```
     *
     * @param name The unique name of the security scheme.
     * @param configure A lambda receiver for configuring the [MutualTLSSecurityBuilder].
     *
     * @see [apiKeySecurity]
     * @see [httpSecurity]
     * @see [oauth2Security]
     * @see [openIdConnectSecurity]
     */
    public fun mutualTLSSecurity(
        name: String,
        configure: MutualTLSSecurityBuilder.() -> Unit = {}
    ) {
        val builder: MutualTLSSecurityBuilder = MutualTLSSecurityBuilder()
            .apply(configure)
        val scheme: ApiSecurityScheme = builder.build(name = name)
        addSecurityScheme(scheme = scheme)
    }

    /**
     * Adds a custom security scheme to the cache,
     * ensuring that the security scheme name is unique.
     *
     * @param scheme The [ApiSecurityScheme] instance to add to the cache.
     * @throws KopapiException If a security scheme with the same name already exists.
     */
    private fun addSecurityScheme(scheme: ApiSecurityScheme) {
        if (noSecurity) {
            securitySchemes.clear()
            return
        }

        if (securitySchemes.any { it.schemeName.equals(other = scheme.schemeName, ignoreCase = true) }) {
            throw KopapiException(
                "Attempting to register security scheme with name '${scheme.schemeName}' more than once.\n" +
                        "Security scheme `names` must be unique across the entire API, " +
                        "both globally and for all Routes."
            )
        }

        securitySchemes.add(scheme)
    }
}
