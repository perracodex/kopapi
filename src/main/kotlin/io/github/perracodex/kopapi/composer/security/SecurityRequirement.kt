/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.security

import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.github.perracodex.kopapi.dsl.operation.elements.ApiSecurityScheme

/**
 * Represents a single security requirement as defined by the OpenAPI Specification.
 *
 * A security requirement specifies a security scheme that applies to an API operation.
 * It consists of the name of the security scheme, its type, and an optional list of scopes (applicable
 * for schemes like OAuth2).
 *
 * @property securityScheme The type of the security scheme (e.g., OAuth2, API Key).
 * @property scopes The list of scopes required for the security scheme. This is typically
 *                used with OAuth2 security schemes to define access privileges.
 *                Defaults to `null` if no scopes are required.
 */
@ComposerAPI
internal data class SecurityRequirement(
    val securityScheme: ApiSecurityScheme,
    val scopes: List<String>? = null
) {
    /**
     * Transforms the [SecurityRequirement] instance into a map structure as required by the OpenAPI Specification.
     *
     * @return A map where the key is the security scheme name and the value is a list of scopes.
     *         If no scopes are required, the value is omitted for non-OAuth2 schemes.
     *         For OAuth2 schemes, scopes must not be null or empty.
     */
    fun toOpenAPISpec(): Map<String, List<String>> {
        return mapOf(securityScheme.schemeName to (scopes ?: emptyList()))
    }
}
