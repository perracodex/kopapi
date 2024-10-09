/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.resolver

import io.github.perracodex.kopapi.inspector.TypeResolver
import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.schema.TypeSchema
import io.github.perracodex.kopapi.inspector.schema.factory.SchemaFactory
import io.github.perracodex.kopapi.inspector.utils.resolveGenerics
import io.github.perracodex.kopapi.utils.Tracer
import kotlin.reflect.KClassifier
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection

/**
 * - Purpose:
 *      - Handles `Map` types.
 * - Action:
 *      - Validate Key Type: Verifies that the map's key type is a `String`, logging an error if not.
 *      - Resolve Value Type: Determines the value type of the map.
 *      - Traverse Value Type: Uses `TypeResolver` to traverse the value type.
 *      - Construct Schema: Creates a schema with `additionalProperties` representing the value schema.
 *      - Result: Constructs and returns the map schema.
 *
 * @see [TypeResolver]
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
        } ?: TypeSchema.of(name = "MapOf${kType}", kType = kType, schema = SchemaFactory.ofObject())

        return TypeSchema.of(
            name = "MapOf${typeSchema.name}",
            kType = kType,
            schema = SchemaFactory.ofAdditionalProperties(value = typeSchema.schema)
        )
    }
}
