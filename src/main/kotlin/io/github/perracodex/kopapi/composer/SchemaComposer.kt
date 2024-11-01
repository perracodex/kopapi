/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer

import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.github.perracodex.kopapi.composer.component.ComponentComposer
import io.github.perracodex.kopapi.composer.info.InfoSectionComposer
import io.github.perracodex.kopapi.composer.operation.OperationComposer
import io.github.perracodex.kopapi.composer.security.GlobalSecurityRequirement
import io.github.perracodex.kopapi.composer.security.SecurityComposer
import io.github.perracodex.kopapi.composer.security.SecurityObject
import io.github.perracodex.kopapi.composer.tags.TagsComposer
import io.github.perracodex.kopapi.dsl.operation.elements.ApiOperation
import io.github.perracodex.kopapi.dsl.operation.elements.ApiSecurityScheme
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiConfiguration
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiInfo
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiServerConfig
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiTag
import io.github.perracodex.kopapi.inspector.schema.SchemaConflicts
import io.github.perracodex.kopapi.schema.CompositionSchema
import io.github.perracodex.kopapi.schema.ElementSchema
import io.github.perracodex.kopapi.schema.ISchema
import io.github.perracodex.kopapi.schema.OpenApiSchema
import io.github.perracodex.kopapi.serialization.SerializationUtils
import io.github.perracodex.kopapi.system.Tracer
import io.github.perracodex.kopapi.types.Composition

/**
 * Responsible for generating the OpenAPI schema.
 */
@ComposerAPI
internal class SchemaComposer(
    private val apiConfiguration: ApiConfiguration,
    private val apiOperations: Set<ApiOperation>,
    private val schemaConflicts: Set<SchemaConflicts.Conflict>,
) {
    private val tracer = Tracer<SchemaComposer>()

    /**
     * Serves the OpenAPI schema in the specified format.
     *
     * @param format The [SchemaRegistry.Format] of the OpenAPI schema to serve.
     * @return The OpenAPI schema in the specified format.
     */
    fun compose(format: SchemaRegistry.Format): String {
        tracer.info("Initiating schema composition. Format: $format")

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
        val securityComposer = SecurityComposer(apiConfiguration = apiConfiguration, apiOperations = apiOperations)
        val globalSecurity: GlobalSecurityRequirement? = securityComposer.composeGlobalSecurityRequirements()
        val securitySchemes: Map<String, ApiSecurityScheme>? = securityComposer.composeSecuritySchemes()
        val securityObject: List<SecurityObject>? = securityComposer.composeOperationSecurity()

        // Compose the `Path Items` section.
        val pathItems: Map<String, OpenApiSchema.PathItemObject> = OperationComposer.compose(
            apiOperations = apiOperations,
            securityObject = securityObject
        )

        // Component schemas.
        val componentSchemas: Map<String, ElementSchema>? = ComponentComposer.compose(
            typeSchemas = SchemaRegistry.getSchemaTypes()
        )

        // Create the `components` object.
        val components: OpenApiSchema.Components? = OpenApiSchema.Components(
            componentSchemas = componentSchemas,
            securitySchemes = securitySchemes
        ).takeIf { it.hasContent() }

        // Create the OpenAPI schema.
        tracer.info("Composing the OpenAPI schema object.")
        val openApiSchema = OpenApiSchema(
            openapi = OPEN_API_VERSION,
            info = infoSection,
            servers = serversSection,
            tags = tags,
            paths = pathItems,
            components = components,
            security = globalSecurity?.toOpenAPISpec(),
        )

        // Serialize the OpenAPI schema, producing the final schema.
        val schema: String = when (format) {
            SchemaRegistry.Format.JSON -> SerializationUtils().toJson(instance = openApiSchema)
            SchemaRegistry.Format.YAML -> SerializationUtils().toYaml(instance = openApiSchema)
        }

        return schema
    }

    companion object {
        /** The version of the OpenAPI specification used by the plugin. */
        private const val OPEN_API_VERSION = "3.1.0"

        /**
         * Determines the appropriate [OpenApiSchema.ContentSchema] based on the given composition
         * and a list of `Schema` objects.
         *
         * - If only one schema is present, it returns that schema directly.
         * - If multiple schemas are present, it combines them according to
         *   the specified `composition` type, defaulting to `Composition.ANY_OF`.
         *
         * @param composition The [Composition] type to apply when combining multiple schemas.
         *                    Defaults to `Composition.ANY_OF` if null.
         * @param schemas The list of [ElementSchema] objects to be combined. Assumes the list is non-empty and preprocessed.
         * @return An [OpenApiSchema.ContentSchema] representing the combined schema.
         */
        @OptIn(ComposerAPI::class)
        fun determineSchema(composition: Composition?, schemas: List<ElementSchema>): OpenApiSchema.ContentSchema {
            val combinedSchema: ISchema = when {
                schemas.size == 1 -> schemas.first()
                else -> when (composition ?: Composition.ANY_OF) {
                    Composition.ANY_OF -> CompositionSchema.AnyOf(anyOf = schemas)
                    Composition.ALL_OF -> CompositionSchema.AllOf(allOf = schemas)
                    Composition.ONE_OF -> CompositionSchema.OneOf(oneOf = schemas)
                }
            }

            return OpenApiSchema.ContentSchema(schema = combinedSchema)
        }
    }
}
