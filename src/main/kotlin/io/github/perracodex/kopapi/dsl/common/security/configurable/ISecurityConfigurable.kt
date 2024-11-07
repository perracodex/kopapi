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
    )

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
    )

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
    )

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
    )

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
    )

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
    )

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
    )

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
    )

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
    )
}
