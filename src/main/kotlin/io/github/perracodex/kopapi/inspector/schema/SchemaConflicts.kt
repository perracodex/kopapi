/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.schema

import io.github.perracodex.kopapi.inspector.TypeSchemaProvider
import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI

/**
 * Manager caching conflicting [TypeSchema] objects.
 */
internal class SchemaConflicts(private val schemaProvider: TypeSchemaProvider) {
    /** Holds Conflict generated during the inspection process. */
    private val conflicts: MutableSet<Conflict> = mutableSetOf()

    /**
     * Retrieves the conflicts generated during the inspection process.
     *
     * @return A list of [Conflict] objects.
     */
    fun get(): Set<Conflict> = conflicts

    /**
     * Updates the cache by detecting conflicting [TypeSchema] objects
     * with the same name but different types.
     *
     * @param newSchema The [TypeSchema] to check for conflicts.
     */
    @TypeInspectorAPI
    fun analyze(newSchema: TypeSchema) {
        schemaProvider.getTypeSchemas().filter { schema ->
            schema.name.equals(other = newSchema.name, ignoreCase = true)
                    && !schema.type.equals(other = newSchema.type, ignoreCase = true)
        }.forEach { existing ->
            conflicts.find {
                it.name.equals(other = newSchema.name, ignoreCase = true)
            }?.conflicts?.add(existing)
                ?: conflicts.add(
                    Conflict(
                        name = newSchema.name,
                        conflicts = mutableSetOf(
                            existing,
                            newSchema
                        )
                    )
                )
        }
    }

    /**
     * Represents a conflict for inspected [TypeSchema] that shares the same name
     * but have different type from another cached [TypeSchema].
     *
     * @param name The conflicting common name.
     * @param conflicts A set of [TypeSchema] items with the same name but different types.
     */
    internal data class Conflict(
        val name: String,
        val conflicts: MutableSet<TypeSchema>
    )
}
