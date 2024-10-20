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
        // Retrieve all existing TypeSchemas and filter out those that have the same name
        // as newSchema (case-insensitive) but a different type (also case-insensitive).
        val conflictingSchemas: List<TypeSchema> = schemaProvider.getTypeSchemas().filter { existingSchema ->
            existingSchema.name.equals(newSchema.name, ignoreCase = true) &&
                    !existingSchema.type.equals(newSchema.type, ignoreCase = true)
        }

        conflictingSchemas.forEach { existingSchema ->
            // Attempt to find an existing Conflict entry that matches the newSchema's name.
            val conflict: Conflict? = conflicts.find { conflict ->
                conflict.name.equals(newSchema.name, ignoreCase = true)
            }

            if (conflict != null) {
                // If a Conflict entry exists, add the conflicting type of the existing schema
                // and the new schema's type to the conflictingTypes set.
                conflict.conflictingTypes.add(existingSchema.type)
                conflict.conflictingTypes.add(newSchema.type)
            } else {
                // If no Conflict entry exists for the newSchema's name, create a new Conflict
                // instance with the differing types.
                conflicts.add(
                    Conflict(
                        name = newSchema.name,
                        conflictingTypes = mutableSetOf(
                            existingSchema.type,
                            newSchema.type
                        )
                    )
                )
            }
        }
    }

    /**
     * Clears the cache of conflicts.
     */
    fun clear() {
        conflicts.clear()
    }

    /**
     * Represents a conflict among [TypeSchema] objects that share the same name
     * but have different types.
     *
     * @param name The common name shared by the conflicting [TypeSchema] objects.
     * @param conflictingTypes A set of type identifiers that are in conflict.
     */
    internal data class Conflict(
        val name: String,
        val conflictingTypes: MutableSet<String>
    )
}
