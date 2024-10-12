/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.elements

import io.github.perracodex.kopapi.dsl.builders.ApiMetadataBuilder
import io.github.perracodex.kopapi.dsl.elements.ApiSecurity.Scheme
import io.github.perracodex.kopapi.dsl.types.AuthenticationMethod
import io.github.perracodex.kopapi.dsl.types.SecurityLocation
import io.ktor.http.*

/**
 * Represents the security configuration for an API endpoint.
 *
 * @property name The name of the security scheme.
 * @property description A detailed description of the security scheme.
 * @property scheme The type of security [Scheme] (e.g., HTTP, API Key, OAuth2, etc.).
 * @property authenticationMethod The [AuthenticationMethod] of security scheme (only applicable when the scheme is HTTP).
 * @property location The location where the API key is passed (only applicable for API Key schemes).
 * @property openIdConnectUrl The [Url] for the OpenID Connect configuration (only applicable for OpenID Connect schemes).
 *
 * @see [ApiMetadataBuilder.httpSecurity]
 * @see [ApiMetadataBuilder.apiKeySecurity]
 * @see [ApiMetadataBuilder.oauth2Security]
 * @see [ApiMetadataBuilder.openIdConnectSecurity]
 * @see [ApiMetadataBuilder.mutualTLSSecurity]
 */
internal data class ApiSecurity(
    val name: String,
    val description: String?,
    val scheme: Scheme,
    val authenticationMethod: AuthenticationMethod? = null, // Only applicable for HTTP scheme.
    val location: SecurityLocation? = null, // Only required for API_KEY scheme.
    val openIdConnectUrl: Url? = null  // Only applicable for OPENID_CONNECT scheme.
) {
    init {
        // Ensure that name is not empty
        require(name.isNotBlank()) { "Name must not be empty." }

        // Ensure that location is only provided for the API_KEY scheme.
        require(!(scheme == Scheme.API_KEY && location == null)) {
            "SecurityLocation must be specified when using the ${Scheme.API_KEY.name} scheme. " +
                    "Found Scheme '$scheme' with Location '$location'."
        }
        require(!(scheme != Scheme.API_KEY && location != null)) {
            "Location should only be used with the ${Scheme.API_KEY.name} scheme. " +
                    "Found Location '$location' with Scheme '$scheme'."
        }

        // Ensure that HTTP authentication method is only provided when scheme is HTTP.
        require(!(scheme == Scheme.HTTP && authenticationMethod == null)) {
            "HTTP type (e.g., basic, bearer) must be specified when using the ${Scheme.HTTP.name} scheme." +
                    " Found Scheme '$scheme' with HTTP authentication method '$authenticationMethod'."
        }
        require(!(scheme != Scheme.HTTP && authenticationMethod != null)) {
            "HTTP type should only be used with the ${Scheme.HTTP.name} scheme. " +
                    "Found HTTP authentication method  '$authenticationMethod' with Scheme '$scheme'."
        }

        // Ensure that openIdConnectUrl is only provided for OPENID_CONNECT scheme.
        require(!(scheme == Scheme.OPENID_CONNECT && openIdConnectUrl == null)) {
            "openIdConnectUrl must be specified when using the ${Scheme.OPENID_CONNECT.name} scheme. " +
                    "Found Scheme '$scheme' with openIdConnectUrl '$openIdConnectUrl'."
        }
        require(!(scheme != Scheme.OPENID_CONNECT && openIdConnectUrl != null)) {
            "openIdConnectUrl should only be used with the ${Scheme.OPENID_CONNECT.name} scheme. " +
                    "Found Scheme '$scheme' with openIdConnectUrl '$openIdConnectUrl'."
        }

        // Mutual TLS requires no additional parameters beyond the scheme itself.
        require(!(scheme == Scheme.MUTUAL_TLS && (authenticationMethod != null || location != null || openIdConnectUrl != null))) {
            "Mutual TLS (mTLS) does not require authentication methods, locations, or OpenID URLs."
        }
    }

    /**
     * Enum representing the various types of security schemes that can be used in an API.
     *
     * @property value The string value of the security scheme.
     */
    enum class Scheme(val value: String) {
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
}
