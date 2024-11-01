/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.security

import io.github.perracodex.kopapi.composer.annotation.ComposerApi

/**
 * Represents the global security requirements in the OpenAPI schema.
 *
 * Global security requirements apply to all API operations by default unless overridden
 * by operation-level security configurations.
 *
 * @property requirements A list of [SecurityRequirement] objects representing each global security requirement.
 */
@ComposerApi
internal data class GlobalSecurityRequirement(
    val requirements: List<SecurityRequirement>
) {
    fun toOpenApiSpec(): List<Map<String, List<String>>?> {
        return requirements.map { it.toOpenApiSpec() }
    }
}
