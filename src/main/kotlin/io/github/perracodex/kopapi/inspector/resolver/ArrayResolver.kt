/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.resolver

import io.github.perracodex.kopapi.inspector.TypeResolver
import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.spec.Spec
import io.github.perracodex.kopapi.inspector.type.ElementMetadata
import io.github.perracodex.kopapi.inspector.type.TypeDescriptor
import io.github.perracodex.kopapi.inspector.type.TypeSchema
import io.github.perracodex.kopapi.utils.Tracer
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KType

/**
 * - Purpose:
 *      - Handles array types, both primitive and generics.
 * - Action:
 *      - Determine Array Type:
 *          - Primitive Array (e.g., `IntArray`): processes it immediately and constructs the schema.
 *          - Typed arrays (`Array<T>`): delegates processing to `CollectionResolver`.
 *      - Result: Returns the constructed schema, or delegates as appropriate.
 *
 * @see [CollectionResolver]
 * @see [TypeResolver]
 */
@TypeInspectorAPI
internal class ArrayResolver(private val typeResolver: TypeResolver) {
    private val tracer = Tracer<ArrayResolver>()

    /**
     * Handles [Collection] (eg: List, Set), in addition to typed [Array]s.
     * by introspecting the contained element type and traversing it if needed.
     *
     * @param kType The [KType] representing the collection type.
     * @param classifier The [KClassifier] representing the `Collection` or typed array `Array<T>`.
     * @param typeParameterMap A map of type parameters' [KClassifier] to actual [KType] items for replacement.
     * @return The resolved [TypeSchema] for the collection type.
     */
    fun traverse(
        kType: KType,
        classifier: KClassifier,
        typeParameterMap: Map<KClassifier, KType>
    ): TypeSchema {
        val className: String = ElementMetadata.getClassName(kClass = (classifier as KClass<*>))

        // Check if dealing with a primitive array first, such as IntArray, ByteArray, etc.,
        // and return the corresponding schema if it is.
        if (TypeDescriptor.isPrimitiveArray(classifier = classifier)) {
            val schema: MutableMap<String, Any>? = TypeDescriptor.mapPrimitiveType(kClass = classifier)
            return TypeSchema.of(
                name = className,
                kType = kType,
                schema = schema ?: Spec.objectType()
            )
        }

        // If not a primitive array then it is expected to be a typed array (Array<T>).
        if (!TypeDescriptor.isTypedArray(kType = kType)) {
            tracer.error("Type is not a typed array 'Array<T>': $kType")
            return TypeSchema.of(
                name = className,
                kType = kType,
                schema = Spec.objectType()
            )
        }

        // If dealing with a typed array (Array<T>), delegate to the CollectionResolver to handle it.
        return typeResolver.traverseCollection(
            kType = kType,
            classifier = classifier,
            typeParameterMap = typeParameterMap
        )
    }
}
