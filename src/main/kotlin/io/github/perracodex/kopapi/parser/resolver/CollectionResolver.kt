/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.parser.resolver

import io.github.perracodex.kopapi.parser.ObjectTypeParser
import io.github.perracodex.kopapi.parser.annotation.ObjectTypeParserAPI
import io.github.perracodex.kopapi.parser.definition.ElementMetadata
import io.github.perracodex.kopapi.parser.definition.TypeDefinition
import io.github.perracodex.kopapi.parser.spec.Spec
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KType

/**
 * Resolves collection types (eg: [List], [Set]) including [Array] types,
 * into their corresponding [TypeDefinition].
 *
 * Responsibilities:
 * - Handling collections (eg: [List], [Set]).
 * - Handling of both primitive and non-primitive [Array] types.
 * - Extracting the contained element type and traversing it if needed.
 */
@ObjectTypeParserAPI
internal object CollectionResolver {
    /**
     * Handles collections (eg: List, Set), including primitive and non-primitive arrays.
     *
     * @param kType The [KType] representing the collection type.
     * @param classifier The [KClassifier] representing the collection class (e.g., List, Set).
     * @param typeParameterMap A map of type parameters' [KClassifier] to actual [KType] items for replacement.
     * @return The resolved [TypeDefinition] for the collection type.
     */
    fun process(
        kType: KType,
        classifier: KClassifier,
        typeParameterMap: Map<KClassifier, KType>
    ): TypeDefinition {
        val className: String = ElementMetadata.getClassName(kClass = (classifier as KClass<*>))

        // Check if the classifier is a primitive array first, such as IntArray, ByteArray, etc.
        if (ObjectTypeParser.isPrimitiveArrayType(classifier = classifier)) {
            val definition: MutableMap<String, Any>? = ObjectTypeParser.mapPrimitiveType(kClass = classifier)
            return TypeDefinition.of(
                name = className,
                kType = kType,
                definition = definition ?: Spec.objectType()
            )
        }

        // Handle non-primitive arrays and collections based on their type arguments.
        val itemType: KType = kType.arguments.firstOrNull()?.type?.let {
            ObjectTypeParser.replaceTypeIfNeeded(type = it, typeParameterMap = typeParameterMap)
        } ?: return TypeDefinition.of(
            name = className,
            kType = kType,
            definition = Spec.objectType()
        )

        // Map the item type to its respective TypeDefinition,
        // considering it's a regular object array or collection.
        val typeDefinition: TypeDefinition = ObjectTypeParser.traverseType(
            kType = itemType,
            typeParameterMap = typeParameterMap
        )

        return TypeDefinition.of(
            name = "ArrayOf${typeDefinition.name}",
            kType = kType,
            definition = Spec.collection(value = typeDefinition.definition)
        )
    }
}