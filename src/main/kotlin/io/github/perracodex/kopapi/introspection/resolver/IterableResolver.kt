/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.introspection.resolver

import io.github.perracodex.kopapi.introspection.TypeIntrospector
import io.github.perracodex.kopapi.introspection.annotation.TypeIntrospectorApi
import io.github.perracodex.kopapi.introspection.descriptor.ElementName
import io.github.perracodex.kopapi.introspection.descriptor.MetadataDescriptor
import io.github.perracodex.kopapi.introspection.descriptor.TypeDescriptor
import io.github.perracodex.kopapi.introspection.descriptor.resolveTypeBinding
import io.github.perracodex.kopapi.introspection.schema.TypeSchema
import io.github.perracodex.kopapi.introspection.schema.factory.SchemaFactory
import io.github.perracodex.kopapi.system.Tracer
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KType

/**
 * - Purpose:
 *      - Handles `Iterables` types like `List`, `Set`, etc.
 * - Action:
 *      - Resolve Element Type: Determines the element type of the iterable.
 *      - Traverse Element Type: Uses `TypeIntrospector` to traverse the element type, which may involve recursion.
 *      - Construct Schema: Builds the iterable schema, incorporating the element schema.
 *      - Result: Constructs and returns the iterable schema.
 *
 * @see [ArrayResolver]
 * @see [TypeIntrospector]
 */
@TypeIntrospectorApi
internal class IterableResolver(private val introspector: TypeIntrospector) {
    private val tracer: Tracer = Tracer<IterableResolver>()

    /**
     * Process [Iterable] types (e.g.: [List], [Set], etc.),
     * by introspecting the contained element type and traversing it if needed.
     *
     * @param kType The [KType] representing the iterable type.
     * @param classifier The [KClassifier] representing the [Iterable]  type.
     * @param typeArgumentBindings A map of type arguments' [KClassifier] to their actual [KType] replacements.
     * @return The resolved [TypeSchema] for the iterable type.
     */
    fun traverse(
        kType: KType,
        classifier: KClassifier,
        typeArgumentBindings: Map<KClassifier, KType>
    ): TypeSchema {
        tracer.debug("Traversing iterable type: $kType.")

        val className: ElementName = MetadataDescriptor.getClassName(kClass = (classifier as KClass<*>))

        val argumentType: KType = kType.arguments.firstOrNull()?.resolveTypeBinding(
            bindings = typeArgumentBindings
        ) ?: run {
            // Iterables always have an argument type, so if not found,
            // log an error and treat it as an object type.
            tracer.error("No argument found for Iterables<T> type: $kType")
            return TypeSchema.of(
                name = className,
                kType = kType,
                schema = SchemaFactory.ofObjectDescriptor()
            )
        }

        // Traverse the iterable argument element to resolve its respective TypeSchema.
        tracer.debug("Traversing iterable element type: $argumentType.")
        val typeSchema: TypeSchema = introspector.traverseType(
            kType = argumentType,
            typeArgumentBindings = typeArgumentBindings
        )

        // Distinguishing iterable names is solely for clarity and debugging,
        // and will never be included in the final schema output,
        // as the iterable itself will never become a reference type;
        // only the contained elements will become references.
        // The iterable itself will be defined as a type array.
        val descriptorPrefix: String = when {
            TypeDescriptor.isCollection(classifier) -> "CollectionOf"
            TypeDescriptor.isIterable(classifier) -> "IterableOf"
            else -> "ArrayOf"
        }
        val name = "$descriptorPrefix${typeSchema.name}"

        return TypeSchema.of(
            name = ElementName(name = name),
            kType = kType,
            schema = SchemaFactory.ofArray(items = typeSchema.schema)
        )
    }
}
