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

/**
 * Builder for the API metadata and schemas.
 */
internal object SchemaProvider {
    /** The different sections of the debug JSON. */
    private enum class SectionType {
        API_METADATA,
        SCHEMAS,
        SCHEMA_CONFLICTS
    }

    /** Whether the provider is enabled. If disabled, no metadata will be collected. */
    var isEnabled: Boolean = true

    /** The list of registered routes [ApiMetadata]. */
    private val apiMetadata: MutableSet<ApiMetadata> = mutableSetOf()

    /** The list of generated schemas from the all the registered [ApiMetadata] objects. */
    private val schemas: MutableSet<TypeSchema> = mutableSetOf()

    /** The list of detected conflicts when generating the schemas. */
    private val schemaConflicts: MutableSet<SchemaConflicts.Conflict> = mutableSetOf()

    /** The raw pre-process JSON data for debugging purposes. */
    private val debugJson: MutableMap<SectionType, List<String>> = mutableMapOf()


    /** The API documentation information. */
    var apiInfo: ApiInfo? = null
        private set

    /**
     * Holds the set of server configurations.
     */
    var apiServers: Set<ApiServerConfig>? = null
        private set

    /**
     * Registers the [ApiInfo] for the API.
     *
     * @param info The [ApiInfo] object representing the metadata of the API.
     */
    fun registerApiInfo(info: ApiInfo?) {
        apiInfo = info
    }

    /**
     * Assign the set of server configurations once.
     * This method can only be called once; subsequent calls will throw an exception.
     *
     * @param servers The set of server configurations to assign.
     */
    fun registerServers(servers: Set<ApiServerConfig>) {
        apiServers = servers
    }

    /**
     * Registers the [ApiMetadata] for a concrete API endpoint.
     *
     * @param metadata The [ApiMetadata] object representing the metadata of the API endpoint.
     */
    fun registerApiMetadata(metadata: ApiMetadata) {
        if (isEnabled) {
            this.apiMetadata.add(metadata)
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
            // Inspect parameters.
            metadata.parameters?.forEach { parameter ->
                if (parameter.type.classifier != Unit::class) {
                    inspector.inspect(kType = parameter.type)
                }
            }

            // Inspect request body.
            metadata.requestBody?.let { requestBody ->
                if (requestBody.type.classifier != Unit::class) {
                    inspector.inspect(kType = requestBody.type)
                }
            }

            // Inspect responses.
            metadata.responses?.forEach { response ->
                if (response.type.classifier != Unit::class) {
                    inspector.inspect(kType = response.type)
                }
            }
        }

        // Add the collected schemas to the set.
        inspector.getTypeSchemas().sortedWith(
            compareBy { it.name }
        ).forEach { schema ->
            schemas.add(schema)
        }

        // Add the collected conflicts to the set.
        inspector.getConflicts().sortedWith(
            compareBy { it.name }
        ).forEach { conflict ->
            schemaConflicts.add(conflict)
        }
    }

    /**
     * Get the full API metadata in JSON format.
     *
     * @return The [ApiMetadata] objects as a list of JSON strings.
     */
    fun getApiMetadataJson(): List<String> {
        return getDebugJson(instance = apiMetadata, key = SectionType.API_METADATA)
    }

    /**
     * Get the schemas in JSON format.
     *
     * @return The [TypeSchema] objects as a list of JSON strings.
     */
    fun getSchemasJson(): List<String> {
        return getDebugJson(instance = schemas, key = SectionType.SCHEMAS)
    }

    /**
     * Get the schema conflicts in JSON format.
     *
     * @return The [SchemaConflicts.Conflict] objects as a list of JSON strings.
     */
    fun getSchemaConflictsJson(): List<String> {
        return getDebugJson(instance = schemaConflicts, key = SectionType.SCHEMA_CONFLICTS)
    }

    /**
     * Serializes a set of raw not-processed objects into a list of JSON strings,
     * caching the result for future requests.
     *
     * @param T The type of objects to serialize. Must extend [Any].
     * @param instance The set of objects to serialize into JSON.
     * @param key The [SectionType] representing the type of JSON to cache.
     * @return A list of JSON strings representing the serialized [instance].
     */
    private fun <T : Any> getDebugJson(instance: MutableSet<T>, key: SectionType): List<String> {
        return debugJson[key] ?: run {
            processSchemas()
            instance.map { item ->
                SerializationUtils.toJson(instance = item)
            }.also {
                debugJson[key] = it
            }
        }
    }

    /**
     * Clears all the registered metadata and schemas.
     */
    fun clear() {
        apiMetadata.clear()
        schemas.clear()
        schemaConflicts.clear()
        debugJson.clear()
    }
}
