/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.resolver

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
 * Resolves primitive and `Generics` [Array] types, into their corresponding [TypeSchema].
 *
 * `Generics` [Array] types processing is delegated to the [CollectionResolver],
 * as they share similar characteristics with `Collection` types.
 *
 * Responsibilities:
 * - Handling primitive arrays (e.g.: IntArray, ByteArray, etc.)
 * - Delegating `Generics` [Array] types to the [CollectionResolver].
 */
@TypeInspectorAPI
internal object ArrayResolver {
    private val tracer = Tracer<ArrayResolver>()

    /**
     * Handles [Collection] (eg: List, Set), in addition to generics [Array] types
     * by introspecting the contained element type and traversing it if needed.
     *
     * @param kType The [KType] representing the collection type.
     * @param classifier The [KClassifier] representing the [Collection] or [Array] type.
     * @param typeParameterMap A map of type parameters' [KClassifier] to actual [KType] items for replacement.
     * @return The resolved [TypeSchema] for the collection type.
     */
    fun process(
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

        // If not a primitive array then it is expected to be a Generics array.
        if (!TypeDescriptor.isGenericsArray(kType = kType)) {
            tracer.error("Type is not a generics Array<T>: $kType")
            return TypeSchema.of(
                name = className,
                kType = kType,
                schema = Spec.objectType()
            )
        }

        // If dealing with a generics array, delegate to the CollectionResolver to handle it.
        return CollectionResolver.process(
            kType = kType,
            classifier = classifier,
            typeParameterMap = typeParameterMap
        )
    }
}
