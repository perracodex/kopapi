/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.core

import io.github.perracodex.kopapi.inspector.TypeInspector
import io.github.perracodex.kopapi.inspector.schema.TypeSchema
import io.github.perracodex.kopapi.serialization.SerializationUtils

/**
 * Builder for the API metadata and schemas.
 */
internal object SchemaProvider {
    /** The list of registered routes [ApiMetadata]. */
    private val apiMetadata: MutableSet<ApiMetadata> = mutableSetOf()

    /** The list of generated schemas from the all the registered [ApiMetadata] objects. */
    private val schemas: MutableSet<TypeSchema> = mutableSetOf()

    /** The full API metadata in JSON format. */
    private var apiMetadataJson: String? = null

    /** The raw not finalized  schema in JSON format. */
    private var schemaRawJson: String? = null

    /** Registers a new [ApiMetadata] object.*/
    fun register(apiMetadata: ApiMetadata) {
        this.apiMetadata.add(apiMetadata)
    }

    /**
     * Processes and collects the [TypeSchema] objects from the registered [ApiMetadata] objects.
     *
     * @return The list of [TypeSchema] objects.
     */
    fun getSchema(): Set<TypeSchema> {
        if (schemas.isEmpty()) {
            val inspector = TypeInspector()

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
        }

        return schemas
    }

    /**
     * Returns the collected API metadata in JSON format.
     */
    fun getDebugJson(): String {
        return """
            {
                "routes-metadata": ${getApiMetadataJson()},
                "object-schemas": ${getSchemasJson(debug = true)}
            }
        """.trimIndent()
    }

    /**
     * Get the full API metadata in JSON format.
     *
     * @return The JSON string of the list of [ApiMetadata] objects.
     */
    private fun getApiMetadataJson(): String {
        return apiMetadataJson ?: run {
            val json: String = SerializationUtils.toJson(instance = apiMetadata)
            apiMetadataJson = json
            json
        }
    }

    /**
     * Get the schemas in JSON format.
     *
     * @param debug `True` to return them in raw not finalized format.
     * @return The JSON string of the list of [ApiMetadata] objects.
     */
    private fun getSchemasJson(debug: Boolean): String {
        return schemaRawJson ?: run {
            val schemas: Set<TypeSchema> = getSchema()
            val json: String = SerializationUtils.toJson(instance = schemas)
            schemaRawJson = json
            json
        }
    }
}
