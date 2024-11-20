/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.composer.security

import io.github.perracodex.kopapi.composer.annotation.ComposerApi

/**
 * Represents the top-level security requirements in the OpenAPI schema.
 *
 * Top-level security requirements apply to all API operations by default unless overridden
 * by operation-level security configurations.
 *
 * @property requirements A list of [SecurityRequirement] objects representing each top-level security requirement.
 */
@ComposerApi
internal data class TopLevelSecurityRequirement(
    val requirements: List<SecurityRequirement>
) {
    fun toOpenApiSpec(): List<Map<String, List<String>>?> {
        return requirements.map { it.toOpenApiSpec() }
    }
}
