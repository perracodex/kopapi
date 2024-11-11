/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.common.security.configurable

import io.github.perracodex.kopapi.dsl.common.security.*
import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.ktor.http.*

/**
 * Handles the registration of security schemes.
 */
@KopapiDsl
public interface ISecurityConfigurable {
    /**
     * Adds a `Basic` HTTP security scheme to the API metadata.
     *
     * #### Usage
     * ```
     * basicSecurity(name = "MyBasicAuth") {
     *     description = "Basic Authentication"
     * }
     * ```
     *
     * @receiver [HttpSecurityBuilder] The builder used to configure the security scheme.
     *
     * @param name The unique name of the security scheme.
     *
     * @see [bearerSecurity]
     * @see [digestSecurity]
     */
    public fun basicSecurity(
        name: String,
        builder: HttpSecurityBuilder.() -> Unit = {}
    )

    /**
     * Adds an `Bearer` HTTP security scheme to the API metadata.
     *
     * #### Usage
     * ```
     * bearerSecurity(name = "MyBearerAuth") {
     *     description = "Bearer Authentication"
     * }
     * ```
     *
     * @receiver [HttpSecurityBuilder] The builder used to configure the security scheme.
     *
     * @param name The unique name of the security scheme.
     *
     * @see [basicSecurity]
     * @see [digestSecurity]
     */
    public fun bearerSecurity(
        name: String,
        builder: HttpSecurityBuilder.() -> Unit = {}
    )

    /**
     * Adds an `Digest` HTTP security scheme to the API metadata.
     *
     * #### Usage
     * ```
     * digestSecurity(name = "MyDigestAuth") {
     *     description = "Digest Authentication"
     * }
     * ```
     *
     * @receiver [HttpSecurityBuilder] The builder used to configure the security scheme.
     *
     * @param name The unique name of the security scheme.
     *
     * @see [basicSecurity]
     * @see [bearerSecurity]
     */
    public fun digestSecurity(
        name: String,
        builder: HttpSecurityBuilder.() -> Unit = {}
    )

    /**
     * Adds an API key security scheme passed via header to the API metadata.
     *
     * #### Usage
     * ```
     * headerApiKeySecurity(name = "Header API Key", key = "X-API-Key") {
     *     description = "API Key Authentication via header."
     * }
     * ```
     *
     * @receiver [ApiKeySecurityBuilder] The builder used to configure the security scheme.
     *
     * @param name The unique name of the security scheme.
     * @param key The name of the header parameter where the API key is passed.
     *
     * @see [queryApiKeySecurity]
     * @see [cookieApiKeySecurity]
     */
    public fun headerApiKeySecurity(
        name: String,
        key: String,
        builder: ApiKeySecurityBuilder.() -> Unit = {}
    )

    /**
     * Adds an API key security scheme passed via query to the API metadata.
     *
     * #### Usage
     * ```
     * queryApiKeySecurity(name = "Query API Key", key = "X-API-Key") {
     *     description = "API Key Authentication via query."
     * }
     * ```
     *
     * @receiver [ApiKeySecurityBuilder] The builder used to configure the security scheme.
     *
     * @param name The unique name of the security scheme.
     * @param key The name of the query parameter where the API key is passed.
     *
     * @see [headerApiKeySecurity]
     * @see [cookieApiKeySecurity]
     */
    public fun queryApiKeySecurity(
        name: String,
        key: String,
        builder: ApiKeySecurityBuilder.() -> Unit = {}
    )

    /**
     * Adds an API key security scheme passed via cookie to the API metadata.
     *
     * #### Usage
     * ```
     * cookieApiKeySecurity(name = "Cookie API Key", key = "X-API-Key") {
     *     description = "API Key Authentication via cookie."
     * }
     * ```
     *
     * @receiver [ApiKeySecurityBuilder] The builder used to configure the security scheme.
     *
     * @param name The unique name of the security scheme.
     * @param key The name of the cookie parameter where the API key is passed.
     *
     * @see [headerApiKeySecurity]
     * @see [queryApiKeySecurity]
     */
    public fun cookieApiKeySecurity(
        name: String,
        key: String,
        builder: ApiKeySecurityBuilder.() -> Unit = {}
    )

    /**
     * Adds an OAuth2 security scheme to the API metadata.
     *
     * #### Usage
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
     * @receiver [OAuth2SecurityBuilder] The builder used to configure the security scheme.
     *
     * @param name The unique name of the security scheme.
     *
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
        builder: OAuth2SecurityBuilder.() -> Unit
    )

    /**
     * Adds an OpenID Connect security scheme to the API metadata.
     *
     * #### Usage
     * ```
     * openIdConnectSecurity(name = "OpenID") {
     *     description = "OpenID Connect Authentication."
     *     url = Url("https://example.com/.well-known/openid-configuration")
     * }
     * ```
     *
     * @receiver [OpenIdConnectSecurityBuilder] The builder used to configure the security scheme.
     *
     * @param name The unique name of the security scheme.
     * @param url The [Url] for the OpenID Connect configuration.
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
        builder: OpenIdConnectSecurityBuilder.() -> Unit = {}
    )

    /**
     * Adds a Mutual TLS security scheme to the API metadata.
     *
     * #### Usage
     * ```
     * mutualTLSSecurity(name = "MutualTLS") {
     *     description = "Mutual TLS Authentication."
     * }
     * ```
     *
     * @receiver [MutualTLSSecurityBuilder] The builder used to configure the security scheme.
     *
     * @param name The unique name of the security scheme.
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
        builder: MutualTLSSecurityBuilder.() -> Unit = {}
    )
}
