/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer

import io.github.perracodex.kopapi.composer.annotation.ComposerApi
import io.github.perracodex.kopapi.composer.component.ComponentComposer
import io.github.perracodex.kopapi.composer.info.InfoSectionComposer
import io.github.perracodex.kopapi.composer.operation.OperationComposer
import io.github.perracodex.kopapi.composer.security.SecurityComposer
import io.github.perracodex.kopapi.composer.security.SecurityObject
import io.github.perracodex.kopapi.composer.security.TopLevelSecurityRequirement
import io.github.perracodex.kopapi.composer.tags.TagsComposer
import io.github.perracodex.kopapi.dsl.operation.elements.ApiOperation
import io.github.perracodex.kopapi.dsl.operation.elements.ApiSecurityScheme
import io.github.perracodex.kopapi.dsl.path.ApiPath
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiConfiguration
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiInfo
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiServerConfig
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiTag
import io.github.perracodex.kopapi.introspector.schema.SchemaConflicts
import io.github.perracodex.kopapi.schema.OpenApiSchema
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.github.perracodex.kopapi.schema.facets.CompositionSchema
import io.github.perracodex.kopapi.schema.facets.ElementSchema
import io.github.perracodex.kopapi.schema.facets.ISchemaFacet
import io.github.perracodex.kopapi.serialization.SerializationUtils
import io.github.perracodex.kopapi.system.Tracer
import io.github.perracodex.kopapi.types.Composition
import io.github.perracodex.kopapi.utils.trimOrNull
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.core.models.ParseOptions
import io.swagger.v3.parser.core.models.SwaggerParseResult

/**
 * Responsible for generating the OpenAPI schema.
 */
@ComposerApi
internal class SchemaComposer(
    private val apiConfiguration: ApiConfiguration,
    private val apiPaths: Set<ApiPath>,
    private val apiOperations: Set<ApiOperation>,
    private val registrationErrors: Set<String>,
    private val schemaConflicts: Set<SchemaConflicts.Conflict>,
) {
    private val tracer = Tracer<SchemaComposer>()

    /**
     * Creates the OpenAPI schema based on the provided configuration and operations.
     *
     * @return The OpenAPI schema in both YAML and JSON formats.
     */
    fun compose(): OpenApiSpec {
        tracer.info("Initiating schema composition")

        // Compose the `Info` section.
        val infoSection: ApiInfo = InfoSectionComposer.compose(
            apiConfiguration = apiConfiguration,
            registrationErrors = registrationErrors,
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
        val topLevelSecurity: TopLevelSecurityRequirement? = securityComposer.composeTopLevelSecurityRequirements()
        val securitySchemes: Map<String, ApiSecurityScheme>? = securityComposer.composeSecuritySchemes()
        val securityObject: List<SecurityObject>? = securityComposer.composeOperationSecurity()

        // Compose the `Path Items` section.
        val pathItems: Map<String, OpenApiSchema.PathItemObject> = OperationComposer.compose(
            apiPaths = apiPaths,
            apiOperations = apiOperations,
            securityObject = securityObject
        )

        // Component schemas.
        val componentSchemas: Map<String, ISchemaFacet>? = ComponentComposer.compose(
            typeSchemas = SchemaRegistry.getSchemaTypes()
        )

        // Create the `components` object.
        val components: OpenApiSchema.Components? = OpenApiSchema.Components(
            componentSchemas = componentSchemas,
            securitySchemes = securitySchemes
        ).takeIf { it.hasContent() }

        // Create the OpenAPI schema.
        tracer.info("Composing the final OpenAPI schema.")
        val openApiSchema = OpenApiSchema(
            openapi = OPEN_API_VERSION,
            info = infoSection,
            servers = serversSection,
            tags = tags,
            paths = pathItems,
            components = components,
            security = topLevelSecurity?.toOpenApiSpec(),
        )

        // Serialize the OpenAPI schema, producing the final specification.
        val openApiYaml: String = SerializationUtils().toYaml(instance = openApiSchema)

        // If the official swagger parser detects, rebuild the schema with the errors appended to the `info` section.
        // Note that the AppInfo composer already adds some errors, but these are very basic.
        val parserErrors: Set<String> = verify(openApiYaml = openApiYaml)
        if (apiConfiguration.apiDocs.swagger.includeErrors && parserErrors.isNotEmpty()) {
            return rebuildWithErrors(openApiSchema = openApiSchema, parserErrors = parserErrors)
        }

        // Generate the JSON after ensuring no errors were detected, as it would have
        // been a wasted step, since the rebuild would have to be done anyway.
        val openApiJson: String = SerializationUtils().toJson(instance = openApiSchema)

        return OpenApiSpec(yaml = openApiYaml, json = openApiJson, errors = parserErrors)
    }

    /**
     * If errors were detected during the final verification of the OpenAPI schema,
     * append the error details to the `info` section.
     *
     * @param openApiSchema The OpenAPI schema to update.
     * @param parserErrors The set of errors detected during the final verification of the OpenAPI schema.
     * @return The updated OpenAPI schema.
     */
    private fun rebuildWithErrors(openApiSchema: OpenApiSchema, parserErrors: Set<String>): OpenApiSpec {
        // Update the info section with error details.
        tracer.info("Validation errors detected. Updating the info section with error details.")

        // Create a new infoSection with the updated description.
        val updatedInfoSection: ApiInfo = updateInfoSectionWithParserErrors(
            infoSection = openApiSchema.info,
            parserErrors = parserErrors
        )

        // Create an updated OpenAPI schema with the new info section.
        val updatedOpenApiSchema: OpenApiSchema = openApiSchema.copy(info = updatedInfoSection)

        // Re-serialize the OpenAPI schema.
        val (updatedOpenApiJson: String, updatedOpenApiYaml: String) = serializeOpenApiSchema(updatedOpenApiSchema)

        return OpenApiSpec(yaml = updatedOpenApiYaml, json = updatedOpenApiJson, errors = parserErrors)
    }

    /**
     * Serializes the OpenAPI schema into both YAML and JSON formats.
     *
     * @param infoSection The `info` section of the OpenAPI schema.
     * @param parserErrors The set of errors detected during the final verification of the OpenAPI schema.
     * @return The updated `info` section.
     */
    private fun updateInfoSectionWithParserErrors(infoSection: ApiInfo, parserErrors: Set<String>): ApiInfo {
        val errorMessages: String = parserErrors.joinToString(separator = "\n") { "- $it" }

        val updatedDescription: String = buildString {
            append(infoSection.description.trimOrNull())
            if (isNotBlank()) {
                append("\n\n")
            }
            append("### Specification Errors:\n")
            append(errorMessages)
        }

        return infoSection.copy(description = updatedDescription)
    }

    /**
     * Serializes the OpenAPI schema into both YAML and JSON formats.
     *
     * @param openApiSchema The OpenAPI schema to serialize.
     * @return A pair containing the OpenAPI schema in JSON and YAML formats.
     */
    private fun serializeOpenApiSchema(openApiSchema: OpenApiSchema): Pair<String, String> {
        val openApiJson: String = SerializationUtils().toJson(instance = openApiSchema)
        val openApiYaml: String = SerializationUtils().toYaml(instance = openApiSchema)
        return Pair(openApiJson, openApiYaml)
    }

    /**
     * Verifies the OpenAPI schema to ensure it is valid.
     *
     * @param openApiYaml The OpenAPI schema in YAML format.
     * @return A set of errors that occurred during the verification process.
     */
    private fun verify(openApiYaml: String): Set<String> {
        return runCatching {
            val options: ParseOptions = ParseOptions().apply {
                isResolve = true               // Resolve $ref references.
                isResolveFully = true          // Fully resolve the model.
                isResolveCombinators = true    // Resolve combinator schemas.
                isResolveResponses = true      // Resolve $ref in responses.
                isResolveRequestBody = true    // Resolve $ref in request bodies.
                isValidateInternalRefs = true  // Validate internal $ref references.
                isValidateExternalRefs = true  // Validate external $ref references.
            }

            val openAPI: SwaggerParseResult = OpenAPIV3Parser().readContents(openApiYaml, null, options)
            return openAPI.messages.toSet()
        }.onFailure { error ->
            tracer.error(message = "Failed to verify the OpenAPI schema.", cause = error)
        }.getOrDefault(emptySet())
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
        fun determineSchema(composition: Composition?, schemas: List<ElementSchema>): OpenApiSchema.ContentSchema {
            val combinedSchema: ISchemaFacet = when {
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

    /**
     * Holds the OpenAPI specification in both YAML and JSON formats.
     *
     * @property yaml The OpenAPI specification in YAML format.
     * @property json The OpenAPI specification in JSON format.
     * @property errors A set of errors that occurred during the final verification of the OpenAPI schema.
     */
    data class OpenApiSpec(
        val yaml: String,
        val json: String,
        val errors: Set<String>
    )
}
