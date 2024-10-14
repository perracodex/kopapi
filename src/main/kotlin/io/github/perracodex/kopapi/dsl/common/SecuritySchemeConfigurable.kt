/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.common

import io.github.perracodex.kopapi.core.KopapiException
import io.github.perracodex.kopapi.dsl.api.builders.security.*
import io.github.perracodex.kopapi.dsl.api.elements.ApiSecurity
import io.github.perracodex.kopapi.dsl.api.types.AuthenticationMethod
import io.github.perracodex.kopapi.dsl.api.types.SecurityLocation
import io.ktor.http.*

/**
 * Abstract base class to handle security scheme configurations.
 */
public abstract class SecuritySchemeConfigurable {
    /**
     * Set of security schemes detailing with authentication.
     */
    @PublishedApi
    internal val securitySchemes: LinkedHashSet<ApiSecurity> = linkedSetOf()

    /**
     * Adds an HTTP security scheme to the API metadata (e.g., Basic, Bearer).
     *
     * #### Sample Usage
     * ```
     * httpSecurity("BasicAuth", AuthenticationMethod.BASIC) {
     *     description = "Basic Authentication"
     * }
     * ```
     *
     * @param name The name of the security scheme.
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
        val scheme: ApiSecurity = builder.build(name = name, method = method)
        addSecurityScheme(name = name, security = scheme)
    }

    /**
     * Adds an API key security scheme to the API metadata.
     *
     * #### Sample Usage
     * ```
     * apiKeySecurity("API Key", APIKeyLocation.HEADER) {
     *     description = "API Key Authentication via header."
     * }
     * ```
     *
     * @param name The name of the security scheme.
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
        location: SecurityLocation,
        configure: ApiKeySecurityBuilder.() -> Unit = {}
    ) {
        val builder: ApiKeySecurityBuilder = ApiKeySecurityBuilder().apply(configure)
        val scheme: ApiSecurity = builder.build(name = name, location = location)
        addSecurityScheme(name = name, security = scheme)
    }

    /**
     * Adds an OAuth2 security scheme to the API metadata.
     *
     * #### Sample Usage
     * ```
     * oauth2Security("OAuth2") {
     *     description = "OAuth2 Authentication."
     * }
     * ```
     *
     * @param name The name of the security scheme.
     * @param configure A lambda receiver for configuring the [OAuth2SecurityBuilder].
     *
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
        val scheme: ApiSecurity = builder.build(name = name)
        addSecurityScheme(name = name, security = scheme)
    }

    /**
     * Adds an OpenID Connect security scheme to the API metadata.
     *
     * #### Sample Usage
     * ```
     * openIdConnectSecurity("OpenID") {
     *     description = "OpenID Connect Authentication."
     *     url = Url("https://example.com/.well-known/openid-configuration")
     * }
     * ```
     *
     * @param name The name of the security scheme.
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
        val scheme: ApiSecurity = builder.build(name = name, url = url)
        addSecurityScheme(name = name, security = scheme)
    }

    /**
     * Adds a Mutual TLS security scheme to the API metadata.
     *
     * #### Sample Usage
     * ```
     * mutualTLSSecurity("MutualTLS") {
     *     description = "Mutual TLS Authentication."
     * }
     * ```
     *
     * @param name The name of the security scheme.
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
        val scheme: ApiSecurity = builder.build(name = name)
        addSecurityScheme(name = name, security = scheme)
    }

    /**
     * Adds a custom security scheme to the cache,
     * ensuring that the security scheme name is unique.
     *
     * @param name The name of the security scheme.
     * @param security The [ApiSecurity] instance to add to the cache.
     * @throws IllegalArgumentException If a security scheme with the same name already exists.
     */
    private fun addSecurityScheme(name: String, security: ApiSecurity) {
        if (securitySchemes.any { it.name.equals(other = name, ignoreCase = true) }) {
            throw KopapiException(
                "Duplicate security scheme name '$name' detected. Security scheme names must be unique."
            )
        }

        securitySchemes.add(security)
    }
}
