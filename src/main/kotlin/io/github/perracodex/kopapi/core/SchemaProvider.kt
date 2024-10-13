/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.core

import io.github.perracodex.kopapi.inspector.TypeSchemaProvider
import io.github.perracodex.kopapi.inspector.schema.SchemaConflicts
import io.github.perracodex.kopapi.inspector.schema.TypeSchema
import io.github.perracodex.kopapi.plugin.dsl.elements.ApiInfo
import io.github.perracodex.kopapi.plugin.dsl.elements.ApiServerConfig
import io.github.perracodex.kopapi.serialization.SerializationUtils
import kotlin.collections.set
import kotlin.reflect.KType

/**
 * Singleton for registering and serving API information, server configurations, and endpoint metadata.
 *
 * Provides JSON representations of the registered data. JSONs are generated on the first request
 * and cached for subsequent requests to avoid redundant processing.
 */
internal object SchemaProvider {
    /** Represents the different sections in the debug JSON output. */
    private enum class SectionType {
        API_INFO,
        API_SERVERS,
        API_METADATA,
        SCHEMAS,
        SCHEMA_CONFLICTS
    }

    /** Flag to enable or disable the schema provider. If disabled, metadata collection is skipped. */
    var isEnabled: Boolean = true

    /** Set of registered API metadata objects. */
    private val apiMetadata: MutableSet<ApiMetadata> = mutableSetOf()

    /** Set of generated type schemas derived from registered API metadata. */
    private val schemas: MutableSet<TypeSchema> = mutableSetOf()

    /** Set of detected schema conflicts during schema generation. */
    private val schemaConflicts: MutableSet<SchemaConflicts.Conflict> = mutableSetOf()

    /** Cached JSON representations for debugging, categorized by section type. */
    private val debugJsonCache: MutableMap<SectionType, Set<String>> = mutableMapOf()

    /** Information about the API, such as title, version, and description. */
    var apiInfo: ApiInfo? = null
        private set

    /** Configuration details for the API servers. */
    var apiServers: Set<ApiServerConfig>? = null
        private set

    /**
     * Registers the API information.
     *
     * @param info The [ApiInfo] object containing metadata about the API.
     */
    fun registerApiInfo(info: ApiInfo?) {
        if (isEnabled) {
            synchronized(this) {
                if (apiInfo != null) {
                    throw KopapiException("API information has already been registered.")
                }
                apiInfo = info
            }
        }
    }

    /**
     * Registers the API server configurations.
     *
     * This method can only be called once. Subsequent calls will throw an [IllegalStateException].
     *
     * @param servers A set of [ApiServerConfig] objects representing server configurations.
     * @throws IllegalStateException if server configurations have already been registered.
     */
    fun registerServers(servers: Set<ApiServerConfig>) {
        if (isEnabled) {
            synchronized(this) {
                if (apiServers != null) {
                    throw KopapiException("API servers have already been registered.")
                }
                apiServers = servers
            }
        }
    }

    /**
     * Registers the [ApiMetadata] for a concrete API endpoint.
     *
     * @param metadata The [ApiMetadata] object representing the metadata of the API endpoint.
     */
    fun registerApiMetadata(metadata: ApiMetadata) {
        if (isEnabled) {
            synchronized(apiMetadata) {
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
     * @return A set of JSON strings representing the [ApiInfo].
     */
    fun getApiInfoJson(): Set<String> {
        return getOrGenerateDebugJson(instance = apiInfo, section = SectionType.API_INFO)
    }

    /**
     * Retrieves the API server configurations in JSON format.
     *
     * @return A set of JSON strings representing the [ApiServerConfig] objects.
     */
    fun getApiServersJson(): Set<String> {
        return getOrGenerateDebugJson(instance = apiServers, section = SectionType.API_SERVERS)
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
    private fun <T : Any> getOrGenerateDebugJson(instance: T?, section: SectionType): Set<String> {
        if (instance == null) {
            return emptySet()
        }

        return debugJsonCache[section] ?: run {
            val json: String = SerializationUtils.toJson(instance)
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
                .map { SerializationUtils.toJson(instance = it) }
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
    }
}
