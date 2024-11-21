/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.introspector

import io.github.perracodex.kopapi.introspector.annotation.TypeIntrospectorApi
import io.github.perracodex.kopapi.introspector.schema.SchemaConflicts
import io.github.perracodex.kopapi.introspector.schema.TypeSchema
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.system.Tracer
import kotlin.reflect.KType

/**
 * The primary interface for initiating the introspection of Kotlin types,
 * transforming [KType] instances into corresponding [TypeSchema] objects,
 * which encapsulate all necessary information to construct OpenAPI-compliant schemas.
 *
 * This class delegates the core type traversal and schema resolution logic to the [TypeIntrospector] class.
 *
 * @see [TypeIntrospector]
 * @see [SchemaConflicts]
 * @see [TypeSchema]
 */
@OptIn(TypeIntrospectorApi::class)
internal class TypeSchemaProvider {
    private val tracer: Tracer = Tracer<TypeSchemaProvider>()

    /** Instance of [SchemaConflicts] to keep track of conflicting [TypeSchema] objects. */
    private val conflicts = SchemaConflicts(schemaProvider = this)

    /** Instance of [TypeIntrospector] to handle type traversal and schema resolution. */
    private val introspector = TypeIntrospector()

    /**
     * Introspect the given [kType] to its corresponding [TypeSchema] representation.
     *
     * @param kType The target [KType] to introspect.
     * @return The resolved [TypeSchema] for the given [kType].
     */
    fun introspect(kType: KType): TypeSchema {
        if (kType.classifier == Unit::class) {
            throw KopapiException("Type 'Unit' cannot be introspected.")
        }
        tracer.debug("Initiating introspection of type: $kType.")
        val result: TypeSchema = introspector.traverseType(kType = kType, typeArgumentBindings = emptyMap())
        conflicts.analyze(newSchema = result)
        return result
    }

    /**
     * Retrieves the conflicts that have been detected during the introspection process.
     *
     * These can be for example when two types with the same name but different types are detected,
     * which would result in incorrect objects `reference` generation.
     *
     * @return A set of [SchemaConflicts.Conflict] objects.
     */
    fun getConflicts(): Set<SchemaConflicts.Conflict> = conflicts.get()

    /**
     * Retrieve all the [TypeSchema] objects that have been introspected.
     *
     * @return A set of [TypeSchema] objects.
     */
    fun getTypeSchemas(): Set<TypeSchema> = introspector.getTypeSchemas()

    /**
     * Reset the [TypeSchemaProvider] to its initial state.
     *
     * All collected [TypeSchema] objects and [SchemaConflicts] will be cleared.
     */
    fun reset() {
        tracer.info("Resetting TypeSchemaProvider.")
        conflicts.clear()
        introspector.clear()
    }
}
