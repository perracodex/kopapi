/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.info

import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiInfo
import io.github.perracodex.kopapi.inspector.schema.SchemaConflicts

/**
 * Composes the `Info` section of the OpenAPI schema.
 */
@ComposerAPI
internal object InfoSectionComposer {
    private const val DEFAULT_TITLE: String = "API"
    private const val DEFAULT_DESCRIPTION: String = "API Description"
    private const val DEFAULT_VERSION: String = "1.0.0"

    /**
     * Composes the `Info` section of the OpenAPI schema for serialization,
     * appending any errors to the description if schema conflicts are detected.
     *
     * @param apiInfo The initial [ApiInfo] object from the configuration.
     * @param schemaConflicts A set of schema conflicts detected during inspection, if any.
     * @return The updated [ApiInfo] object ready for serialization.
     */
    fun compose(apiInfo: ApiInfo?, schemaConflicts: Set<SchemaConflicts.Conflict>): ApiInfo {
        val baseApiInfo: ApiInfo = apiInfo ?: ApiInfo(
            title = DEFAULT_TITLE,
            description = DEFAULT_DESCRIPTION,
            version = DEFAULT_VERSION,
            termsOfService = null,
            contact = null,
            license = null
        )

        // If no schema conflicts are detected, return the result immediately.
        if (schemaConflicts.isEmpty()) {
            return baseApiInfo
        }

        // Build the error messages for each conflict.
        val errors: Set<String> = schemaConflicts.map { conflict ->
            "'${conflict.name}': ${conflict.conflictingTypes.joinToString(separator = ", ") { "[$it]" }}"
        }.toSortedSet()

        // Build the updated description with errors and multiline formatting.
        val updatedDescription: String = buildString {
            append(baseApiInfo.description)
            if (errors.isNotEmpty()) {
                append("\n\nErrors:\n")
                errors.forEach { error ->
                    append("- $error\n")
                }
            }
        }.trim()

        return baseApiInfo.copy(description = updatedDescription)
    }
}
