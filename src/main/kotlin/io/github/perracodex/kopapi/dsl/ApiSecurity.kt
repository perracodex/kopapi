/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl

/**
 * Represents the security configuration for an API endpoint.
 *
 * @property name The name of the security scheme.
 * @property description A detailed description of the security scheme.
 * @property scheme The type of security scheme (e.g., HTTP, API Key, OAuth2, etc.).
 * @property httpType The HTTP type of security scheme (only applicable when the scheme is HTTP).
 * @property location The location where the API key is passed (only applicable for API Key schemes).
 * @property openIdConnectUrl The URL for the OpenID Connect configuration (only applicable for OpenID Connect schemes).
 */
@ConsistentCopyVisibility
public data class ApiSecurity @PublishedApi internal constructor(
    val name: String,
    val description: String? = null,
    val scheme: Scheme,
    val httpType: HttpType? = null,       // Only applicable for HTTP scheme.
    val location: Location? = null,       // Only required for API_KEY scheme.
    val openIdConnectUrl: String? = null  // Only applicable for OPENID_CONNECT scheme.
) {
    init {
        // Ensure that name is not empty
        require(name.isNotEmpty()) {
            "Name must not be empty."
        }

        // Ensure that location is only provided for the API_KEY scheme.
        require(!(scheme == Scheme.API_KEY && location == null)) {
            "Location must be specified when using the API_KEY scheme. " +
                    "Found Scheme '$scheme' with Location '$location'."
        }
        require(!(scheme != Scheme.API_KEY && location != null)) {
            "Location should only be used with the API_KEY scheme. " +
                    "Found Location '$location' with Scheme '$scheme'."
        }

        // Ensure that HTTP type is only provided when scheme is HTTP.
        require(!(scheme == Scheme.HTTP && httpType == null)) {
            "HTTP type (e.g., basic, bearer) must be specified when using the HTTP scheme." +
                    " Found Scheme '$scheme' with HTTP Type '$httpType'."
        }
        require(!(scheme != Scheme.HTTP && httpType != null)) {
            "HTTP type should only be used with the HTTP scheme. " +
                    "Found HTTP Type '$httpType' with Scheme '$scheme'."
        }

        // Ensure that openIdConnectUrl is only provided for OPENID_CONNECT scheme.
        require(!(scheme == Scheme.OPENID_CONNECT && openIdConnectUrl == null)) {
            "openIdConnectUrl must be specified when using the OPENID_CONNECT scheme. " +
                    "Found Scheme '$scheme' with openIdConnectUrl '$openIdConnectUrl'."
        }
        require(!(scheme != Scheme.OPENID_CONNECT && openIdConnectUrl != null)) {
            "openIdConnectUrl should only be used with the OPENID_CONNECT scheme. " +
                    "Found Scheme '$scheme' with openIdConnectUrl '$openIdConnectUrl'."
        }
    }

    /**
     * Enum representing the various types of security schemes that can be used in an API.
     *
     * @property value The string value of the security scheme.
     */
    public enum class Scheme(public val value: String) {
        /** HTTP security scheme (e.g., basic, bearer). */
        HTTP(value = "http"),

        /** API key security scheme, used in headers, query parameters, or cookies. */
        API_KEY(value = "apiKey"),

        /** OAuth2 security scheme for OAuth2 authentication. */
        OAUTH2(value = "oauth2"),

        /** OpenID Connect security scheme, built on OAuth2. */
        OPENID_CONNECT(value = "openIdConnect"),

        /** Mutual TLS security scheme (mTLS). */
        MUTUAL_TLS(value = "mutualTLS")
    }

    /**
     * Enum representing the possible locations where the API key can be passed for the API Key security scheme.
     * This is applicable only for the API_KEY scheme.
     *
     * @property value The string value of the location.
     */
    public enum class Location(public val value: String) {
        /** The API key is passed in a cookie. */
        COOKIE(value = "cookie"),

        /** The API key is passed in an HTTP header. */
        HEADER(value = "header"),

        /** The API key is passed as a query parameter. */
        QUERY(value = "query")
    }

    /**
     * Enum representing the possible HTTP authentication types, applicable only to HTTP schemes.
     *
     * @property value The string value of the HTTP type.
     */
    public enum class HttpType(public val value: String) {
        /** HTTP Basic authentication scheme. */
        BASIC(value = "basic"),

        /** HTTP Bearer authentication scheme (commonly used with JWT). */
        BEARER(value = "bearer"),

        /** HTTP Digest authentication scheme. */
        DIGEST(value = "digest")
    }
}
