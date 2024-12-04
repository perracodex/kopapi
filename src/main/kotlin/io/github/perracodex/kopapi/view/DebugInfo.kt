/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.view

import io.github.perracodex.kopapi.view.annotation.DebugViewApi

/**
 * Data class for storing debug view sections.
 */
@DebugViewApi
internal data class DebugInfo(
    val apiOperationSections: Map<String, Section>,
    val typeSchemaSections: Map<String, Section>,
    val allApiOperationsYamlSection: String,
    val allApiOperationsJsonSection: String,
    val allTypeSchemasYamlSection: String,
    val allTypeSchemasJsonSection: String,
    val openApiYaml: String,
    val openApiJson: String
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
