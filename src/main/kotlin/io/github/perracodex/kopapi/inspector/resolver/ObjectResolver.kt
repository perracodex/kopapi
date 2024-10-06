/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.resolver

import io.github.perracodex.kopapi.inspector.TypeInspector
import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.spec.Spec
import io.github.perracodex.kopapi.inspector.type.ElementMetadata
import io.github.perracodex.kopapi.inspector.type.TypeDescriptor
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
 * - Handling of complex types (e.g.: classes, data classes).
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
        TypeDescriptor.mapPrimitiveType(kClass = kClass)?.let { schema ->
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
    @Suppress("DuplicatedCode")
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

        // Add a schema placeholder early to avoid circular references.
        val schemaPlaceholder: TypeSchema = TypeSchema.of(
            name = className,
            kType = kType,
            schema = Spec.properties(value = mutableMapOf())
        )
        TypeInspector.addToCache(schema = schemaPlaceholder)

        // Create a properties map to hold each of the traversed schemas.
        val propertiesSchemas: MutableMap<String, Any> = mutableMapOf()

        // Traverse each class property and resolve its schema.
        val classProperties: List<KProperty1<out Any, *>> = PropertyResolver.getProperties(kClass = kClass)
        classProperties.forEach { property ->
            val propertySchema: PropertySchema = PropertyResolver.traverse(
                classKType = kType,
                property = property,
                typeParameterMap = typeParameterMap
            )
            propertiesSchemas[propertySchema.name] = propertySchema.schema
        }

        // Once done traversing remove the processing type from the in-memory
        // tracker to handle different branches.
        inProcessTypeSemaphore.remove(kType.nativeName())

        // Update the placeholder with actual processed schemas.
        schemaPlaceholder.schema.putAll(
            Spec.properties(value = propertiesSchemas)
        )

        return TypeSchema.of(
            name = className,
            kType = kType,
            schema = Spec.reference(schema = className)
        )
    }
}
