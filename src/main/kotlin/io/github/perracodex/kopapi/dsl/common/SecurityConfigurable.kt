/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.common

import io.github.perracodex.kopapi.dsl.common.security.*
import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.elements.ApiSecurityScheme
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.types.AuthMethod
import io.github.perracodex.kopapi.types.SecurityLocation
import io.ktor.http.*

/**
 * Abstract base class to handle security scheme configurations.
 */
@KopapiDsl
public abstract class SecurityConfigurable {
    @Suppress("PropertyName")
    internal val _securityConfig: Config = Config()

    /**
     * Adds a `Basic` HTTP security scheme to the API metadata.
     *
     * #### Sample Usage
     * ```
     * basicSecurity(name = "MyBasicAuth") {
     *     description = "Basic Authentication"
     * }
     * ```
     *
     * @param name The unique name of the security scheme.
     * @param configure A lambda receiver for configuring the [HttpSecurityBuilder].
     *
     * @see [bearerSecurity]
     * @see [digestSecurity]
     */
    public fun basicSecurity(
        name: String,
        configure: HttpSecurityBuilder.() -> Unit = {}
    ) {
        val builder: HttpSecurityBuilder = HttpSecurityBuilder().apply(configure)
        val scheme: ApiSecurityScheme = builder.build(name = name, method = AuthMethod.BASIC)
        _securityConfig.addSecurityScheme(scheme = scheme)
    }

    /**
     * Adds an `Bearer` HTTP security scheme to the API metadata.
     *
     * #### Sample Usage
     * ```
     * bearerSecurity(name = "MyBearerAuth") {
     *     description = "Bearer Authentication"
     * }
     * ```
     *
     * @param name The unique name of the security scheme.
     * @param configure A lambda receiver for configuring the [HttpSecurityBuilder].
     *
     * @see [basicSecurity]
     * @see [digestSecurity]
     */
    public fun bearerSecurity(
        name: String,
        configure: HttpSecurityBuilder.() -> Unit = {}
    ) {
        val builder: HttpSecurityBuilder = HttpSecurityBuilder().apply(configure)
        val scheme: ApiSecurityScheme = builder.build(name = name, method = AuthMethod.BEARER)
        _securityConfig.addSecurityScheme(scheme = scheme)
    }

    /**
     * Adds an `Digest` HTTP security scheme to the API metadata.
     *
     * #### Sample Usage
     * ```
     * digestSecurity(name = "MyDigestAuth") {
     *     description = "Digest Authentication"
     * }
     * ```
     *
     * @param name The unique name of the security scheme.
     * @param configure A lambda receiver for configuring the [HttpSecurityBuilder].
     *
     * @see [basicSecurity]
     * @see [bearerSecurity]
     */
    public fun digestSecurity(
        name: String,
        configure: HttpSecurityBuilder.() -> Unit = {}
    ) {
        val builder: HttpSecurityBuilder = HttpSecurityBuilder().apply(configure)
        val scheme: ApiSecurityScheme = builder.build(name = name, method = AuthMethod.DIGEST)
        _securityConfig.addSecurityScheme(scheme = scheme)
    }

    /**
     * Adds an API key security scheme passed via header to the API metadata.
     *
     * #### Sample Usage
     * ```
     * headerApiKeySecurity(name = "Header API Key", key = "X-API-Key") {
     *     description = "API Key Authentication via header."
     * }
     * ```
     *
     * @param name The unique name of the security scheme.
     * @param key The name of the header parameter where the API key is passed.
     * @param configure A lambda receiver for configuring the [ApiKeySecurityBuilder].
     *
     * @see [queryApiKeySecurity]
     * @see [cookieApiKeySecurity]
     */
    public fun headerApiKeySecurity(
        name: String,
        key: String,
        configure: ApiKeySecurityBuilder.() -> Unit = {}
    ) {
        val builder: ApiKeySecurityBuilder = ApiKeySecurityBuilder().apply(configure)
        val scheme: ApiSecurityScheme = builder.build(
            name = name,
            key = key,
            location = SecurityLocation.HEADER
        )
        _securityConfig.addSecurityScheme(scheme = scheme)
    }

    /**
     * Adds an API key security scheme passed via query to the API metadata.
     *
     * #### Sample Usage
     * ```
     * queryApiKeySecurity(name = "Query API Key", key = "X-API-Key") {
     *     description = "API Key Authentication via query."
     * }
     * ```
     *
     * @param name The unique name of the security scheme.
     * @param key The name of the query parameter where the API key is passed.
     * @param configure A lambda receiver for configuring the [ApiKeySecurityBuilder].
     *
     * @see [headerApiKeySecurity]
     * @see [cookieApiKeySecurity]
     */
    public fun queryApiKeySecurity(
        name: String,
        key: String,
        configure: ApiKeySecurityBuilder.() -> Unit = {}
    ) {
        val builder: ApiKeySecurityBuilder = ApiKeySecurityBuilder().apply(configure)
        val scheme: ApiSecurityScheme = builder.build(
            name = name,
            key = key,
            location = SecurityLocation.QUERY
        )
        _securityConfig.addSecurityScheme(scheme = scheme)
    }

    /**
     * Adds an API key security scheme passed via cookie to the API metadata.
     *
     * #### Sample Usage
     * ```
     * cookieApiKeySecurity(name = "Cookie API Key", key = "X-API-Key") {
     *     description = "API Key Authentication via cookie."
     * }
     * ```
     *
     * @param name The unique name of the security scheme.
     * @param key The name of the cookie parameter where the API key is passed.
     * @param configure A lambda receiver for configuring the [ApiKeySecurityBuilder].
     *
     * @see [headerApiKeySecurity]
     * @see [queryApiKeySecurity]
     */
    public fun cookieApiKeySecurity(
        name: String,
        key: String,
        configure: ApiKeySecurityBuilder.() -> Unit = {}
    ) {
        val builder: ApiKeySecurityBuilder = ApiKeySecurityBuilder().apply(configure)
        val scheme: ApiSecurityScheme = builder.build(
            name = name,
            key = key,
            location = SecurityLocation.COOKIE
        )
        _securityConfig.addSecurityScheme(scheme = scheme)
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
     *     clientCredentials {
     *         tokenUrl = "https://example.com/token"
     *         scope(name = "admin:tools", description = "Administrate Tools")
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
     * }
     * ```
     *
     * @param name The unique name of the security scheme.
     * @param configure A lambda receiver for configuring the [OAuth2SecurityBuilder].
     *
     * @see [OAuth2SecurityBuilder]
     * @see [OAuthFlowBuilder]
     * @see [basicSecurity]
     * @see [bearerSecurity]
     * @see [digestSecurity]
     * @see [headerApiKeySecurity]
     * @see [cookieApiKeySecurity]
     * @see [queryApiKeySecurity]
     * @see [mutualTLSSecurity]
     * @see [openIdConnectSecurity]
     */
    public fun oauth2Security(
        name: String,
        configure: OAuth2SecurityBuilder.() -> Unit = {}
    ) {
        val builder: OAuth2SecurityBuilder = OAuth2SecurityBuilder().apply(configure)
        val scheme: ApiSecurityScheme = builder.build(name = name)
        _securityConfig.addSecurityScheme(scheme = scheme)
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
     * @see [basicSecurity]
     * @see [bearerSecurity]
     * @see [digestSecurity]
     * @see [headerApiKeySecurity]
     * @see [cookieApiKeySecurity]
     * @see [queryApiKeySecurity]
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
        _securityConfig.addSecurityScheme(scheme = scheme)
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
     * @see [basicSecurity]
     * @see [bearerSecurity]
     * @see [digestSecurity]
     * @see [headerApiKeySecurity]
     * @see [cookieApiKeySecurity]
     * @see [queryApiKeySecurity]
     * @see [oauth2Security]
     * @see [openIdConnectSecurity]
     */
    public fun mutualTLSSecurity(
        name: String,
        configure: MutualTLSSecurityBuilder.() -> Unit = {}
    ) {
        val builder: MutualTLSSecurityBuilder = MutualTLSSecurityBuilder().apply(configure)
        val scheme: ApiSecurityScheme = builder.build(name = name)
        _securityConfig.addSecurityScheme(scheme = scheme)
    }

    internal class Config {
        /**
         * Set of security schemes detailing with authentication.
         */
        val securitySchemes: LinkedHashSet<ApiSecurityScheme> = linkedSetOf()

        /**
         * Flag to indicate that no security is required for the API operation.
         * Once set to `true`, all security schemes are ignored.
         */
        var skipSecurity: Boolean = false

        /**
         * Adds a custom security scheme to the cache,
         * ensuring that the security scheme name is unique.
         *
         * @param scheme The [ApiSecurityScheme] instance to add to the cache.
         * @throws KopapiException If a security scheme with the same name already exists.
         */
        fun addSecurityScheme(scheme: ApiSecurityScheme) {
            if (skipSecurity) {
                securitySchemes.clear()
                return
            }

            if (securitySchemes.any { it.schemeName.equals(other = scheme.schemeName, ignoreCase = true) }) {
                throw KopapiException(
                    "Attempting to register security scheme with name '${scheme.schemeName}' more than once.\n" +
                            "The OpenAPI specification requires every Security Scheme name to be unique throughout the entire API."
                )
            }

            securitySchemes.add(scheme)
        }
    }
}
