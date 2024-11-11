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
internal class SecurityDelegate : ISecurityConfigurable {
    /** The cache for security schemes. */
    val securitySchemes: LinkedHashSet<ApiSecurityScheme> = linkedSetOf()

    /**
     * Flag to indicate that no security is required for the API operation.
     * Once set to `true`, all security schemes are ignored.
     */
    var skipSecurity: Boolean = false

    override fun basicSecurity(
        name: String,
        builder: HttpSecurityBuilder.() -> Unit
    ) {
        val schemeBuilder: HttpSecurityBuilder = HttpSecurityBuilder().apply(builder)
        val scheme: ApiSecurityScheme = schemeBuilder.build(name = name, method = AuthMethod.BASIC)
        addSecurityScheme(scheme = scheme)
    }

    override fun bearerSecurity(
        name: String,
        builder: HttpSecurityBuilder.() -> Unit
    ) {
        val schemeBuilder: HttpSecurityBuilder = HttpSecurityBuilder().apply(builder)
        val scheme: ApiSecurityScheme = schemeBuilder.build(name = name, method = AuthMethod.BEARER)
        addSecurityScheme(scheme = scheme)
    }

    override fun digestSecurity(
        name: String,
        builder: HttpSecurityBuilder.() -> Unit
    ) {
        val schemeBuilder: HttpSecurityBuilder = HttpSecurityBuilder().apply(builder)
        val scheme: ApiSecurityScheme = schemeBuilder.build(name = name, method = AuthMethod.DIGEST)
        addSecurityScheme(scheme = scheme)
    }

    override fun headerApiKeySecurity(
        name: String,
        key: String,
        builder: ApiKeySecurityBuilder.() -> Unit
    ) {
        val schemeBuilder: ApiKeySecurityBuilder = ApiKeySecurityBuilder().apply(builder)
        val scheme: ApiSecurityScheme = schemeBuilder.build(
            name = name,
            key = key,
            location = SecurityLocation.HEADER
        )
        addSecurityScheme(scheme = scheme)
    }

    override fun queryApiKeySecurity(
        name: String,
        key: String,
        builder: ApiKeySecurityBuilder.() -> Unit
    ) {
        val schemeBuilder: ApiKeySecurityBuilder = ApiKeySecurityBuilder().apply(builder)
        val scheme: ApiSecurityScheme = schemeBuilder.build(
            name = name,
            key = key,
            location = SecurityLocation.QUERY
        )
        addSecurityScheme(scheme = scheme)
    }

    override fun cookieApiKeySecurity(
        name: String,
        key: String,
        builder: ApiKeySecurityBuilder.() -> Unit
    ) {
        val schemeBuilder: ApiKeySecurityBuilder = ApiKeySecurityBuilder().apply(builder)
        val scheme: ApiSecurityScheme = schemeBuilder.build(
            name = name,
            key = key,
            location = SecurityLocation.COOKIE
        )
        addSecurityScheme(scheme = scheme)
    }

    override fun oauth2Security(
        name: String,
        builder: OAuth2SecurityBuilder.() -> Unit
    ) {
        val schemeBuilder: OAuth2SecurityBuilder = OAuth2SecurityBuilder().apply(builder)
        val scheme: ApiSecurityScheme = schemeBuilder.build(name = name)
        addSecurityScheme(scheme = scheme)
    }

    override fun openIdConnectSecurity(
        name: String,
        url: Url,
        builder: OpenIdConnectSecurityBuilder.() -> Unit
    ) {
        val schemeBuilder: OpenIdConnectSecurityBuilder = OpenIdConnectSecurityBuilder().apply(builder)
        val scheme: ApiSecurityScheme = schemeBuilder.build(name = name, url = url)
        addSecurityScheme(scheme = scheme)
    }

    override fun mutualTLSSecurity(
        name: String,
        builder: MutualTLSSecurityBuilder.() -> Unit
    ) {
        val schemeBuilder: MutualTLSSecurityBuilder = MutualTLSSecurityBuilder().apply(builder)
        val scheme: ApiSecurityScheme = schemeBuilder.build(name = name)
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
