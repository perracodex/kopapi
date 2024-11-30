/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.composer

import io.github.perracodex.kopapi.composer.annotation.ComposerApi
import io.github.perracodex.kopapi.composer.component.ComponentComposer
import io.github.perracodex.kopapi.composer.info.InfoSectionComposer
import io.github.perracodex.kopapi.composer.operation.OperationComposer
import io.github.perracodex.kopapi.composer.security.SecurityComposer
import io.github.perracodex.kopapi.composer.security.SecurityObject
import io.github.perracodex.kopapi.composer.security.TopLevelSecurityRequirement
import io.github.perracodex.kopapi.composer.tag.TagComposer
import io.github.perracodex.kopapi.dsl.operation.element.ApiOperation
import io.github.perracodex.kopapi.dsl.operation.element.ApiSecurityScheme
import io.github.perracodex.kopapi.dsl.path.element.ApiPath
import io.github.perracodex.kopapi.dsl.plugin.element.ApiConfiguration
import io.github.perracodex.kopapi.dsl.plugin.element.ApiInfo
import io.github.perracodex.kopapi.dsl.plugin.element.ApiServerConfig
import io.github.perracodex.kopapi.dsl.plugin.element.ApiTag
import io.github.perracodex.kopapi.introspection.schema.SchemaConflicts
import io.github.perracodex.kopapi.schema.OpenApiSchema
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.github.perracodex.kopapi.schema.facet.ISchemaFacet
import io.github.perracodex.kopapi.serialization.SerializationUtils
import io.github.perracodex.kopapi.system.Tracer
import io.github.perracodex.kopapi.type.OpenApiFormat
import io.github.perracodex.kopapi.util.trimOrNull
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
    private val format: OpenApiFormat?
) {
    private val tracer: Tracer = Tracer<SchemaComposer>()

    /**
     * Creates the OpenAPI schema based on the provided configuration and operations.
     *
     * @return The OpenAPI schema in both YAML and JSON formats.
     */
    fun compose(): OpenApiSpec = synchronized(this) {
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
        val tags: List<ApiTag>? = TagComposer(
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
        val openApiSpec: OpenApiSpec = serializeOpenApiSchema(
            openApiSchema = openApiSchema,
            format = format,
            errors = null
        )

        // If the official swagger parser detects, rebuild the schema with the errors appended to the `info` section.
        // Note that the AppInfo composer already adds some errors, but these are very basic.
        val parserErrors: Set<String> = verify(openApiSpec = openApiSpec)
        if (apiConfiguration.apiDocs.swagger.includeErrors && parserErrors.isNotEmpty()) {
            return rebuildWithErrors(openApiSchema = openApiSchema, parserErrors = parserErrors, format = format)
        }

        return openApiSpec.copy(errors = parserErrors)
    }

    /**
     * If errors were detected during the final verification of the OpenAPI schema,
     * append the error details to the `info` section.
     *
     * @param openApiSchema The OpenAPI schema to update.
     * @param parserErrors The set of errors detected during the final verification of the OpenAPI schema.
     * @param format The format to serialize the OpenAPI schema in. `null` to serialize to all formats.
     * @return The updated OpenAPI schema.
     */
    private fun rebuildWithErrors(
        openApiSchema: OpenApiSchema,
        parserErrors: Set<String>,
        format: OpenApiFormat?
    ): OpenApiSpec {
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
        return serializeOpenApiSchema(
            openApiSchema = updatedOpenApiSchema,
            format = format,
            errors = parserErrors
        )
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
     * @param format The format to serialize the OpenAPI schema in.
     * @param errors The set of errors detected during the final verification of the OpenAPI schema.
     * @return The serialized OpenAPI schema.
     */
    private fun serializeOpenApiSchema(
        openApiSchema: OpenApiSchema,
        format: OpenApiFormat?,
        errors: Set<String>?
    ): OpenApiSpec {
        val (yaml: String?, json: String?) = when (format) {
            OpenApiFormat.YAML -> SerializationUtils().toYaml(instance = openApiSchema) to null
            OpenApiFormat.JSON -> null to SerializationUtils().toJson(instance = openApiSchema)
            else -> SerializationUtils().toYaml(instance = openApiSchema) to SerializationUtils().toJson(instance = openApiSchema)
        }
        return OpenApiSpec(yaml = yaml, json = json, errors = errors)
    }

    /**
     * Verifies the OpenAPI schema to ensure it is valid.
     *
     * @param openApiSpec The OpenAPI specification to verify.
     * @return A set of errors that occurred during the verification process.
     */
    private fun verify(openApiSpec: OpenApiSpec): Set<String> {
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

            val openApi: String = when {
                !openApiSpec.yaml.isNullOrBlank() -> openApiSpec.yaml
                !openApiSpec.json.isNullOrBlank() -> openApiSpec.json
                else -> throw IllegalStateException("No OpenAPI specification found.")
            }

            val openApiResult: SwaggerParseResult = OpenAPIV3Parser().readContents(openApi, null, options)
            return openApiResult.messages.toSet()
        }.onFailure { error ->
            tracer.error(message = "Failed to verify the OpenAPI schema.", cause = error)
        }.getOrDefault(emptySet())
    }

    companion object {
        /** The version of the OpenAPI specification used by the plugin. */
        private const val OPEN_API_VERSION = "3.1.0"
    }

    /**
     * Holds the OpenAPI specification in both YAML and JSON formats.
     *
     * @property yaml The OpenAPI specification in YAML format.
     * @property json The OpenAPI specification in JSON format.
     * @property errors A set of errors that occurred during the final verification of the OpenAPI schema.
     */
    data class OpenApiSpec(
        val yaml: String?,
        val json: String?,
        val errors: Set<String>?
    )
}
