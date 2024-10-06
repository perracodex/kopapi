/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.resolver

import io.github.perracodex.kopapi.inspector.TypeInspector
import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.spec.Spec
import io.github.perracodex.kopapi.inspector.type.ElementMetadata
import io.github.perracodex.kopapi.inspector.type.TypeSchema
import io.github.perracodex.kopapi.inspector.type.nativeName
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KProperty1
import kotlin.reflect.KType

/**
 * Resolves complex or basics type (such as classes, data classes or primitives) into a [TypeSchema].
 *
 * Primitive types are immediately mapped, while complex types are recursively inspected.
 *
 * Responsibilities:
 * - Handling of complex types (eg: classes, data classes).
 * - Handling of primitive types.
 * - Handling circular dependencies and recursive structures.
 * - Caching the created [TypeSchema] to avoid redundant processing.
 */
@TypeInspectorAPI
internal object ObjectResolver {
    /** Temporarily tracks processed [KType] objects while traversing/recursing. */
    private val inProcessTypeSemaphore: MutableSet<String> = mutableSetOf()

    /**
     * Processes a complex or basic type into a [TypeSchema],
     * traversing its properties and handling metadata.
     *
     * @param kType The [KType] representing the type to resolve.
     * @param kClass The [KClass] corresponding to the type to resolve.
     * @param typeParameterMap A map of type parameters' [KClassifier] to their corresponding [KType].
     * @return The resolved [TypeSchema] for the complex or basic type.
     */
    fun process(
        kType: KType,
        kClass: KClass<*>,
        typeParameterMap: Map<KClassifier, KType>
    ): TypeSchema {
        val className: String = ElementMetadata.getClassName(kClass = kClass)

        // Handle primitive types.
        TypeInspector.mapPrimitiveType(kClass = kClass)?.let { schema ->
            return TypeSchema.of(
                name = className,
                kType = kType,
                schema = schema
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
     * @return The resolved [TypeSchema] for the complex type.
     */
    private fun handleComplexType(
        className: String,
        kType: KType,
        kClass: KClass<*>,
        typeParameterMap: Map<KClassifier, KType>
    ): TypeSchema {
        // Prevent infinite recursion for self-referencing objects.
        if (inProcessTypeSemaphore.contains(kType.nativeName())) {
            return TypeSchema.of(
                name = className,
                kType = kType,
                schema = Spec.reference(schema = className)
            )
        }
        inProcessTypeSemaphore.add(kType.nativeName())

        // Create an empty TypeSchema before processing properties to handle circular dependencies.
        val propertiesMap: MutableMap<String, Any> = mutableMapOf()
        val schemaPlaceholder: TypeSchema = TypeSchema.of(
            name = className,
            kType = kType,
            schema = Spec.properties(value = propertiesMap)
        )
        TypeInspector.addToCache(schema = schemaPlaceholder)

        // Create a properties map to hold each of the traversed schemas.
        val propertiesSchemas: MutableMap<String, Map<String, Any>> = mutableMapOf()

        // Traverse each property and resolve its schema.
        val typeProperties: List<KProperty1<out Any, *>> = PropertyResolver.getProperties(kClass = kClass)
        typeProperties.forEach { property ->
            val (propertyName, extendedSchema) = PropertyResolver.traverse(
                classKType = kType,
                property = property,
                typeParameterMap = typeParameterMap
            )
            propertiesSchemas[propertyName] = extendedSchema
        }

        // Add the resolved property schema to the properties map
        // so they are reflected in the placeholder TypeSchema.
        propertiesMap.putAll(propertiesSchemas)

        // Once done remove the processing type from the in-memory tracker to handle different branches.
        inProcessTypeSemaphore.remove(kType.nativeName())

        return TypeSchema.of(
            name = className,
            kType = kType,
            schema = Spec.reference(schema = className)
        )
    }
}
