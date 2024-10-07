/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector

import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.type.TypeSchema
import io.github.perracodex.kopapi.inspector.type.TypeSchemaConflicts
import kotlin.reflect.KType

/**
 * TypeInspector serves as the primary interface for introspecting various Kotlin types,
 * transforming [KType] instances into corresponding [TypeSchema] objects. These schemas
 * encapsulate all necessary information to construct OpenAPI-compliant schemas.
 *
 * This class delegates the core type traversal and schema resolution logic to the [TypeResolver] class.
 * The [TypeSchemaConflicts] instance manages and logs any schema naming conflicts  detected during
 * the inspection process.
 *
 * @see [TypeResolver]
 * @see [TypeSchemaConflicts]
 * @see [TypeSchema]
 */
@OptIn(TypeInspectorAPI::class)
internal class TypeInspector {
    /** Instance of [TypeSchemaConflicts] to manage conflicting [TypeSchema] objects. */
    private val conflicts = TypeSchemaConflicts(typeInspector = this)

    /** Instance of [TypeResolver] to handle type traversal and schema resolution. */
    private val typeResolver = TypeResolver()

    /**
     * Inspect the given [kType] to its corresponding [TypeSchema] representation.
     *
     * @param kType The target [KType] to inspect.
     * @return The resolved [TypeSchema] for the given [kType].
     */
    fun inspect(kType: KType): TypeSchema {
        val result: TypeSchema = typeResolver.traverseType(kType = kType, typeParameterMap = emptyMap())
        conflicts.analyze(newSchema = result)
        return result
    }

    /**
     * Retrieves the currently cached [TypeSchema] objects.
     *
     * @return A set of [TypeSchema] objects.
     */
    fun getTypeSchemas(): Set<TypeSchema> = typeResolver.typeSchemaCache

    /**
     * Resets the instance by clearing all processed data, including conflicts.
     */
    fun reset() {
        typeResolver.typeSchemaCache.clear()
        conflicts.clear()
    }
}
