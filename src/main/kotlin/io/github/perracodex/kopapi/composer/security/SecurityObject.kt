/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.composer.security

import io.github.perracodex.kopapi.composer.annotation.ComposerApi

/**
 * Represents the security configuration for a specific API operation.
 *
 * Each [SecurityObject] instance associates an API operation, identified by its HTTP method
 * and path, with a list of security requirements that must be satisfied to access the operation.
 * An empty security list indicates that the operation is publicly accessible without any security measures.
 *
 * @property method The HTTP method of the operation (e.g., GET, POST, PUT, DELETE).
 *                  This should correspond to one of the standard HTTP methods.
 * @property path The URL path of the API Operation, following the OpenAPI path templating conventions.
 *                For example, "/users/{userId}".
 * @property security Optional list of security requirements for the operation. Each entry specifies
 *                    a security scheme and its associated scopes. An empty list denotes that
 *                    the API Operation does not require any security, overriding any top-level security settings.
 */
@ComposerApi
internal data class SecurityObject(
    val method: String,
    val path: String,
    val security: List<SecurityRequirement>?
)
