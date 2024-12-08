/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.introspection.schema

import io.github.perracodex.kopapi.introspection.TypeSchemaProvider
import io.github.perracodex.kopapi.introspection.annotation.TypeIntrospectorApi

/**
 * Manager caching conflicting [TypeSchema] objects.
 */
internal class SchemaConflicts(private val schemaProvider: TypeSchemaProvider) {
    /** Holds Conflict generated during the introspection process. */
    private val conflicts: MutableSet<Conflict> = mutableSetOf()

    /**
     * Retrieves the conflicts generated during the introspection process.
     *
     * @return A list of [Conflict] objects.
     */
    fun get(): Set<Conflict> = conflicts

    /**
     * Finds conflicting [TypeSchema] objects and stores them in the cache.
     * These are schemas that share the same name but have different types.
     */
    @TypeIntrospectorApi
    fun analyze() {
        val typeSchemas: Set<TypeSchema> = schemaProvider.getTypeSchemas()

        // Find all schemas that have the same name and group them by name (case-sensitive).
        typeSchemas
            .groupBy { it.name }
            .filter { it.value.size > 1 }
            .forEach { (name, duplicates) ->
                val conflictingTypes: Set<String> = duplicates.map { it.type }.toSet()
                val conflict = Conflict(name = name, conflictingTypes = conflictingTypes)
                conflicts.add(conflict)
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
        val conflictingTypes: Set<String>
    )
}
