/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.resolver

import io.github.perracodex.kopapi.inspector.TypeSchemaBuilder
import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.descriptor.MetadataDescriptor
import io.github.perracodex.kopapi.inspector.descriptor.TypeDescriptor
import io.github.perracodex.kopapi.inspector.schema.TypeSchema
import io.github.perracodex.kopapi.inspector.schema.factory.SchemaFactory
import io.github.perracodex.kopapi.inspector.utils.resolveGenerics
import io.github.perracodex.kopapi.utils.Tracer
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KType

/**
 * - Purpose:
 *      - Handles `Collection` types like `List`, `Set`, etc.
 * - Action:
 *      - Resolve Element Type: Determines the element type of the collection.
 *      - Traverse Element Type: Uses `TypeSchemaBuilder` to traverse the element type, which may involve recursion.
 *      - Construct Schema: Builds the collection schema, incorporating the element schema.
 *      - Result: Constructs and returns the collection schema.
 *
 * @see [ArrayResolver]
 * @see [TypeSchemaBuilder]
 */
@TypeInspectorAPI
internal class CollectionResolver(private val typeSchemaBuilder: TypeSchemaBuilder) {
    private val tracer = Tracer<CollectionResolver>()

    /**
     * Process [Collection] types (e.g.: [List], [Set], etc.),
     * by introspecting the contained element type and traversing it if needed.
     *
     * @param kType The [KType] representing the collection type.
     * @param classifier The [KClassifier] representing the [Collection]  type.
     * @param typeParameterMap A map of type parameters' [KClassifier] to actual [KType] items for replacement.
     * @return The resolved [TypeSchema] for the collection type.
     */
    fun traverse(
        kType: KType,
        classifier: KClassifier,
        typeParameterMap: Map<KClassifier, KType>
    ): TypeSchema {
        val className: String = MetadataDescriptor.getClassName(kClass = (classifier as KClass<*>))

        val argumentType: KType = kType.arguments.firstOrNull()?.type?.resolveGenerics(
            typeParameterMap = typeParameterMap
        ) ?: run {
            // Collections always have an argument type, so if not found,
            // log an error and treat it as an object type.
            tracer.error("No argument found for Collection<T> type: $kType")
            return TypeSchema.of(
                name = className,
                kType = kType,
                schema = SchemaFactory.ofObject()
            )
        }

        // Traverse the collection argument element to resolve its respective TypeSchema.
        val typeSchema: TypeSchema = typeSchemaBuilder.traverseType(
            kType = argumentType,
            typeParameterMap = typeParameterMap
        )

        // Distinguishing names between Array<T> and Collections is solely for clarity and debugging,
        // as neither results in a referable object in the final schema; only the contained element does.
        val name: String = if (TypeDescriptor.isCollection(classifier = classifier)) {
            "CollectionOf${typeSchema.name}"
        } else {
            "ArrayOf${typeSchema.name}"
        }

        return TypeSchema.of(
            name = name,
            kType = kType,
            schema = SchemaFactory.ofCollection(items = typeSchema.schema)
        )
    }
}
