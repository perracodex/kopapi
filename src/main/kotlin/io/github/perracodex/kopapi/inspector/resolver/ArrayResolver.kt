/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.resolver

import io.github.perracodex.kopapi.inspector.TypeInspector
import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.descriptor.ElementName
import io.github.perracodex.kopapi.inspector.descriptor.MetadataDescriptor
import io.github.perracodex.kopapi.inspector.descriptor.TypeDescriptor
import io.github.perracodex.kopapi.inspector.schema.TypeSchema
import io.github.perracodex.kopapi.inspector.schema.factory.PrimitiveFactory
import io.github.perracodex.kopapi.inspector.schema.factory.SchemaFactory
import io.github.perracodex.kopapi.schema.ElementSchema
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
 * @see [TypeInspector]
 */
@TypeInspectorAPI
internal class ArrayResolver(private val typeInspector: TypeInspector) {
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
        val className: ElementName = MetadataDescriptor.getClassName(kClass = (classifier as KClass<*>))

        // Check if dealing with a primitive array first, such as IntArray, ByteArray, etc.,
        // and return the corresponding schema if it is.
        if (TypeDescriptor.isPrimitiveArray(classifier = classifier)) {
            val schema: ElementSchema? = PrimitiveFactory.newSchema(kClass = classifier)
            return TypeSchema.of(
                name = className,
                kType = kType,
                schema = schema ?: SchemaFactory.ofObject()
            )
        }

        // If not a primitive array then it is expected to be a typed array (Array<T>).
        if (!TypeDescriptor.isTypedArray(kType = kType)) {
            tracer.error("Type is not a typed array 'Array<T>': $kType")
            return TypeSchema.of(
                name = className,
                kType = kType,
                schema = SchemaFactory.ofObject()
            )
        }

        // If dealing with a typed array (Array<T>), delegate to the CollectionResolver to handle it.
        return typeInspector.traverseCollection(
            kType = kType,
            classifier = classifier,
            typeArgumentBindings = typeArgumentBindings
        )
    }
}
