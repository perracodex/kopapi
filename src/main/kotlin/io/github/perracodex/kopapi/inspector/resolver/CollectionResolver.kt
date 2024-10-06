/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.resolver

import io.github.perracodex.kopapi.inspector.TypeInspector
import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.spec.Spec
import io.github.perracodex.kopapi.inspector.type.ElementMetadata
import io.github.perracodex.kopapi.inspector.type.TypeSchema
import io.github.perracodex.kopapi.utils.Tracer
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KType

/**
 * Resolves [Collection] types (e.g.: [List], [Set], etc.), into their corresponding [TypeSchema].
 *
 * Responsibilities:
 * - Traversing the contained element type and resolving its respective [TypeSchema].
 */
@TypeInspectorAPI
internal object CollectionResolver {
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
    fun process(
        kType: KType,
        classifier: KClassifier,
        typeParameterMap: Map<KClassifier, KType>
    ): TypeSchema {
        val className: String = ElementMetadata.getClassName(kClass = (classifier as KClass<*>))

        val argumentType: KType = kType.arguments.firstOrNull()?.type?.let {
            TypeInspector.replaceTypeIfNeeded(
                type = it,
                typeParameterMap = typeParameterMap
            )
        } ?: run {
            // Collections always have an argument type, so if not found,
            // log an error and treat it as an object type.
            tracer.error("No argument found for Collection<T> type: $kType")
            return TypeSchema.of(
                name = className,
                kType = kType,
                schema = Spec.objectType()
            )
        }

        // Traverse the argument type to its respective TypeSchema,
        val typeSchema: TypeSchema = TypeInspector.traverse(
            kType = argumentType,
            typeParameterMap = typeParameterMap
        )

        // Although is a collection type, we suffix the name with 'ArrayOf' for simplicity.
        return TypeSchema.of(
            name = "ArrayOf${typeSchema.name}",
            kType = kType,
            schema = Spec.collection(value = typeSchema.schema)
        )
    }
}