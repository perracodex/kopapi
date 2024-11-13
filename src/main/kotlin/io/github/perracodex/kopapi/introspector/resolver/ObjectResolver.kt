/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.introspector.resolver

import io.github.perracodex.kopapi.annotation.SchemaAnnotationAttributes
import io.github.perracodex.kopapi.annotation.SchemaAnnotationParser
import io.github.perracodex.kopapi.introspector.TypeIntrospector
import io.github.perracodex.kopapi.introspector.annotation.TypeIntrospectorApi
import io.github.perracodex.kopapi.introspector.descriptor.ElementName
import io.github.perracodex.kopapi.introspector.descriptor.MetadataDescriptor
import io.github.perracodex.kopapi.introspector.schema.TypeSchema
import io.github.perracodex.kopapi.introspector.schema.factory.PrimitiveFactory
import io.github.perracodex.kopapi.introspector.schema.factory.SchemaFactory
import io.github.perracodex.kopapi.schema.facet.ElementSchema
import io.github.perracodex.kopapi.schema.facet.SchemaProperty
import io.github.perracodex.kopapi.system.Tracer
import io.github.perracodex.kopapi.util.nativeName
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
 *          - Traverse Properties: Recursively traverses each property using `TypeIntrospector`.
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
 * @see [TypeIntrospector]
 */
@TypeIntrospectorApi
internal class ObjectResolver(private val introspector: TypeIntrospector) {
    private val tracer = Tracer<ObjectResolver>()

    /** Semaphore to prevent infinite recursion during type processing. */
    private val semaphore: MutableSet<String> = mutableSetOf()

    /**
     * Processes a complex or basic type into a [TypeSchema],
     * traversing its properties and handling metadata.
     *
     * @param kType The [KType] representing the type to resolve.
     * @param kClass The [KClass] corresponding to the type to resolve.
     * @param typeArgumentBindings A map of type arguments' [KClassifier] to their actual [KType] replacements.
     * @return The resolved [TypeSchema] for the complex or basic type.
     */
    fun traverse(
        kType: KType,
        kClass: KClass<*>,
        typeArgumentBindings: Map<KClassifier, KType>
    ): TypeSchema {
        tracer.debug("Traversing object type: $kType.")

        val className: ElementName = MetadataDescriptor.getClassName(kClass = kClass)

        // Handle primitive types.
        PrimitiveFactory.newSchema(kClass = kClass)?.let { schema ->
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
            typeArgumentBindings = typeArgumentBindings
        )
    }

    /**
     * Handles complex types, such as data classes, traversing their properties to resolve them.
     *
     * @param className The name of the class to process.
     * @param kType The [KType] representing the complex type.
     * @param kClass The [KClass] representing the complex type.
     * @param typeArgumentBindings A map of type arguments' [KClassifier] to their actual [KType] replacements.
     * @return The resolved [TypeSchema] for the complex type.
     */
    private fun handleComplexType(
        className: ElementName,
        kType: KType,
        kClass: KClass<*>,
        typeArgumentBindings: Map<KClassifier, KType>
    ): TypeSchema {
        // Avoid redundant processing: skip if the type is already processed or self-references itself.
        if (introspector.isCached(kType = kType) || semaphore.contains(kType.nativeName())) {
            return TypeSchema.of(
                name = className,
                kType = kType,
                schema = SchemaFactory.ofReference(schemaName = className.name, referencedType = kType)
            )
        }
        semaphore.add(kType.nativeName())

        // Resolve the object attributes.
        val attributes: SchemaAnnotationAttributes? = SchemaAnnotationParser.parse(element = kClass)

        // Add a schema placeholder early to avoid circular references.
        val schemaPlaceholder: TypeSchema = TypeSchema.of(
            name = className,
            kType = kType,
            schema = SchemaFactory.ofObjectDescriptor(attributes = attributes)
        )
        introspector.addToCache(schema = schemaPlaceholder)

        // Get the placeholder schema to place each property.
        val propertiesSchemas: ElementSchema.ObjectDescriptor = schemaPlaceholder.schema as ElementSchema.ObjectDescriptor

        // Retrieve all relevant properties of the generic class.
        val classProperties: List<KProperty1<out Any, *>> = introspector.getClassProperties(kClass = kClass)

        // Traverse each property to resolve its schema using the merged type parameters.
        classProperties.forEach { property ->
            val (name: String, schemaProperty: SchemaProperty) = introspector.traverseProperty(
                classKType = kType,
                property = property,
                typeArgumentBindings = typeArgumentBindings
            )

            propertiesSchemas.objectProperties[name] = schemaProperty
        }

        // Once done traversing remove the processing type from the in-memory
        // tracker to handle different branches.
        semaphore.remove(kType.nativeName())

        tracer.debug("Resolved object type: $kType.")
        return TypeSchema.of(
            name = className,
            kType = kType,
            schema = SchemaFactory.ofReference(schemaName = className.name, referencedType = kType)
        )
    }
}
