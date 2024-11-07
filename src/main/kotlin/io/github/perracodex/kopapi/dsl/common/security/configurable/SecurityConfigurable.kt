/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.common.security.configurable

import io.github.perracodex.kopapi.dsl.common.security.*
import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.elements.ApiSecurityScheme
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.types.AuthMethod
import io.github.perracodex.kopapi.types.SecurityLocation
import io.ktor.http.*

/**
 * Configurable handling security scheme registration.
 */
@KopapiDsl
internal class SecurityConfigurable : ISecurityConfigurable {
    /** The cache for security schemes. */
    val securitySchemes: LinkedHashSet<ApiSecurityScheme> = linkedSetOf()

    /**
     * Flag to indicate that no security is required for the API operation.
     * Once set to `true`, all security schemes are ignored.
     */
    var skipSecurity: Boolean = false

    override fun basicSecurity(
        name: String,
        configure: HttpSecurityBuilder.() -> Unit
    ) {
        val builder: HttpSecurityBuilder = HttpSecurityBuilder().apply(configure)
        val scheme: ApiSecurityScheme = builder.build(name = name, method = AuthMethod.BASIC)
        addSecurityScheme(scheme = scheme)
    }

    override fun bearerSecurity(
        name: String,
        configure: HttpSecurityBuilder.() -> Unit
    ) {
        val builder: HttpSecurityBuilder = HttpSecurityBuilder().apply(configure)
        val scheme: ApiSecurityScheme = builder.build(name = name, method = AuthMethod.BEARER)
        addSecurityScheme(scheme = scheme)
    }

    override fun digestSecurity(
        name: String,
        configure: HttpSecurityBuilder.() -> Unit
    ) {
        val builder: HttpSecurityBuilder = HttpSecurityBuilder().apply(configure)
        val scheme: ApiSecurityScheme = builder.build(name = name, method = AuthMethod.DIGEST)
        addSecurityScheme(scheme = scheme)
    }

    override fun headerApiKeySecurity(
        name: String,
        key: String,
        configure: ApiKeySecurityBuilder.() -> Unit
    ) {
        val builder: ApiKeySecurityBuilder = ApiKeySecurityBuilder().apply(configure)
        val scheme: ApiSecurityScheme = builder.build(
            name = name,
            key = key,
            location = SecurityLocation.HEADER
        )
        addSecurityScheme(scheme = scheme)
    }

    override fun queryApiKeySecurity(
        name: String,
        key: String,
        configure: ApiKeySecurityBuilder.() -> Unit
    ) {
        val builder: ApiKeySecurityBuilder = ApiKeySecurityBuilder().apply(configure)
        val scheme: ApiSecurityScheme = builder.build(
            name = name,
            key = key,
            location = SecurityLocation.QUERY
        )
        addSecurityScheme(scheme = scheme)
    }

    override fun cookieApiKeySecurity(
        name: String,
        key: String,
        configure: ApiKeySecurityBuilder.() -> Unit
    ) {
        val builder: ApiKeySecurityBuilder = ApiKeySecurityBuilder().apply(configure)
        val scheme: ApiSecurityScheme = builder.build(
            name = name,
            key = key,
            location = SecurityLocation.COOKIE
        )
        addSecurityScheme(scheme = scheme)
    }

    override fun oauth2Security(
        name: String,
        configure: OAuth2SecurityBuilder.() -> Unit
    ) {
        val builder: OAuth2SecurityBuilder = OAuth2SecurityBuilder().apply(configure)
        val scheme: ApiSecurityScheme = builder.build(name = name)
        addSecurityScheme(scheme = scheme)
    }

    override fun openIdConnectSecurity(
        name: String,
        url: Url,
        configure: OpenIdConnectSecurityBuilder.() -> Unit
    ) {
        val builder: OpenIdConnectSecurityBuilder = OpenIdConnectSecurityBuilder().apply(configure)
        val scheme: ApiSecurityScheme = builder.build(name = name, url = url)
        addSecurityScheme(scheme = scheme)
    }

    override fun mutualTLSSecurity(
        name: String,
        configure: MutualTLSSecurityBuilder.() -> Unit
    ) {
        val builder: MutualTLSSecurityBuilder = MutualTLSSecurityBuilder().apply(configure)
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
