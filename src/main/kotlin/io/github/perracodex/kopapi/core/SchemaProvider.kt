/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.core

import io.github.perracodex.kopapi.inspector.TypeSchemaProvider
import io.github.perracodex.kopapi.inspector.schema.SchemaConflicts
import io.github.perracodex.kopapi.inspector.schema.TypeSchema
import io.github.perracodex.kopapi.serialization.SerializationUtils

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
    private val debugJson: MutableMap<SectionType, String> = mutableMapOf()

    /** Registers a new [ApiMetadata] object.*/
    fun register(apiMetadata: ApiMetadata) {
        if (isEnabled) {
            this.apiMetadata.add(apiMetadata)
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
        inspector.getTypeSchemas().forEach { schema ->
            schemas.add(schema)
        }

        schemaConflicts.addAll(inspector.getConflicts())
    }

    /**
     * Get the full API metadata in JSON format.
     *
     * @return The JSON string of the list of [ApiMetadata] objects.
     */
    fun getApiMetadataJson(): String {
        return debugJson[SectionType.API_METADATA] ?: run {
            val json: String = SerializationUtils.toJson(instance = apiMetadata)
            debugJson[SectionType.API_METADATA] = json
            json
        }
    }

    /**
     * Get the schemas in JSON format.
     *
     * @return The JSON string of the list of [ApiMetadata] objects.
     */
    fun getSchemasJson(): String {
        return debugJson[SectionType.SCHEMAS] ?: run {
            processSchemas()
            val json: String = SerializationUtils.toJson(instance = schemas)
            debugJson[SectionType.SCHEMAS] = json
            json
        }
    }

    /**
     * Get the schema conflicts in JSON format.
     *
     * @return The JSON string of the list of [SchemaConflicts.Conflict] objects.
     */
    fun getSchemaConflictsJson(): String {
        return debugJson[SectionType.SCHEMA_CONFLICTS] ?: run {
            processSchemas()
            val json: String = SerializationUtils.toJson(instance = schemaConflicts)
            debugJson[SectionType.SCHEMA_CONFLICTS] = json
            json
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
