/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer

import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.github.perracodex.kopapi.composer.info.InfoSectionComposer
import io.github.perracodex.kopapi.composer.path.PathComposer
import io.github.perracodex.kopapi.composer.security.GlobalSecurityRequirement
import io.github.perracodex.kopapi.composer.security.OperationSecurity
import io.github.perracodex.kopapi.composer.security.SecuritySectionComposer
import io.github.perracodex.kopapi.composer.tags.TagsComposer
import io.github.perracodex.kopapi.dsl.operation.elements.ApiOperation
import io.github.perracodex.kopapi.dsl.operation.elements.ApiSecurityScheme
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiConfiguration
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiInfo
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiServerConfig
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiTag
import io.github.perracodex.kopapi.inspector.schema.SchemaConflicts
import io.github.perracodex.kopapi.serialization.SerializationUtils

/**
 * Responsible for generating the OpenAPI schema.
 */
@ComposerAPI
internal class SchemaComposer(
    private val apiConfiguration: ApiConfiguration,
    private val apiOperations: Set<ApiOperation>,
    private val schemaConflicts: Set<SchemaConflicts.Conflict>,
) {
    /**
     * Serves the OpenAPI schema in the specified format.
     *
     * @param format The [SchemaRegistry.Format] of the OpenAPI schema to serve.
     * @return The OpenAPI schema in the specified format.
     */
    fun compose(format: SchemaRegistry.Format): String {
        // Compose the `Info` section.
        val infoSection: ApiInfo = InfoSectionComposer.compose(
            apiInfo = apiConfiguration.apiInfo,
            schemaConflicts = schemaConflicts
        )

        // Get the `Servers` section.
        val serversSection: List<ApiServerConfig>? = apiConfiguration.apiServers?.toList()

        // Compose the `Tags` section.
        val tags: List<ApiTag>? = TagsComposer(
            apiConfiguration = apiConfiguration,
            apiOperations = apiOperations
        ).compose()

        // Compose the `Security` sections.
        val securityComposer = SecuritySectionComposer(apiConfiguration = apiConfiguration, apiOperations = apiOperations)
        val globalSecurity: GlobalSecurityRequirement? = securityComposer.composeGlobalSecurityRequirements()
        val securitySchemes: Map<String, ApiSecurityScheme>? = securityComposer.composeSecuritySchemes()
        val operationSecurity: List<OperationSecurity>? = securityComposer.composeOperationSecurity()

        // Compose the `Paths` section.
        val pathsSection: Map<String, OpenAPiSchema.PathItem> = PathComposer.compose(
            apiOperations = apiOperations,
            operationSecurity = operationSecurity
        )

        // Create the `components` object.
        val components: OpenAPiSchema.Components? = OpenAPiSchema.Components(
            securitySchemes = securitySchemes
        ).takeIf { it.hasContent() }

        // Create the OpenAPI schema.
        val openApiSchema = OpenAPiSchema(
            openapi = OPEN_API_VERSION,
            info = infoSection,
            servers = serversSection,
            tags = tags,
            paths = pathsSection,
            components = components,
            security = globalSecurity?.toOpenAPISpec(),
        )

        // Serialize the OpenAPI schema, producing the final schema.
        val schema: String = when (format) {
            SchemaRegistry.Format.JSON -> SerializationUtils.toRawJson(instance = openApiSchema)
            SchemaRegistry.Format.YAML -> SerializationUtils.toYaml(instance = openApiSchema)
        }

        return schema
    }

    private companion object {
        /** The version of the OpenAPI specification used by the plugin. */
        private const val OPEN_API_VERSION = "3.1.0"
    }
}
