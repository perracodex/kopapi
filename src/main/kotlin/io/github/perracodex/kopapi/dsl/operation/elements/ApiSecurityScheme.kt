/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.elements

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.perracodex.kopapi.dsl.operation.types.AuthenticationMethod
import io.github.perracodex.kopapi.dsl.operation.types.SecurityLocation
import io.github.perracodex.kopapi.system.KopapiException

/**
 * Represents the security configuration for an API endpoint.
 *
 * @property schemeName The name of the security scheme. This is the key used to reference the scheme in the OpenAPI spec.
 * @property description A detailed description of the security scheme.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ApiSecurityScheme.ApiKey::class, name = "apiKey"),
    JsonSubTypes.Type(value = ApiSecurityScheme.Http::class, name = "http"),
    JsonSubTypes.Type(value = ApiSecurityScheme.OAuth2::class, name = "oauth2"),
    JsonSubTypes.Type(value = ApiSecurityScheme.OpenIdConnect::class, name = "openIdConnect"),
    JsonSubTypes.Type(value = ApiSecurityScheme.MutualTLS::class, name = "mutualTLS")
)
@JsonIgnoreProperties("schemeName")
internal sealed class ApiSecurityScheme(
    open val schemeName: String,
    open val description: String?
) {
    /**
     * Represents an API Key security scheme.
     *
     * @property apiKeyName The name of the header, query, or cookie parameter where the API key is passed.
     * @property location The location where the API key is passed (header, query, or cookie).
     */
    data class ApiKey(
        override val schemeName: String,
        override val description: String?,
        @JsonProperty("name")
        val apiKeyName: String,
        @JsonProperty("in")
        val location: SecurityLocation
    ) : ApiSecurityScheme(schemeName = schemeName, description = description) {
        init {
            if (schemeName.isBlank()) {
                throw KopapiException("Security scheme name must not be empty.")
            }
            if (apiKeyName.isBlank()) {
                throw KopapiException("API key name must not be empty.")
            }
        }
    }

    /**
     * Represents an HTTP security scheme (e.g., basic, bearer).
     *
     * @property method The name of the HTTP authentication scheme to be used (e.g., "basic", "bearer").
     */
    data class Http(
        override val schemeName: String,
        override val description: String?,
        @JsonProperty("scheme")
        val method: AuthenticationMethod
    ) : ApiSecurityScheme(schemeName = schemeName, description = description) {
        init {
            if (schemeName.isBlank()) {
                throw KopapiException("Security scheme name must not be empty.")
            }
        }
    }

    /**
     * Represents an OAuth2 security scheme.
     *
     * @property flows The OAuth2 flows configuration.
     */
    data class OAuth2(
        override val schemeName: String,
        override val description: String?,
        val flows: OAuthFlows
    ) : ApiSecurityScheme(schemeName = schemeName, description = description) {
        init {
            if (schemeName.isBlank()) {
                throw KopapiException("Security scheme name must not be empty.")
            }
            if (!flows.hasAtLeastOneFlow()) {
                throw KopapiException("At least one OAuth2 flow must be specified for OAuth2 schemes.")
            }
        }

        /**
         * Represents the OAuth2 flows configuration.
         * Each flow type has optional properties dependent on the flow.
         *
         * @property implicit Configuration for the OAuth2 Implicit flow.
         * @property password Configuration for the OAuth2 Password flow.
         * @property clientCredentials Configuration for the OAuth2 Client Credentials flow.
         * @property authorizationCode Configuration for the OAuth2 Authorization Code flow.
         */
        data class OAuthFlows(
            val implicit: OAuthFlow? = null,
            val password: OAuthFlow? = null,
            val clientCredentials: OAuthFlow? = null,
            val authorizationCode: OAuthFlow? = null
        ) {
            init {
                if (!hasAtLeastOneFlow()) {
                    throw KopapiException("At least one OAuth2 flow must be specified.")
                }
            }

            fun hasAtLeastOneFlow(): Boolean =
                implicit != null || password != null || clientCredentials != null || authorizationCode != null
        }

        /**
         * Represents an individual OAuth2 flow configuration,
         * detailing the endpoints and the available scopes.
         *
         * @property authorizationUrl The URL to which the application should redirect users for authorization.
         * @property tokenUrl The URL from which the application can obtain the token.
         * @property refreshUrl Optional URL from which the application can obtain a refreshed token.
         * @property scopes Describes the scopes available within this flow.
         */
        data class OAuthFlow(
            val authorizationUrl: String? = null,
            val tokenUrl: String? = null,
            val refreshUrl: String? = null,
            val scopes: Map<String, String>
        ) {
            init {
                if (scopes.isEmpty()) {
                    throw KopapiException("Scopes must not be empty.")
                }
            }
        }
    }

    /**
     * Represents an OpenID Connect security scheme.
     *
     * @property openIdConnectUrl The URL for the OpenID Connect configuration.
     */
    data class OpenIdConnect(
        override val schemeName: String,
        override val description: String?,
        val openIdConnectUrl: String
    ) : ApiSecurityScheme(schemeName = schemeName, description = description) {
        init {
            if (schemeName.isBlank()) {
                throw KopapiException("Security scheme name must not be empty.")
            }
            if (openIdConnectUrl.isBlank()) {
                throw KopapiException("OpenID Connect URL must be specified.")
            }
        }
    }

    /**
     * Represents a Mutual TLS (mTLS) security scheme.
     */
    data class MutualTLS(
        override val schemeName: String,
        override val description: String?
    ) : ApiSecurityScheme(schemeName = schemeName, description = description) {
        init {
            if (schemeName.isBlank()) {
                throw KopapiException("Security scheme name must not be empty.")
            }
        }
    }
}
