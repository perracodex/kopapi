/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.info

import io.github.perracodex.kopapi.composer.annotation.ComposerApi
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiConfiguration
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiInfo
import io.github.perracodex.kopapi.inspector.schema.SchemaConflicts
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.github.perracodex.kopapi.system.Tracer

/**
 * Composes the `Info` section of the OpenAPI schema.
 */
@ComposerApi
internal object InfoSectionComposer {
    private val tracer = Tracer<InfoSectionComposer>()

    private const val DEFAULT_TITLE: String = "API"
    private const val DEFAULT_DESCRIPTION: String = "API Description"
    private const val DEFAULT_VERSION: String = "1.0.0"

    /**
     * Composes the `Info` section of the OpenAPI schema for serialization,
     * appending any errors to the description if errors were detected.
     *
     * @param apiConfiguration The API configuration object holding the `info` section data.
     * @param registrationErrors A set of errors detected during API registration, if any.
     * @param schemaConflicts A set of schema conflicts detected during inspection, if any.
     * @return The updated [ApiInfo] object ready for serialization.
     */
    fun compose(
        apiConfiguration: ApiConfiguration,
        registrationErrors: Set<String>,
        schemaConflicts: Set<SchemaConflicts.Conflict>
    ): ApiInfo {
        tracer.info("Composing the 'Info' section of the OpenAPI schema.")

        val baseApiInfo: ApiInfo = apiConfiguration.apiInfo ?: ApiInfo(
            title = DEFAULT_TITLE,
            description = DEFAULT_DESCRIPTION,
            version = DEFAULT_VERSION,
            termsOfService = null,
            contact = null,
            license = null
        )

        // If no errors were detected, return the result immediately.
        if (!apiConfiguration.apiDocs.swagger.includeErrors || (registrationErrors.isEmpty() && schemaConflicts.isEmpty())) {
            return baseApiInfo
        }

        // Build the updated description with errors and multiline formatting.
        val updatedDescription: String = buildString {
            append(baseApiInfo.description)

            // Append registration errors.
            if (registrationErrors.isNotEmpty()) {
                append("\n\n### Detected Errors:\n")
                registrationErrors.forEach { error ->
                    append("- $error\n")
                }
            }

            // Append schema conflicts.
            val conflicts: Set<String> = SchemaRegistry.getFormattedTypeConflicts(schemaConflicts = schemaConflicts)
            if (conflicts.isNotEmpty()) {
                append("\n\n### Detected Schema Conflicts:\n")
                conflicts.forEach { conflict ->
                    append("- $conflict\n")
                }
            }
        }.trim()

        return baseApiInfo.copy(description = updatedDescription)
    }
}
