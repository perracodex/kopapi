/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector

import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.schema.SchemaConflicts
import io.github.perracodex.kopapi.inspector.schema.TypeSchema
import kotlin.reflect.KType

/**
 * The primary interface for initiating the introspection of Kotlin types,
 * transforming [KType] instances into corresponding [TypeSchema] objects,
 * which encapsulate all necessary information to construct OpenAPI-compliant schemas.
 *
 * This class delegates the core type traversal and schema resolution logic to the [TypeInspector] class.
 *
 * @see [TypeInspector]
 * @see [SchemaConflicts]
 * @see [TypeSchema]
 */
@OptIn(TypeInspectorAPI::class)
internal class TypeSchemaProvider {
    /** Instance of [SchemaConflicts] to keep track of conflicting [TypeSchema] objects. */
    private val conflicts = SchemaConflicts(schemaProvider = this)

    /** Instance of [TypeInspector] to handle type traversal and schema resolution. */
    private val typeInspector = TypeInspector()

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
        val result: TypeSchema = typeInspector.traverseType(kType = kType, typeArgumentBindings = emptyMap())
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
     * Retrieve all the [TypeSchema] objects that have been inspected.
     *
     * @return A set of [TypeSchema] objects.
     */
    fun getTypeSchemas(): Set<TypeSchema> = typeInspector.getTypeSchemas()
}
