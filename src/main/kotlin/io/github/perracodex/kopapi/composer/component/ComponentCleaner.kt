/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.component

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.perracodex.kopapi.composer.SchemaComposer
import io.github.perracodex.kopapi.composer.annotation.ComposerApi
import io.github.perracodex.kopapi.introspection.schema.SchemaConflicts
import io.github.perracodex.kopapi.schema.OpenApiSchema
import io.github.perracodex.kopapi.schema.facet.ElementSchema
import io.github.perracodex.kopapi.serialization.SerializationUtils
import io.github.perracodex.kopapi.system.Tracer
import io.github.perracodex.kopapi.type.OpenApiFormat

/**
 * Cleans the OpenAPI schema by removing orphaned schemas.
 * These are unused schemas that are not referenced, as they may have been defined
 * in properties marked as `transient`.
 *
 * @property openApiSpec The OpenAPI specification structure.
 * @param openApiSpec The OpenAPI specification to clean.
 * @param format The format to serialize the OpenAPI schema in, or `null` to serialize to all formats.
 * @param schemaConflicts A set of existing schema conflicts. Orphaned schemas will be removed from this set.
 */
@OptIn(ComposerApi::class)
internal class ComponentCleaner(
    private val openApiSchema: OpenApiSchema,
    private val openApiSpec: SchemaComposer.OpenApiSpec,
    private val format: OpenApiFormat?,
    private val schemaConflicts: MutableSet<SchemaConflicts.Conflict>,
) {
    private val tracer: Tracer = Tracer<ComponentCleaner>()

    /**
     * Cleans the OpenAPI schema by removing orphaned schemas.
     */
    fun clean(): SchemaComposer.OpenApiSpec {
        require(!openApiSpec.yaml.isNullOrBlank() || !openApiSpec.json.isNullOrBlank()) {
            "No OpenAPI specification found."
        }
        val serializationUtils = SerializationUtils()

        // Determine the appropriate mapper and schema content.
        val (mapper: ObjectMapper, schema: String) = when {
            !openApiSpec.yaml.isNullOrBlank() -> serializationUtils.openApiYamlMapper to openApiSpec.yaml
            !openApiSpec.json.isNullOrBlank() -> serializationUtils.openApiJsonMapper to openApiSpec.json
            else -> return openApiSpec
        }
        val parsedMap: Map<*, *> = mapper.readValue(schema, Map::class.java) as Map<*, *>

        // Transform the map to ensure the correct types.
        val openApiMap: MutableMap<String, Any?> = parsedMap.entries.associate { (key, value) ->
            key.toString() to value
        }.toMutableMap()

        // Collect the names of the schemas that are used in the OpenAPI schema.
        val usedRefs: MutableSet<String> = mutableSetOf()
        collectUsedRefs(node = openApiMap, usedRefs = usedRefs)

        // Remove orphaned schemas, and re-serialize the OpenAPI schema if necessary.
        if (removeOrphanSchemas(openApiSchema = openApiSchema, openApiMap = openApiMap, usedRefs = usedRefs)) {
            val (yaml: String?, json: String?) = when (format) {
                OpenApiFormat.YAML -> serializationUtils.toYaml(instance = openApiMap) to null
                OpenApiFormat.JSON -> null to serializationUtils.toJson(instance = openApiMap)
                null -> serializationUtils.toYaml(instance = openApiMap) to serializationUtils.toJson(instance = openApiMap)
            }

            return openApiSpec.copy(
                yaml = yaml,
                json = json
            )
        }

        return openApiSpec
    }

    /**
     * Collects the names of the schemas that are used in the OpenAPI schema.
     *
     * @param node The current node to traverse.
     * @param usedRefs The set of used schema names.
     */
    private fun collectUsedRefs(node: Any?, usedRefs: MutableSet<String>) {
        when (node) {
            is Map<*, *> -> {
                node.forEach { (key, value) ->
                    if (key == ElementSchema.Reference.REFERENCE_KEY && value is String) {
                        // Add the $ref value to the set.
                        usedRefs.add(value)
                    }
                    // Continue traversing.
                    collectUsedRefs(node = value, usedRefs = usedRefs)
                }
            }

            is List<*> -> node.forEach {
                collectUsedRefs(node = it, usedRefs = usedRefs)
            }
        }
    }

    /**
     * Removes orphaned schemas from the OpenAPI schema.
     *
     * @param openApiSchema The OpenAPI schema structure.
     * @param openApiMap The OpenAPI schema to clean.
     * @param usedRefs The set of used schema names.
     * @return `true` if orphaned schemas were removed; `false` otherwise.
     */
    private fun removeOrphanSchemas(
        openApiSchema: OpenApiSchema,
        openApiMap: MutableMap<String, Any?>,
        usedRefs: Set<String>
    ): Boolean {
        // Retrieve the 'components' section as a non-empty mutable map.
        val components: MutableMap<String, Any?> = getNonEmptyMapSection(
            source = openApiMap,
            key = COMPONENTS_KEY,
            errorMessage = "No valid or non-empty '$COMPONENTS_KEY' section found. No schemas removed."
        ) ?: return false

        // Retrieve the 'schemas' section as a non-empty mutable map.
        val schemas: MutableMap<String, Any?> = getNonEmptyMapSection(
            source = components,
            key = SCHEMA_KEY,
            errorMessage = "No valid or non-empty '$SCHEMA_KEY' section found within '$COMPONENTS_KEY'. No schemas removed."
        ) ?: return false

        // Extract just the schema names from the used references.
        val usedSchemaNames: Set<String> = usedRefs.mapNotNull { ref ->
            ref.substringAfterLast(delimiter = "/", missingDelimiterValue = "").takeIf { it.isNotEmpty() }
        }.toSet()

        val orphans: Set<String> = schemas.keys - usedSchemaNames
        if (orphans.isEmpty()) {
            return false
        }

        // Remove each orphaned schema.
        orphans.forEach { orphan ->
            schemas.remove(orphan)
            openApiSchema.components?.componentSchemas?.remove(orphan)
            schemaConflicts.removeIf { it.name == orphan }
        }

        // Update the document with cleaned schemas.
        components[SCHEMA_KEY] = schemas
        openApiMap[COMPONENTS_KEY] = components

        tracer.debug("Removed orphan schemas: $orphans")
        return true
    }

    /**
     * Retrieves a section from the given source map by the specified key.
     * Ensures that the section is a non-empty Map<*, *> and then converts it
     * to MutableMap<String, Any?>. Returns `null` if any of these checks fail.
     *
     * @param source The source map to retrieve the section from.
     * @param key The key of the section to retrieve.
     * @param errorMessage The error message to log if the section is not found or is empty.
     * @return The mutable map section if it exists and is non-empty; `null` otherwise.
     */
    private fun getNonEmptyMapSection(
        source: Map<String, Any?>,
        key: String,
        errorMessage: String
    ): MutableMap<String, Any?>? {
        val section: Any? = source[key]
        if (section !is Map<*, *> || section.isEmpty()) {
            tracer.debug(errorMessage)
            return null
        }

        val mutableMap: MutableMap<String, Any?> = section.toMutableMapSafely() ?: run {
            tracer.debug("Failed to convert '$key' section to a mutable map.")
            return null
        }
        return mutableMap
    }

    /**
     * Safely converts a Map<*, *> into a MutableMap<String, Any?>.
     * Returns `null` if any key is not a String.
     */
    private fun Map<*, *>.toMutableMapSafely(): MutableMap<String, Any?>? {
        val mutableMap: MutableMap<String, Any?> = mutableMapOf<String, Any?>()
        for ((key, value) in this) {
            val keyStr: String = key?.toString() ?: return null
            mutableMap[keyStr] = value
        }
        return mutableMap
    }

    private companion object {
        private const val SCHEMA_KEY: String = "schemas"
        private const val COMPONENTS_KEY: String = "components"
    }
}
