/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.security

import io.github.perracodex.kopapi.composer.annotation.ComposerAPI

/**
 * Represents the security configuration for a specific API operation.
 *
 * Each [OperationSecurity] instance associates an API operation, identified by its HTTP method
 * and path, with a list of security requirements that must be satisfied to access the operation.
 * An empty security list indicates that the operation is publicly accessible without any security measures.
 *
 * @property method The HTTP method of the operation (e.g., GET, POST, PUT, DELETE).
 *                  This should correspond to one of the standard HTTP methods.
 * @property path The URL path of the API Operation, following the OpenAPI path templating conventions.
 *                For example, "/users/{userId}".
 * @property security Optional list of security requirements for the operation. Each entry specifies
 *                    a security scheme and its associated scopes. An empty list denotes that
 *                    the API Operation does not require any security, overriding any global security settings.
 */
@ComposerAPI
internal data class OperationSecurity(
    val method: String,
    val path: String,
    val security: List<SecurityRequirement>?
)
