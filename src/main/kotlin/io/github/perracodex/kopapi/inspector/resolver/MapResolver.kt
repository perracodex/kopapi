/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.resolver

import io.github.perracodex.kopapi.inspector.TypeResolver
import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.spec.Spec
import io.github.perracodex.kopapi.inspector.type.TypeSchema
import io.github.perracodex.kopapi.inspector.type.resolveGenerics
import io.github.perracodex.kopapi.utils.Tracer
import kotlin.reflect.KClassifier
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection

/**
 * Resolves [Map] objects into their corresponding [TypeSchema].
 *
 * Maps do not generate their schema references,
 * but a schema will be created for their value type if such is a complex object,
 * or a schema reference will be assigned if its type has already been processed.
 *
 * Responsibilities:
 * - Handling [Map] objects by resolving their key and value types.
 * - Traverse the value type if it is a complex object.
 * - Logging errors for unsupported key types.
 * - Creating a [TypeSchema] which includes an `additionalProperties` spec for the value type.
 * - Caching the created [TypeSchema] to avoid redundant processing.
 */
@TypeInspectorAPI
internal class MapResolver(private val typeResolver: TypeResolver) {
    private val tracer = Tracer<MapResolver>()

    /**
     * Resolves a [Map] type to a [TypeSchema].
     *
     * @param kType The [KType] representing the map type.
     * @param typeParameterMap A map of type parameters' [KClassifier] to actual [KType] items for replacement.
     * @return The resolved [TypeSchema] for the map, with additionalProperties for the value type.
     */
    fun traverse(
        kType: KType,
        typeParameterMap: Map<KClassifier, KType>
    ): TypeSchema {
        val mapArguments: List<KTypeProjection> = kType.arguments
        val keyType: KType? = mapArguments.getOrNull(index = 0)?.type?.resolveGenerics(typeParameterMap = typeParameterMap)
        val valueType: KType? = mapArguments.getOrNull(index = 1)?.type?.resolveGenerics(typeParameterMap = typeParameterMap)

        // OpenAPI requires keys to be strings.
        // The key is actually not needed for processing since for maps we only traverse the value type,
        // but we log an error for debugging purposes.
        if (keyType == null) {
            tracer.error("Unable to resolve the Map key type from $kType")
        }
        if (keyType?.classifier != String::class) {
            tracer.error("Map keys must be strings in OpenAPI. Found key type: $keyType.")
        }

        // Process the value type.
        val typeSchema: TypeSchema = valueType?.let {
            typeResolver.traverseType(kType = valueType, typeParameterMap = typeParameterMap)
        } ?: TypeSchema.of(name = "MapOf${kType}", kType = kType, schema = Spec.objectType())

        return TypeSchema.of(
            name = "MapOf${typeSchema.name}",
            kType = kType,
            schema = Spec.additionalProperties(value = typeSchema.schema)
        )
    }
}
