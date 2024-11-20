/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.introspector.resolver

import io.github.perracodex.kopapi.introspector.TypeIntrospector
import io.github.perracodex.kopapi.introspector.annotation.TypeIntrospectorApi
import io.github.perracodex.kopapi.introspector.descriptor.ElementName
import io.github.perracodex.kopapi.introspector.descriptor.MetadataDescriptor
import io.github.perracodex.kopapi.introspector.descriptor.TypeDescriptor
import io.github.perracodex.kopapi.introspector.descriptor.resolveTypeBinding
import io.github.perracodex.kopapi.introspector.schema.TypeSchema
import io.github.perracodex.kopapi.introspector.schema.factory.SchemaFactory
import io.github.perracodex.kopapi.system.Tracer
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KType

/**
 * - Purpose:
 *      - Handles `Collection` types like `List`, `Set`, etc.
 * - Action:
 *      - Resolve Element Type: Determines the element type of the collection.
 *      - Traverse Element Type: Uses `TypeIntrospector` to traverse the element type, which may involve recursion.
 *      - Construct Schema: Builds the collection schema, incorporating the element schema.
 *      - Result: Constructs and returns the collection schema.
 *
 * @see [ArrayResolver]
 * @see [TypeIntrospector]
 */
@TypeIntrospectorApi
internal class CollectionResolver(private val introspector: TypeIntrospector) {
    private val tracer = Tracer<CollectionResolver>()

    /**
     * Process [Collection] types (e.g.: [List], [Set], etc.),
     * by introspecting the contained element type and traversing it if needed.
     *
     * @param kType The [KType] representing the collection type.
     * @param classifier The [KClassifier] representing the [Collection]  type.
     * @param typeArgumentBindings A map of type arguments' [KClassifier] to their actual [KType] replacements.
     * @return The resolved [TypeSchema] for the collection type.
     */
    fun traverse(
        kType: KType,
        classifier: KClassifier,
        typeArgumentBindings: Map<KClassifier, KType>
    ): TypeSchema {
        tracer.debug("Traversing collection type: $kType.")

        val className: ElementName = MetadataDescriptor.getClassName(kClass = (classifier as KClass<*>))

        val argumentType: KType = kType.arguments.firstOrNull()?.resolveTypeBinding(
            bindings = typeArgumentBindings
        ) ?: run {
            // Collections always have an argument type, so if not found,
            // log an error and treat it as an object type.
            tracer.error("No argument found for Collection<T> type: $kType")
            return TypeSchema.of(
                name = className,
                kType = kType,
                schema = SchemaFactory.ofObjectDescriptor()
            )
        }

        // Traverse the collection argument element to resolve its respective TypeSchema.
        tracer.debug("Traversing collection element type: $argumentType.")
        val typeSchema: TypeSchema = introspector.traverseType(
            kType = argumentType,
            typeArgumentBindings = typeArgumentBindings
        )

        // Distinguishing names between Array<T> and Collections is solely for clarity and debugging,
        // as neither results in a referable object in the final schema; only the contained element does.
        val name: String = when {
            TypeDescriptor.isCollection(classifier = classifier) -> "CollectionOf${typeSchema.name}"
            else -> "ArrayOf${typeSchema.name}"
        }

        return TypeSchema.of(
            name = ElementName(name = name),
            kType = kType,
            schema = SchemaFactory.ofCollection(items = typeSchema.schema)
        )
    }
}
