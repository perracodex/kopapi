/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.introspector.resolver

import io.github.perracodex.kopapi.annotation.SchemaAttributeBinder
import io.github.perracodex.kopapi.introspector.TypeIntrospector
import io.github.perracodex.kopapi.introspector.annotation.TypeIntrospectorApi
import io.github.perracodex.kopapi.introspector.descriptor.MetadataDescriptor
import io.github.perracodex.kopapi.introspector.descriptor.resolveArgumentBinding
import io.github.perracodex.kopapi.introspector.schema.TypeSchema
import io.github.perracodex.kopapi.schema.facet.ElementSchema
import io.github.perracodex.kopapi.schema.facet.SchemaProperty
import io.github.perracodex.kopapi.system.Tracer
import java.lang.reflect.Field
import kotlin.reflect.*
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.superclasses

/**
 * - Purpose:
 *      - Handles properties of objects, including metadata extraction and type resolution.
 * - Action:
 *      - Retrieve Properties:
 *          - Maintain Order: Gets properties in declaration order, including inherited properties.
 *          - Exclude Non-Public: Excludes non-public properties.
 *      - Process Each Property:
 *          - Extract Metadata: Retrieves information such as name, nullability, and annotations.
 *          - Resolve Property Type: Determines the property's type, substituting generics as necessary.
 *          - Traverse Property Type: Uses `TypeIntrospector` to traverse the property's type.
 *          - Apply Metadata: Incorporates metadata into the property's schema.
 *      - Result: Collects property schemas to be included in the parent object schema.
 *
 * @see [TypeIntrospector]
 * @see [MetadataDescriptor]
 */
@TypeIntrospectorApi
internal class PropertyResolver(private val introspector: TypeIntrospector) {
    private val tracer: Tracer = Tracer<PropertyResolver>()

    /**
     * Processes the given [property] by traversing it and resolving its schema.
     *
     * @param classKType The [KType] of the class declaring the property.
     * @param property The [KProperty1] to process.
     * @param typeArgumentBindings A map of type arguments' [KClassifier] to their actual [KType] replacements.
     * @return A [SchemaProperty] containing information about the property.
     */
    fun traverse(
        classKType: KType,
        property: KProperty1<out Any, *>,
        typeArgumentBindings: Map<KClassifier, KType>
    ): Pair<String, SchemaProperty> {
        tracer.debug("Processing property: ${property.name}.")

        val metadata: MetadataDescriptor = MetadataDescriptor.of(
            classKType = classKType,
            property = property
        )

        // Resolves the property type, substituting type arguments if applicable,
        // or retaining the original type if no matching binding is found.
        val propertyType: KType = property.returnType.resolveArgumentBinding(
            bindings = typeArgumentBindings
        )

        // Traverse the property type to obtain its schema.
        val typeSchema: TypeSchema = introspector.traverseType(
            kType = propertyType,
            typeArgumentBindings = typeArgumentBindings
        )

        // If attribute metadata is present, apply it to the schema.
        val schema: ElementSchema = metadata.attributes?.let { attributes ->
            when (typeSchema.schema) {
                is ElementSchema.ObjectDescriptor ->
                    typeSchema.schema

                else ->
                    SchemaAttributeBinder.bind(
                        schema = typeSchema.schema,
                        attributes = attributes
                    )
            }
        } ?: typeSchema.schema

        // Create the SchemaProperty with metadata
        return Pair(
            metadata.name, SchemaProperty(
                schema = schema,
                isNullable = metadata.isNullable,
                isRequired = metadata.isRequired,
                renamedFrom = metadata.renamedFrom,
                isTransient = metadata.isTransient
            )
        )
    }

    /**
     * Retrieves the properties from the given [kClass] preserving their declaration order.
     * This includes those defined in the primary constructor and in the class body.
     * Non-public properties are excluded from the result.
     *
     * Inherited properties are also included, being appended after the subclass's properties.
     *
     * @param kClass The [KClass] to retrieve properties from.
     * @return A list of [KProperty1] items sorted according to their declaration order.
     */
    fun getProperties(kClass: KClass<*>): List<KProperty1<out Any, *>> {
        tracer.debug("Retrieving properties for class: $kClass.")

        // Hold the output ordered properties.
        val orderedProperties: MutableList<KProperty1<out Any, *>> = mutableListOf()

        // Hold processed classes to avoid infinite loops in inheritance hierarchies.
        val processedClasses: MutableSet<KClass<*>> = mutableSetOf()

        // Track added property names globally across the class hierarchy.
        // Needed to avoid duplicate properties in case of shadowing.
        val addedPropertyNames: MutableSet<String> = mutableSetOf()

        var currentClass: KClass<*>? = kClass

        while (currentClass != null && currentClass != Any::class && !processedClasses.contains(currentClass)) {
            processedClasses.add(currentClass)

            runCatching {
                // 1. Retrieve declared fields using Java reflection to maintain order,
                // as Kotlin reflection does not guarantee order for properties.
                val declaredFields: List<Field> = currentClass.java.declaredFields.filter { !it.isSynthetic }
                if (declaredFields.isEmpty()) {
                    tracer.warning("No backing fields found for class: $currentClass")
                    return@runCatching
                }

                // 2. Retrieve all properties declared directly within the current class.
                // This includes both properties from the primary constructor and those declared in the class body.
                val declaredMemberProperties: Collection<KProperty1<out Any, *>> = currentClass
                    .declaredMemberProperties.filter { it.visibility == KVisibility.PUBLIC }
                if (declaredMemberProperties.isEmpty()) {
                    tracer.warning("No public properties found for class: $currentClass")
                    return@runCatching
                }

                // 3. Map of property names to their corresponding Kotlin property instances for quick lookup.
                val kotlinPropertiesMap: Map<String, KProperty1<out Any, *>> = declaredMemberProperties.associateBy { it.name }

                // 4. Map Java fields to Kotlin properties in the order they are declared in the source code.
                for (field in declaredFields) {
                    val propertyName: String = field.name
                    if (propertyName.isNotBlank()) {
                        kotlinPropertiesMap[propertyName]?.let { kotlinProperty ->
                            // Avoid adding duplicate properties, in case of shadowing.
                            if (addedPropertyNames.add(propertyName)) {
                                orderedProperties.add(kotlinProperty)
                            }
                        }
                    }
                }

                if (orderedProperties.isEmpty()) {
                    tracer.warning("No properties found through Java fields for class: $currentClass.")
                }
            }.onFailure { error ->
                tracer.error(message = "Failed to retrieve properties for class: $currentClass", cause = error)
            }

            // Move to the superclass, if any.
            currentClass = currentClass.superclasses.firstOrNull()
        }

        if (orderedProperties.isEmpty()) {
            tracer.error("No properties found for class: $kClass")
        } else {
            tracer.debug("Retrieved ${orderedProperties.size} properties for class: $kClass.")
        }

        return orderedProperties
    }
}
