/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.parser.resolver

import io.github.perracodex.kopapi.parser.TypeInspector
import io.github.perracodex.kopapi.parser.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.parser.definition.ElementMetadata
import io.github.perracodex.kopapi.parser.definition.TypeDefinition
import io.github.perracodex.kopapi.parser.spec.SpecKey
import kotlin.reflect.*
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Handles traversing of object properties, including traversing their types and handling metadata.
 *
 * Responsibilities:
 * - Processing properties by traversing their type definitions.
 * - Handling metadata such as annotations, nullability, etc.
 * - Ensuring that obtained properties are sorted as per the primary constructor's parameter order.
 */
@TypeInspectorAPI
internal object PropertyResolver {
    /**
     * Processes a property by traversing its type definition,
     * in addition to handling metadata such as annotations, nullability, etc.
     *
     * @param property The property to process.
     * @param typeParameterMap A map of type parameter classifiers to actual KTypes for replacement.
     * @return A Pair containing the serialized name and the extended definition map.
     */
    fun traverse(
        property: KProperty1<*, *>,
        typeParameterMap: Map<KClassifier, KType>
    ): Pair<String, Map<String, Any>> {
        val metadata: ElementMetadata = ElementMetadata.of(property = property)

        val propertyType: KType = TypeInspector.replaceTypeIfNeeded(
            type = property.returnType,
            typeParameterMap = typeParameterMap
        )

        val typeDefinition: TypeDefinition = TypeInspector.traverse(
            kType = propertyType,
            typeParameterMap = typeParameterMap
        )

        typeDefinition.definition.apply {
            metadata.originalName?.let {
                put(SpecKey.ORIGINAL_NAME(), it)
            }
            if (!metadata.isRequired) {
                put(SpecKey.REQUIRED(), false)
            }
            if (metadata.isTransient) {
                put(SpecKey.TRANSIENT(), true)
            }
        }

        return metadata.name to typeDefinition.definition
    }

    /**
     * Retrieves and sorts properties based on the primary constructor's parameter order.
     * For classes without a primary constructor, properties are sorted based on their declaration order.
     *
     * @param kClass The Kotlin class.
     * @return A list of KProperty1 sorted according to the constructor's parameter order.
     */
    fun getProperties(kClass: KClass<*>): List<KProperty1<out Any, *>> {
        val primaryConstructor: KFunction<Any>? = kClass.primaryConstructor
        val constructorParameters: List<String> = primaryConstructor?.parameters?.mapNotNull { it.name } ?: emptyList()

        // Map property names to KProperty1 objects.
        val propertyMap: Map<String, KProperty1<out Any, *>> = kClass.declaredMemberProperties.associateBy { it.name }

        // Sort properties based on constructor parameter order.
        val sortedProperties: List<KProperty1<out Any, *>> = constructorParameters.mapNotNull { propertyMap[it] }

        // Append any additional properties not defined in the constructor.
        val additionalProperties: List<KProperty1<out Any, *>> = propertyMap.keys
            .subtract(constructorParameters.toSet())
            .mapNotNull { propertyMap[it] }

        return sortedProperties + additionalProperties
    }
}
