/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.core.composer

import io.github.perracodex.kopapi.core.ApiMetadata
import io.github.perracodex.kopapi.core.KopapiException
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiInfo
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiServerConfig
import io.github.perracodex.kopapi.inspector.TypeSchemaProvider
import io.github.perracodex.kopapi.inspector.schema.SchemaConflicts
import io.github.perracodex.kopapi.inspector.schema.TypeSchema
import io.github.perracodex.kopapi.plugin.Configuration
import io.github.perracodex.kopapi.serialization.SerializationUtils
import io.github.perracodex.kopapi.utils.Tracer
import kotlin.collections.set
import kotlin.reflect.KType

/**
 * Singleton for registering and serving API information, server configurations, and endpoint metadata.
 * This class is responsible for generating the OpenAPI schema from the registered metadata.
 */
internal object SchemaComposer {
    private val tracer = Tracer<SchemaComposer>()

    /** The version of the OpenAPI specification used by the plugin. */
    private const val OPEN_API_VERSION = "3.1.0"

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

    /** Set of registered API metadata objects. */
    private val apiMetadata: MutableSet<ApiMetadata> = mutableSetOf()

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
            if (SchemaComposer.configuration != null) {
                throw KopapiException("Configuration has already been registered.")
            }
            isEnabled = configuration.isEnabled
            if (isEnabled) {
                SchemaComposer.configuration = configuration
            }
        }
    }

    /**
     * Registers the [ApiMetadata] for a concrete API endpoint.
     *
     * @param metadata The [ApiMetadata] object representing the metadata of the API endpoint.
     */
    fun registerApiMetadata(metadata: ApiMetadata) {
        synchronized(apiMetadata) {
            if (isEnabled) {
                apiMetadata.add(metadata)
            }
        }
    }

    /**
     * Processes and collects the [TypeSchema] objects from the registered [ApiMetadata] objects.
     */
    private fun processSchemas() {
        if (schemas.isNotEmpty()) {
            return
        }

        val inspector = TypeSchemaProvider()

        apiMetadata.forEach { metadata ->
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
                inspectType(inspector = inspector, type = response.type)
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
     * @return A set of JSON strings representing the registered [ApiMetadata].
     */
    fun getApiMetadataJson(): Set<String> {
        return getOrGenerateDebugJson(instance = apiMetadata, section = SectionType.API_METADATA)
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
     * Clears all registered metadata, schemas, conflicts, and cached JSON data.
     */
    fun clear() {
        apiMetadata.clear()
        schemas.clear()
        schemaConflicts.clear()
        debugJsonCache.clear()
        configuration = null
    }

    /**
     * Serves the OpenAPI schema in the specified format.
     *
     * @param format The [Format] of the OpenAPI schema to serve.
     * @return The OpenAPI schema in the specified format.
     */
    fun getOpenApiSchema(format: Format): String {
        if (!isEnabled) {
            return ""
        }
        openApiSchemaCache.getOrDefault(key = format, defaultValue = null)?.let { schema ->
            return schema
        }

        processSchemas()

        return configuration?.let { configuration ->
            // Compose the `Info` section.
            val infoSection: ApiInfo = InfoSectionComposer.compose(
                apiInfo = configuration.apiInfo,
                schemaConflicts = schemaConflicts
            )

            // Get the `Servers` section.
            val serversSection: List<ApiServerConfig>? = configuration.apiServers?.toList()

            val openApiSchema = OpenAPiSchema(
                openapi = OPEN_API_VERSION,
                info = infoSection,
                servers = serversSection
            )

            val schema: String = when (format) {
                Format.JSON -> SerializationUtils.toRawJson(instance = openApiSchema)
                Format.YAML -> SerializationUtils.toYaml(instance = openApiSchema)
            }

            openApiSchemaCache[format] = schema
            return@let schema
        } ?: run {
            tracer.warning("No configuration found.")
            return ""
        }
    }

    private data class OpenAPiSchema(
        val openapi: String,
        val info: ApiInfo,
        val servers: List<ApiServerConfig>?
    )
}
