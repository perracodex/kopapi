/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer

import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.github.perracodex.kopapi.dsl.operation.elements.ApiOperation
import io.github.perracodex.kopapi.dsl.operation.elements.ApiSecurityScheme
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiInfo
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiServerConfig
import io.github.perracodex.kopapi.inspector.TypeSchemaProvider
import io.github.perracodex.kopapi.inspector.schema.SchemaConflicts
import io.github.perracodex.kopapi.inspector.schema.TypeSchema
import io.github.perracodex.kopapi.plugin.Configuration
import io.github.perracodex.kopapi.serialization.SerializationUtils
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.system.Tracer
import kotlin.collections.set
import kotlin.reflect.KType

/**
 * Singleton for registering and serving API information,
 * server configurations, and endpoint metadata.
 */
internal object SchemaRegistry {
    private val tracer = Tracer<SchemaRegistry>()

    /**
     * Represents the format of the final OpenAPI schema output.
     */
    enum class Format {
        /** JSON format. */
        JSON,

        /** YAML format. */
        YAML
    }

    /** Represents the different sections in the debug JSON output. */
    private enum class SectionType {
        API_INFO,
        API_SERVERS,
        API_SECURITY_SCHEMES,
        API_METADATA,
        SCHEMAS,
        SCHEMA_CONFLICTS
    }

    /**
     * The enabled state of the schema provider. Defaults to `true`.
     * Overridden by the plugin configuration.
     */
    private var isEnabled: Boolean = true

    /** Set of registered API Operations metadata. */
    private val apiOperation: MutableSet<ApiOperation> = mutableSetOf()

    /** Set of generated type schemas derived from registered API metadata. */
    private val schemas: MutableSet<TypeSchema> = mutableSetOf()

    /** Set of detected schema conflicts during schema generation. */
    private val schemaConflicts: MutableSet<SchemaConflicts.Conflict> = mutableSetOf()

    /** Cached JSON representations for debugging, categorized by section type. */
    private val debugJsonCache: MutableMap<SectionType, Set<String>> = mutableMapOf()

    /** Cached OpenAPI schema representations, categorized by format. */
    private val openApiSchemaCache: MutableMap<Format, String> = mutableMapOf()

    /** Information about the API, such as title, version, and description. */
    var configuration: Configuration? = null
        private set

    /**
     * Registers the [Configuration] object for the Kopapi plugin.
     *
     * This method should be called only once during the application lifecycle.
     *
     * @param configuration The [Configuration] object representing the plugin configuration.
     */
    fun registerConfiguration(configuration: Configuration) {
        synchronized(this) {
            if (SchemaRegistry.configuration != null) {
                throw KopapiException("Configuration has already been registered.")
            }
            isEnabled = configuration.isEnabled
            if (isEnabled) {
                SchemaRegistry.configuration = configuration
                assertSecuritySchemeNamesUniqueness()
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
                apiOperation.add(operation)
                assertSecuritySchemeNamesUniqueness()
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
        schemas.clear()
        schemaConflicts.clear()
        debugJsonCache.clear()
        openApiSchemaCache.clear()
        configuration = null
    }

    /**
     * Helper method for checking if a security scheme name is already registered.
     *
     * `Security schemes` can be defined globally in the plugin configuration or within each Route endpoint.
     * As these are registered at different timings, we need to ensure that security scheme names are unique
     * across the entire API.
     *
     * @throws KopapiException if detected that any security scheme names are not unique
     * between global and Route definitions.
     */
    private fun assertSecuritySchemeNamesUniqueness() {
        val global: Set<ApiSecurityScheme>? = configuration?.apiSecuritySchemes

        // Check between global and API metadata.
        if (!global.isNullOrEmpty() && apiOperation.isNotEmpty()) {
            val globalSchemeNames: Set<String> = global.map { it.schemeName.lowercase() }.toSet()

            apiOperation.forEach { metadata ->
                metadata.securitySchemes?.forEach { scheme ->
                    if (scheme.schemeName.lowercase() in globalSchemeNames) {
                        throw KopapiException(
                            "Attempting to register security scheme with name '${scheme.schemeName}' more than once.\n" +
                                    "Security scheme `names` must be unique across the entire API, " +
                                    "both globally and for all Routes.\n" +
                                    "['${metadata.method.value}'] '${metadata.path}'"
                        )
                    }
                }
            }
        }
    }

    /**
     * Processes and collects the [TypeSchema] objects from the registered [ApiOperation] objects.
     */
    private fun processSchemas() {
        if (schemas.isNotEmpty()) {
            return
        }

        val inspector = TypeSchemaProvider()

        apiOperation.forEach { metadata ->
            // Inspect each parameter type.
            metadata.parameters?.forEach { parameter ->
                inspectType(inspector = inspector, type = parameter.type)
            }

            // Inspect the request body type.
            metadata.requestBody?.let { requestBody ->
                inspectType(inspector = inspector, type = requestBody.type)
            }

            // Inspect each response type.
            metadata.responses?.forEach { response ->
                response.type?.let {
                    inspectType(inspector = inspector, type = response.type)
                }
            }
        }

        // Collect and store sorted schemas.
        inspector.getTypeSchemas().sortedWith(
            compareBy { it.name }
        ).forEach { schema ->
            schemas.add(schema)
        }

        // Collect and store sorted schema conflicts.
        inspector.getConflicts().sortedWith(
            compareBy { it.name }
        ).forEach { conflict ->
            schemaConflicts.add(conflict)
        }
    }

    /**
     * Inspects a type using the provided [TypeSchemaProvider] if it's not of type [Unit].
     *
     * @param inspector The [TypeSchemaProvider] instance used for inspection.
     * @param type The [KType] to inspect.
     */
    private fun inspectType(inspector: TypeSchemaProvider, type: KType) {
        if (type.classifier != Unit::class) {
            inspector.inspect(kType = type)
        }
    }

    /**
     * Retrieves the API information in JSON format.
     *
     * @return The JSON string representing the [ApiInfo].
     */
    fun getApiInfoJson(): String? {
        configuration?.apiInfo?.let { apiInfo ->
            return getOrGenerateDebugJson(instance = apiInfo, section = SectionType.API_INFO).first()
        }
        tracer.warning("No configuration found.")
        return null
    }

    /**
     * Retrieves the API information in JSON format.
     *
     * @return A set of JSON strings representing the [ApiInfo].
     */
    fun getSecuritySchemesJson(): Set<String> {
        configuration?.apiSecuritySchemes?.let { schemes ->
            return getOrGenerateDebugJson(instance = schemes, section = SectionType.API_SECURITY_SCHEMES)
        }
        tracer.warning("No configuration found.")
        return emptySet()
    }

    /**
     * Retrieves the API server configurations in JSON format.
     *
     * @return A set of JSON strings representing the [ApiServerConfig] objects.
     */
    fun getApiServersJson(): Set<String> {
        configuration?.apiServers?.let { apiServers ->
            return getOrGenerateDebugJson(instance = apiServers, section = SectionType.API_SERVERS)
        }
        tracer.warning("No configuration found.")
        return emptySet()
    }

    /**
     * Retrieves the API metadata in JSON format.
     *
     * @return A set of JSON strings representing the registered [ApiOperation].
     */
    fun getApiOperationJson(): Set<String> {
        return getOrGenerateDebugJson(instance = apiOperation, section = SectionType.API_METADATA)
    }

    /**
     * Retrieves the generated schemas in JSON format.
     *
     * @return A set of JSON strings representing the [TypeSchema] objects.
     */
    fun getSchemasJson(): Set<String> {
        return getOrGenerateDebugJson(instance = schemas, section = SectionType.SCHEMAS)
    }

    /**
     * Retrieves the schema conflicts in JSON format.
     *
     * @return A list of JSON strings representing the [SchemaConflicts.Conflict] objects.
     */
    fun getSchemaConflictsJson(): Set<String> {
        return getOrGenerateDebugJson(instance = schemaConflicts, section = SectionType.SCHEMA_CONFLICTS)
    }

    /**
     * Serializes an object into a JSON string, utilizing caching for efficiency.
     *
     * @param T The type of the object to serialize.
     * @param instance The object to serialize.
     * @param section The [SectionType] indicating the category of the object.
     * @return A list containing a single JSON string representing the serialized object.
     */
    @Suppress("SameParameterValue")
    private fun <T : Any> getOrGenerateDebugJson(instance: T?, section: SectionType): Set<String> {
        if (instance == null) {
            return emptySet()
        }

        return debugJsonCache[section] ?: run {
            val json: String = SerializationUtils.toRawJson(instance)
            debugJsonCache[section] = setOf(json)
            setOf(json)
        }
    }

    /**
     * Serializes a set of objects into a list of JSON strings, utilizing caching for efficiency.
     *
     * @param T The type of objects to serialize.
     * @param instance The set of objects to serialize.
     * @param section The [SectionType] indicating the category of objects.
     * @return A list of JSON strings representing the serialized objects.
     */
    private fun <T : Any> getOrGenerateDebugJson(instance: Set<T>, section: SectionType): Set<String> {
        processSchemas()

        return debugJsonCache[section] ?: run {
            instance
                .map { SerializationUtils.toRawJson(instance = it) }
                .toSortedSet()
                .also { sortedSet ->
                    debugJsonCache[section] = sortedSet
                }
        }
    }

    /**
     * Serves the OpenAPI schema in the specified format.
     *
     * @param format The [Format] of the OpenAPI schema to serve.
     * @return The OpenAPI schema in the specified format.
     */
    @OptIn(ComposerAPI::class)
    fun getOpenApiSchema(format: Format): String {
        if (!isEnabled || configuration == null) {
            return ""
        }
        openApiSchemaCache.getOrDefault(key = format, defaultValue = null)?.let { schema ->
            return schema
        }

        configuration?.let { configuration ->
            processSchemas()

            val schema: String = SchemaComposer(
                configuration = configuration,
                apiOperations = apiOperation,
                schemaConflicts = schemaConflicts
            ).compose(format = format)

            openApiSchemaCache[format] = schema
            return schema
        } ?: run {
            tracer.warning("No configuration found.")
            return ""
        }
    }
}
