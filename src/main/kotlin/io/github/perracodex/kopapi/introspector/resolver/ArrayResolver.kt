/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.introspector.resolver

import io.github.perracodex.kopapi.introspector.TypeIntrospector
import io.github.perracodex.kopapi.introspector.annotation.TypeIntrospectorApi
import io.github.perracodex.kopapi.introspector.descriptor.ElementName
import io.github.perracodex.kopapi.introspector.descriptor.MetadataDescriptor
import io.github.perracodex.kopapi.introspector.descriptor.TypeDescriptor
import io.github.perracodex.kopapi.introspector.schema.TypeSchema
import io.github.perracodex.kopapi.introspector.schema.factory.PrimitiveFactory
import io.github.perracodex.kopapi.introspector.schema.factory.SchemaFactory
import io.github.perracodex.kopapi.schema.facet.ElementSchema
import io.github.perracodex.kopapi.system.Tracer
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
 * @see [TypeIntrospector]
 */
@TypeIntrospectorApi
internal class ArrayResolver(private val introspector: TypeIntrospector) {
    private val tracer = Tracer<ArrayResolver>()

    /**
     * Handles [Collection] (eg: List, Set), in addition to typed [Array]s.
     * by introspecting the contained element type and traversing it if needed.
     *
     * @param kType The [KType] representing the collection type.
     * @param classifier The [KClassifier] representing the `Collection` or typed array `Array<T>`.
     * @param typeArgumentBindings A map of type arguments' [KClassifier] to actual [KType] items for replacement.
     * @return The resolved [TypeSchema] for the collection type.
     */
    fun traverse(
        kType: KType,
        classifier: KClassifier,
        typeArgumentBindings: Map<KClassifier, KType>
    ): TypeSchema {
        tracer.debug("Traversing array type: $kType.")

        val className: ElementName = MetadataDescriptor.getClassName(kClass = (classifier as KClass<*>))

        // Check if dealing with a primitive array first, such as IntArray, ByteArray, etc.,
        // and return the corresponding schema if it is.
        if (TypeDescriptor.isPrimitiveArray(classifier = classifier)) {
            tracer.debug("Processing a primitive array: $classifier.")
            val schema: ElementSchema? = PrimitiveFactory.newSchema(kClass = classifier)
            return TypeSchema.of(
                name = className,
                kType = kType,
                schema = schema ?: SchemaFactory.ofObjectDescriptor()
            )
        }

        // If not a primitive array then it is expected to be a typed array (Array<T>).
        if (!TypeDescriptor.isTypedArray(kType = kType)) {
            tracer.error("Type is not a typed array 'Array<T>': $kType")
            return TypeSchema.of(
                name = className,
                kType = kType,
                schema = SchemaFactory.ofObjectDescriptor()
            )
        }

        // If dealing with a typed array (Array<T>), delegate to the CollectionResolver to handle it.
        tracer.debug("Delegating array processing to CollectionResolver for typed array: $kType.")
        return introspector.traverseCollection(
            kType = kType,
            classifier = classifier,
            typeArgumentBindings = typeArgumentBindings
        )
    }
}
