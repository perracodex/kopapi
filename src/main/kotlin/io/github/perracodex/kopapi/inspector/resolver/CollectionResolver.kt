/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.resolver

import io.github.perracodex.kopapi.inspector.TypeInspector
import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.spec.Spec
import io.github.perracodex.kopapi.inspector.type.ElementMetadata
import io.github.perracodex.kopapi.inspector.type.TypeSchema
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KType

/**
 * Resolves collection types (eg: [List], [Set]) including [Array] types,
 * into their corresponding [TypeSchema].
 *
 * Responsibilities:
 * - Handling collections (eg: [List], [Set]).
 * - Handling of both primitive and non-primitive [Array] types.
 * - Extracting the contained element type and traversing it if needed.
 */
@TypeInspectorAPI
internal object CollectionResolver {
    /**
     * Handles collections (eg: List, Set), including primitive and non-primitive arrays.
     *
     * @param kType The [KType] representing the collection type.
     * @param classifier The [KClassifier] representing the collection class (e.g., List, Set).
     * @param typeParameterMap A map of type parameters' [KClassifier] to actual [KType] items for replacement.
     * @return The resolved [TypeSchema] for the collection type.
     */
    fun process(
        kType: KType,
        classifier: KClassifier,
        typeParameterMap: Map<KClassifier, KType>
    ): TypeSchema {
        val className: String = ElementMetadata.getClassName(kClass = (classifier as KClass<*>))

        // Check if the classifier is a primitive array first, such as IntArray, ByteArray, etc.
        if (TypeInspector.isPrimitiveArrayType(classifier = classifier)) {
            val schema: MutableMap<String, Any>? = TypeInspector.mapPrimitiveType(kClass = classifier)
            return TypeSchema.of(
                name = className,
                kType = kType,
                schema = schema ?: Spec.objectType()
            )
        }

        // Handle non-primitive arrays and collections based on their type arguments.
        val itemType: KType = kType.arguments.firstOrNull()?.type?.let {
            TypeInspector.replaceTypeIfNeeded(type = it, typeParameterMap = typeParameterMap)
        } ?: return TypeSchema.of(
            name = className,
            kType = kType,
            schema = Spec.objectType()
        )

        // Map the item type to its respective TypeSchema,
        // considering it's a regular object array or collection.
        val typeSchema: TypeSchema = TypeInspector.traverse(
            kType = itemType,
            typeParameterMap = typeParameterMap
        )

        return TypeSchema.of(
            name = "ArrayOf${typeSchema.name}",
            kType = kType,
            schema = Spec.collection(value = typeSchema.schema)
        )
    }
}