/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.resolver

import io.github.perracodex.kopapi.inspector.TypeInspector
import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.spec.Spec
import io.github.perracodex.kopapi.inspector.type.ElementMetadata
import io.github.perracodex.kopapi.inspector.type.TypeDefinition
import io.github.perracodex.kopapi.inspector.type.nativeName
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KProperty1
import kotlin.reflect.KType

/**
 * Resolves complex or basics type (such as data classes or primitives) into a [TypeDefinition].
 *
 * Resolves both Kotlin primitives and complex types like data classes. It also
 * manages recursive structures, circular dependencies, and caches type definitions for reuse.
 *
 * Primitive types are immediately mapped, while complex types are recursively processed,
 * including their properties.
 *
 * Responsibilities:
 * - Handling of complex types (eg: data classes).
 * - Handling of primitive types.
 * - Traversing properties of complex types and resolving them.
 * - Handling circular dependencies and recursive structures.
 * - Caching the created [TypeDefinition] to avoid redundant processing.
 */
@TypeInspectorAPI
internal object ObjectResolver {
    /** Temporarily tracks processed [KType] objects while recursing. */
    private val inProcess: MutableSet<String> = mutableSetOf()

    /**
     * Processes a complex or basic type into a [TypeDefinition],
     * traversing its properties and handling metadata.
     *
     * @param kType The [KType] representing the type to resolve.
     * @param kClass The [KClass] corresponding to the type to resolve.
     * @param typeParameterMap A map of type parameters' [KClassifier] to their corresponding [KType].
     * @return The resolved [TypeDefinition] for the complex or basic type.
     */
    fun process(
        kType: KType,
        kClass: KClass<*>,
        typeParameterMap: Map<KClassifier, KType>
    ): TypeDefinition {
        val className: String = ElementMetadata.getClassName(kClass = kClass)

        // Handle primitive types.
        TypeInspector.mapPrimitiveType(kClass = kClass)?.let { definition ->
            return TypeDefinition.of(
                name = className,
                kType = kType,
                definition = definition
            )
        }

        // If not a primitive type then assume it's a complex type.
        return handleComplexType(
            className = className,
            kType = kType,
            kClass = kClass,
            typeParameterMap = typeParameterMap
        )
    }

    /**
     * Handles complex types, such as data classes, traversing their properties to resolve them.
     *
     * @param className The name of the class to process.
     * @param kType The [KType] representing the complex type.
     * @param kClass The [KClass] representing the complex type.
     * @param typeParameterMap A map of type parameters' [KClassifier] to their corresponding [KType].
     * @return The resolved [TypeDefinition] for the complex type.
     */
    private fun handleComplexType(
        className: String,
        kType: KType,
        kClass: KClass<*>,
        typeParameterMap: Map<KClassifier, KType>
    ): TypeDefinition {
        // Prevent infinite recursion for self-referencing objects.
        if (inProcess.contains(kType.nativeName())) {
            return TypeDefinition.of(
                name = className,
                kType = kType,
                definition = Spec.reference(schema = className)
            )
        }
        inProcess.add(kType.nativeName())

        // Create an empty definition before processing properties to handle circular dependencies.
        val propertiesMap: MutableMap<String, Any> = mutableMapOf()
        val placeholder: TypeDefinition = TypeDefinition.of(
            name = className,
            kType = kType,
            definition = Spec.properties(value = propertiesMap)
        )
        TypeInspector.addToCache(definition = placeholder)

        // Create a properties map to hold each of the traversed definitions.
        val propertyDefinitions: MutableMap<String, Map<String, Any>> = mutableMapOf()

        // Traverse each property and resolve its definition.
        val typeProperties: List<KProperty1<out Any, *>> = PropertyResolver.getProperties(kClass = kClass)
        typeProperties.forEach { property ->
            val (propertyName, extendedDefinition) = PropertyResolver.traverse(
                property = property,
                typeParameterMap = typeParameterMap
            )
            propertyDefinitions[propertyName] = extendedDefinition
        }

        // Add the resolved property definitions to the properties map
        // so they are reflected in the placeholder type definition.
        propertiesMap.putAll(propertyDefinitions)

        // Once done remove the processing type from the in-memory tracker to handle different branches.
        inProcess.remove(kType.nativeName())

        return TypeDefinition.of(
            name = className,
            kType = kType,
            definition = Spec.reference(schema = className)
        )
    }
}
