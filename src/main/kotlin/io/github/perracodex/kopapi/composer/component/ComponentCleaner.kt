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
        // Get the components.
        val componentsSection: Any? = openApiMap[COMPONENTS_KEY]
        if (componentsSection !is Map<*, *> || componentsSection.isEmpty()) {
            return false
        }
        val components: MutableMap<String, Any?> = componentsSection.entries.associate { (key, value) ->
            key.toString() to value
        }.toMutableMap()

        // Get the schemas.
        val schemasSection: Any? = components[SCHEMA_KEY]
        if (schemasSection !is Map<*, *> || schemasSection.isEmpty()) {
            return false
        }
        val schemas: MutableMap<String, Any?> = schemasSection.entries.associate { (key, value) ->
            key.toString() to value
        }.toMutableMap()

        // Remove unused schemas.
        val usedSchemaNames: Set<String> = usedRefs.map { it.substringAfterLast("/") }.toSet()
        val orphans: Set<String> = schemas.keys - usedSchemaNames

        if (orphans.isNotEmpty()) {
            orphans.forEach { orphan ->
                schemas.remove(orphan)

                // Remove the orphan from the OpenAPI schema structure.
                openApiSchema.components?.componentSchemas?.remove(orphan)

                // Remove the orphan from the schema conflicts.
                schemaConflicts.removeIf { it.name == orphan }
            }

            // Put the modified schemas back.
            components[SCHEMA_KEY] = schemas
            openApiMap[COMPONENTS_KEY] = components

            tracer.debug("Removed orphan schemas: $orphans")
        }

        return orphans.isNotEmpty()
    }

    private companion object {
        private const val SCHEMA_KEY: String = "schemas"
        private const val COMPONENTS_KEY: String = "components"
    }
}
