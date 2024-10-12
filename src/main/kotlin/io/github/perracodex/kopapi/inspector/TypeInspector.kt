/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector

import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.schema.SchemaConflicts
import io.github.perracodex.kopapi.inspector.schema.TypeSchema
import kotlin.reflect.KType

/**
 * TypeInspector serves as the primary interface for introspecting various Kotlin types,
 * transforming [KType] instances into corresponding [TypeSchema] objects. These schemas
 * encapsulate all necessary information to construct OpenAPI-compliant schemas.
 *
 * This class delegates the core type traversal and schema resolution logic to the [TypeSchemaBuilder] class.
 *
 * @see [TypeSchemaBuilder]
 * @see [SchemaConflicts]
 * @see [TypeSchema]
 */
@OptIn(TypeInspectorAPI::class)
internal class TypeInspector {
    /** Instance of [SchemaConflicts] to keep track of conflicting [TypeSchema] objects. */
    private val conflicts = SchemaConflicts(typeInspector = this)

    /** Instance of [TypeSchemaBuilder] to handle type traversal and schema resolution. */
    private val typeSchemaBuilder = TypeSchemaBuilder()

    /**
     * Inspect the given [kType] to its corresponding [TypeSchema] representation.
     *
     * @param kType The target [KType] to inspect.
     * @return The resolved [TypeSchema] for the given [kType].
     */
    fun inspect(kType: KType): TypeSchema {
        if (kType.classifier == Unit::class) {
            throw IllegalArgumentException("Type 'Unit' cannot be inspected.")
        }
        val result: TypeSchema = typeSchemaBuilder.traverseType(kType = kType, typeParameterMap = emptyMap())
        conflicts.analyze(newSchema = result)
        return result
    }

    /**
     * Retrieves the conflicts that have been detected during the inspection process.
     *
     * These can be for example when two types with the same name but different types are detected,
     * which would result in incorrect objects `reference` generation.
     *
     * @return A set of [SchemaConflicts.Conflict] objects.
     */
    fun getConflicts(): Set<SchemaConflicts.Conflict> = conflicts.get()

    /**
     * Retrieves the currently cached [TypeSchema] objects.
     *
     * @return A set of [TypeSchema] objects.
     */
    fun getTypeSchemas(): Set<TypeSchema> = typeSchemaBuilder.typeSchemaCache

    /**
     * Resets the instance by clearing all processed data, including conflicts.
     */
    fun reset() {
        typeSchemaBuilder.typeSchemaCache.clear()
        conflicts.clear()
    }
}
