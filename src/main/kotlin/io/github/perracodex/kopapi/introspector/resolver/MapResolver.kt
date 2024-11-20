/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.introspector.resolver

import io.github.perracodex.kopapi.introspector.TypeIntrospector
import io.github.perracodex.kopapi.introspector.annotation.TypeIntrospectorApi
import io.github.perracodex.kopapi.introspector.descriptor.ElementName
import io.github.perracodex.kopapi.introspector.descriptor.resolveTypeBinding
import io.github.perracodex.kopapi.introspector.schema.TypeSchema
import io.github.perracodex.kopapi.introspector.schema.factory.SchemaFactory
import io.github.perracodex.kopapi.system.Tracer
import kotlin.reflect.KClassifier
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection

/**
 * - Purpose:
 *      - Handles `Map` types.
 * - Action:
 *      - Validate Key Type: Verifies that the map's key type is a `String`, logging an error if not.
 *      - Resolve Value Type: Determines the value type of the map.
 *      - Traverse Value Type: Uses `TypeIntrospector` to traverse the value type.
 *      - Construct Schema: Creates a schema with `additionalProperties` representing the value schema.
 *      - Result: Constructs and returns the map schema.
 *
 * @see [TypeIntrospector]
 */
@TypeIntrospectorApi
internal class MapResolver(private val introspector: TypeIntrospector) {
    private val tracer = Tracer<MapResolver>()

    /**
     * Resolves a [Map] type to a [TypeSchema].
     *
     * @param kType The [KType] representing the map type.
     * @param typeArgumentBindings A map of type arguments' [KClassifier] to actual [KType] items for replacement.
     * @return The resolved [TypeSchema] for the map, with additionalProperties for the value type.
     */
    fun traverse(
        kType: KType,
        typeArgumentBindings: Map<KClassifier, KType>
    ): TypeSchema {
        tracer.debug("Resolving Map type: $kType.")

        val (keyType: KType?, valueType: KType?) = resolveMapComponents(
            kType = kType,
            typeArgumentBindings = typeArgumentBindings
        )

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
        // If the value type is null, default to an object schema with the name
        // of the kType to highlight the issue.
        val typeSchema: TypeSchema = valueType?.let {
            tracer.debug("Traversing Map value type: $valueType.")
            introspector.traverseType(kType = valueType, typeArgumentBindings = typeArgumentBindings)
        } ?: TypeSchema.of(
            name = ElementName(name = "MapOf${kType}"),
            kType = kType,
            schema = SchemaFactory.ofObjectDescriptor()
        )

        return TypeSchema.of(
            name = ElementName(name = "MapOf${typeSchema.name}"),
            kType = kType,
            schema = SchemaFactory.ofAdditionalProperties(value = typeSchema.schema)
        )
    }

    /**
     * Resolves the component types of a [Map] type, such as its key and value types, by
     * returning the resolved types of the generic arguments. The first element represents
     * the type at index 0 (commonly the key), and the second element represents the type
     * at index 1 (commonly the value).
     *
     * #### Example:
     * Given a `Map<String, Int>`, this returns a pair with `String` as the first type
     * and `Int` as the second.
     *
     * @param kType The [KType] representing the map type.
     * @param typeArgumentBindings A map of type arguments' [KClassifier] to [KType] replacements.
     * @return A [Pair] of resolved [KType?] elements, representing the first and second type arguments.
     */
    private fun resolveMapComponents(
        kType: KType,
        typeArgumentBindings: Map<KClassifier, KType>
    ): Pair<KType?, KType?> {
        val mapArguments: List<KTypeProjection> = kType.arguments
        val keyType: KType? = mapArguments.getOrNull(index = 0)?.resolveTypeBinding(bindings = typeArgumentBindings)
        val valueType: KType? = mapArguments.getOrNull(index = 1)?.resolveTypeBinding(bindings = typeArgumentBindings)
        return keyType to valueType
    }
}
