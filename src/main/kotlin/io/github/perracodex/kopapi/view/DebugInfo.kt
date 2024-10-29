/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.view

import io.github.perracodex.kopapi.view.annotation.DebugViewAPI

/**
 * Data class for storing debug view sections.
 */
@DebugViewAPI
internal data class DebugInfo(
    val apiOperationSections: Map<String, Section>,
    val typeSchemaSections: Map<String, Section>,
    val allApiOperationsYamlSection: String,
    val allApiOperationsJsonSection: String,
    val allTypeSchemasYamlSection: String,
    val allTypeSchemasJsonSection: String
) {
    /**
     * Data class for storing individual sections.
     */
    internal data class Section(
        val rawSection: String,
        val yamlSection: String,
        val jsonSection: String
    )
}
