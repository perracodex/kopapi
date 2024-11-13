/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.schema

import io.github.perracodex.kopapi.composer.SchemaComposer
import io.github.perracodex.kopapi.composer.annotation.ComposerApi
import io.github.perracodex.kopapi.composer.operation.OperationVerifier
import io.github.perracodex.kopapi.composer.request.RequestBodyComposer
import io.github.perracodex.kopapi.composer.response.ResponseComposer
import io.github.perracodex.kopapi.dsl.operation.element.ApiOperation
import io.github.perracodex.kopapi.dsl.path.element.ApiPath
import io.github.perracodex.kopapi.dsl.plugin.element.ApiConfiguration
import io.github.perracodex.kopapi.introspector.TypeSchemaProvider
import io.github.perracodex.kopapi.introspector.schema.SchemaConflicts
import io.github.perracodex.kopapi.introspector.schema.TypeSchema
import io.github.perracodex.kopapi.serialization.SerializationUtils
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.system.Tracer
import io.github.perracodex.kopapi.type.OpenApiFormat
import java.util.*

import kotlin.collections.set
import kotlin.reflect.KType

/**
 * Singleton for registering and serving OpenAPI schema data.
 */
@OptIn(ComposerApi::class)
internal object SchemaRegistry {
    private val tracer = Tracer<SchemaRegistry>()

    /** Represents the different sections in the debug JSON output. */
    enum class Section {
        API_INFO,
        API_SERVERS,
        API_SECURITY_SCHEMES,
        API_OPERATION,
        TYPE_SCHEMAS,
        SCHEMA_CONFLICTS,
        API_CONFIGURATION
    }

    /** Represents the different resource URLs for the API documentation. */
    enum class ResourceUrl {
        OPENAPI,
        REDOC,
        SWAGGER_UI,
    }

    /**
     * The enabled state of the schema provider. Default: `true`.
     *
     * Overridden by the plugin configuration.
     */
    private var isEnabled: Boolean = true

    /** Set of registered API Operations metadata. */
    private val apiOperation: MutableSet<ApiOperation> = mutableSetOf()

    /** Set of registered API Path metadata. */
    private val apiPath: MutableSet<ApiPath> = mutableSetOf()

    /** Set of generated type schemas derived from registered API metadata. */
    private val typeSchemas: MutableSet<TypeSchema> = mutableSetOf()

    /** Set of detected schema conflicts during schema generation. */
    private val schemaConflicts: MutableSet<SchemaConflicts.Conflict> = mutableSetOf()

    /** Cached JSON representations for debugging, categorized by section type. */
    private val debugJsonCache: MutableMap<Section, Set<String>> = mutableMapOf()

    /** Cached OpenAPI schema specification. */
    private var openApiSpec: SchemaComposer.OpenApiSpec? = null

    /** Set of errors detected during the schema generation process. */
    private val errors: SortedSet<String> = sortedSetOf(String.CASE_INSENSITIVE_ORDER)

    /** Information about the API, such as title, version, and description. */
    var apiConfiguration: ApiConfiguration? = null
        private set

    /** The [TypeSchemaProvider] instance used for introspecting types and generating schemas. */
    private val introspector = TypeSchemaProvider()

    /**
     * Registers the [ApiConfiguration] object for the Kopapi plugin.
     *
     * This method should be called only once during the application lifecycle.
     *
     * @param apiConfiguration The [ApiConfiguration] object representing the plugin configuration.
     */
    fun registerApiConfiguration(apiConfiguration: ApiConfiguration) {
        synchronized(this) {
            if (SchemaRegistry.apiConfiguration != null) {
                throw KopapiException("API Configuration has already been registered.")
            }
            isEnabled = apiConfiguration.isEnabled
            if (isEnabled) {
                SchemaRegistry.apiConfiguration = apiConfiguration
            } else {
                clear()
            }
        }
    }

    /**
     * Registers the [ApiOperation] for a concrete API endpoint.
     *
     * @param operation The [ApiOperation] object representing the metadata of the API endpoint.
     */
    fun registerApiOperation(operation: ApiOperation) {
        synchronized(apiOperation) {
            if (isEnabled) {
                OperationVerifier.verify(
                    newApiOperation = operation,
                    apiOperations = apiOperation
                )?.let { errors ->
                    this.errors.addAll(errors)
                }

                apiOperation.add(operation)
            }
        }
    }

    /**
     * Registers the [ApiPath] for a concrete API endpoint.
     *
     * @param path The [ApiPath] object representing the metadata of the API endpoint.
     */
    fun registerApiPath(path: ApiPath) {
        synchronized(apiPath) {
            if (isEnabled) {
                apiPath.add(path)
            }
        }
    }

    /**
     * Clears all cached data.
     * This method should be called if the plugin is disabled.
     *
     * `Routes.api` definitions can be registered before or after the plugin is installed,
     * so if the plugin is disabled, we need to clear all cached data after the application
     * starts to free up resources.
     */
    fun clear() {
        apiOperation.clear()
        apiPath.clear()
        typeSchemas.clear()
        schemaConflicts.clear()
        debugJsonCache.clear()
        openApiSpec = null
        introspector.reset()
        apiConfiguration = null
    }

    /**
     * Processes and collects the [TypeSchema] objects from the registered [ApiOperation] objects.
     */
    private fun processTypeSchemas() {
        if (typeSchemas.isNotEmpty()) {
            return
        }

        apiOperation.forEach { metadata ->
            // Introspect each parameter type.
            metadata.parameters?.forEach { parameter ->
                introspectType(type = parameter.type)
            }

            // Introspect the request body type.
            metadata.requestBody?.let { requestBody ->
                RequestBodyComposer.compose(requestBody = requestBody)
            }

            // Introspect each response type.
            metadata.responses?.let { responses ->
                ResponseComposer.compose(responses = responses)
            }
        }

        // Collect and store sorted schemas.
        introspector.getTypeSchemas().sortedWith(
            compareBy { it.name }
        ).forEach { schema ->
            typeSchemas.add(schema)
        }

        // Collect and store sorted schema conflicts.
        introspector.getConflicts().sortedWith(
            compareBy { it.name }
        ).forEach { conflict ->
            schemaConflicts.add(conflict)
        }
    }

    /**
     * Introspects a type using the provided [TypeSchemaProvider], excluding the [Unit] type.
     *
     * @param type The [KType] to introspect.
     * @return The [TypeSchema] object representing the introspected type, or `null` if the type is [Unit].
     */
    fun introspectType(type: KType): TypeSchema? {
        return when (type.classifier) {
            Unit::class -> return null
            else -> introspector.introspect(kType = type)
        }
    }

    /**
     * Retrieves the serialized JSON data for a specific section in raw unformatted format.
     *
     * @param section The [Section] indicating the category of the object.
     * @return A set of JSON strings representing the data for the specified section.
     */
    fun getDebugSection(section: Section): Set<String> {
        return when (section) {
            Section.API_CONFIGURATION -> getConfigurationSectionJson(
                instance = apiConfiguration,
                section = Section.API_CONFIGURATION
            )

            Section.API_INFO -> getConfigurationSectionJson(
                instance = apiConfiguration?.apiInfo,
                section = Section.API_INFO
            )

            Section.API_SERVERS -> getConfigurationSectionJson(
                instance = apiConfiguration?.apiServers,
                section = Section.API_SERVERS
            )

            Section.API_SECURITY_SCHEMES -> getConfigurationSectionJson(
                instance = apiConfiguration?.apiSecuritySchemes,
                section = Section.API_SECURITY_SCHEMES
            )

            Section.API_OPERATION -> {
                getOrGenerateDebugJson(
                    instance = apiOperation,
                    section = Section.API_OPERATION
                )
            }

            Section.TYPE_SCHEMAS -> {
                getOrGenerateDebugJson(
                    instance = typeSchemas,
                    section = Section.TYPE_SCHEMAS
                )
            }

            Section.SCHEMA_CONFLICTS -> {
                getOrGenerateDebugJson(
                    instance = schemaConflicts,
                    section = Section.SCHEMA_CONFLICTS
                )
            }
        }
    }

    /**
     * Helper method to handle [ApiConfiguration] sections serialization.
     *
     * @param T The type of the instance to serialize.
     * @param instance The instance to serialize.
     * @param section The [Section] to process.
     * @return A set of JSON strings representing the serialized data for the section.
     */
    private fun <T : Any> getConfigurationSectionJson(instance: T?, section: Section): Set<String> {
        return instance?.let {
            getOrGenerateDebugJson(instance = it, section = section)
        } ?: run {
            tracer.warning("No API configuration found.")
            emptySet()
        }
    }

    /**
     * Serializes an object into a JSON string, utilizing caching for efficiency.
     *
     * @param T The type of the object to serialize.
     * @param instance The object to serialize.
     * @param section The [Section] indicating the category of the object.
     * @return A list containing a single JSON string representing the serialized object.
     */
    private fun <T : Any> getOrGenerateDebugJson(instance: T?, section: Section): Set<String> {
        if (instance == null) {
            return emptySet()
        }

        return debugJsonCache[section] ?: run {
            val json: String = SerializationUtils().toRawJson(instance)
            debugJsonCache[section] = setOf(json)
            setOf(json)
        }
    }

    /**
     * Serializes a set of objects into a list of JSON strings, utilizing caching for efficiency.
     *
     * @param T The type of objects to serialize.
     * @param instance The set of objects to serialize.
     * @param section The [Section] indicating the category of objects.
     * @return A list of JSON strings representing the serialized objects.
     */
    private fun <T : Any> getOrGenerateDebugJson(instance: Set<T>, section: Section): Set<String> {
        processTypeSchemas()

        return debugJsonCache[section] ?: run {
            instance
                .map { SerializationUtils().toRawJson(instance = it) }
                .toSortedSet()
                .also { sortedSet ->
                    debugJsonCache[section] = sortedSet
                }
        }
    }

    /**
     * Serves the OpenAPI schema in the specified format.
     *
     * @param format The [OpenApiFormat] to serve.
     * @param cacheAllFormats If `true`, all available formats will be generated and cached,
     *                        otherwise, only the specified format will be generated and cached.
     *                        This is useful for improving performance when multiple formats
     *                        may eventually be needed, such as in the debug panel.
     * @return The OpenAPI schema in the specified format.
     */
    fun getOpenApiSchema(format: OpenApiFormat, cacheAllFormats: Boolean): String {
        if (!isEnabled) {
            throw KopapiException("Attempted to generate OpenAPI schema while plugin is disabled.")
        }

        // Return cached schema if available.
        val openApiSchema: String? = openApiSpec?.let { cache ->
            when (format) {
                OpenApiFormat.JSON -> cache.json
                OpenApiFormat.YAML -> cache.yaml
            }
        }
        if (openApiSchema != null) {
            return openApiSchema
        }

        // Generate the OpenAPI schema.
        return apiConfiguration?.let { configuration ->
            processTypeSchemas()

            val composedSpec: SchemaComposer.OpenApiSpec = SchemaComposer(
                apiConfiguration = configuration,
                apiPaths = apiPath,
                apiOperations = apiOperation,
                registrationErrors = errors,
                schemaConflicts = schemaConflicts,
                format = if (cacheAllFormats) null else format
            ).compose()
            openApiSpec = composedSpec
            composedSpec.errors?.let { compositionErrors ->
                errors.addAll(compositionErrors)
            }

            when (format) {
                OpenApiFormat.JSON -> composedSpec.json
                OpenApiFormat.YAML -> composedSpec.yaml
            }
        } ?: throw KopapiException("Failed to generate OpenAPI schema.")
    }

    /**
     * Retrieves the set of [TypeSchema] objects generated from the registered API metadata.
     */
    fun getSchemaTypes(): Set<TypeSchema> {
        processTypeSchemas()
        return typeSchemas
    }

    /**
     * Retrieves the URL for a specific API documentation resource.
     *
     * @param url The [ResourceUrl] indicating the type of resource to retrieve.
     * @return The URL for the specified API documentation resource.
     */
    fun getResourceUrl(url: ResourceUrl): String {
        return apiConfiguration?.let { configuration ->
            when (url) {
                ResourceUrl.OPENAPI -> configuration.apiDocs.openApiUrl
                ResourceUrl.REDOC -> configuration.apiDocs.redocUrl
                ResourceUrl.SWAGGER_UI -> configuration.apiDocs.swagger.url
            }
        } ?: ""
    }

    /**
     * Retrieves the set of errors detected during the schema generation process.
     */
    fun getErrors(): Set<String> {
        if (errors.isNotEmpty() || schemaConflicts.isNotEmpty()) {
            return errors + getFormattedTypeConflicts(schemaConflicts = schemaConflicts)
        }
        return emptySet()
    }

    /**
     * Gets the formatted type conflicts for presentation.
     */
    fun getFormattedTypeConflicts(schemaConflicts: Set<SchemaConflicts.Conflict>): Set<String> {
        return schemaConflicts.map { conflict ->
            "'${conflict.name}': ${conflict.conflictingTypes.joinToString(separator = ", ") { "[$it]" }}"
        }.toSortedSet()
    }
}
