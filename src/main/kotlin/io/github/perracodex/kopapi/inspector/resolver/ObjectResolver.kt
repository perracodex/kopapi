/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.resolver

import io.github.perracodex.kopapi.inspector.TypeResolver
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
 * - Purpose:
 *      - Handles complex object types, such as classes and data classes.
 * - Action:
 *      - Check for Primitive Type:
 *          - If determined is a primitive type, maps it directly to its schema representation.
 *      - Handle Complex Type:
 *          - Circular Reference Prevention: Uses a semaphore to prevent infinite recursion in case of self-referencing types.
 *          - Add Placeholder Schema: Adds a placeholder schema to the cache before processing properties.
 *          - Retrieve Properties: Uses `PropertyResolver` to get the class properties.
 *          - Traverse Properties: Recursively traverses each property using `TypeResolver`.
 *          - Update Schema: Fills in the placeholder schema with the resolved properties.
 *          - Remove from Semaphore: Removes the type from the semaphore after processing.
 *      - Result: Constructs and returns the object schema.
 *
 * #### Circular Reference Handling
 * - Purpose:
 *      - Prevents infinite loops when types reference themselves directly or indirectly.
 * - Mechanism:
 *      - Semaphore: Tracks types currently being processed to detect circular references.
 *      - Placeholder Schema: Adds a temporary schema to the cache to satisfy references during processing.
 *      - Update After Processing: Replaces the placeholder with the completed schema after processing.
 *      - Remove from Semaphore: Removes the type from the semaphore after processing.
 * - Example:
 *      - When a class Node has a property of type Node, the semaphore detects
 *        the self-reference and handles it appropriately.
 *
 * @see [PropertyResolver]
 * @see [TypeResolver]
 */
@TypeInspectorAPI
internal class ObjectResolver(private val typeResolver: TypeResolver) {
    /** Semaphore to prevent infinite recursion during type processing. */
    private val semaphore: MutableSet<String> = mutableSetOf()

    /**
     * Processes a complex or basic type into a [TypeSchema],
     * traversing its properties and handling metadata.
     *
     * @param kType The [KType] representing the type to resolve.
     * @param kClass The [KClass] corresponding to the type to resolve.
     * @param typeParameterMap A map of type parameters' [KClassifier] to their corresponding [KType].
     * @return The resolved [TypeSchema] for the complex or basic type.
     */
    fun traverse(
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
        if (semaphore.contains(kType.nativeName())) {
            return TypeSchema.of(
                name = className,
                kType = kType,
                schema = Spec.reference(schema = className)
            )
        }
        semaphore.add(kType.nativeName())

        // Add a schema placeholder early to avoid circular references.
        val schemaPlaceholder: TypeSchema = TypeSchema.of(
            name = className,
            kType = kType,
            schema = Spec.properties(value = mutableMapOf())
        )
        typeResolver.addToCache(schema = schemaPlaceholder)

        // Initialize a map to hold resolved schemas for each property.
        val propertiesSchemas: MutableMap<String, Any> = mutableMapOf()

        // Retrieve all relevant properties of the generic class.
        val classProperties: List<KProperty1<out Any, *>> = typeResolver.getClassProperties(kClass = kClass)

        // Traverse each property to resolve its schema using the merged type parameters.
        classProperties.forEach { property ->
            val propertySchema: PropertySchema = typeResolver.traverseProperty(
                classKType = kType,
                property = property,
                typeParameterMap = typeParameterMap
            )
            propertiesSchemas[propertySchema.name] = propertySchema.schema
        }

        // Once done traversing remove the processing type from the in-memory
        // tracker to handle different branches.
        semaphore.remove(kType.nativeName())

        // Update the cached schema placeholder with the resolved property schemas.
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
