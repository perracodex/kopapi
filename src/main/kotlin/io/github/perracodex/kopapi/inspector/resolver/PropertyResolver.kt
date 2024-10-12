/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.resolver

import io.github.perracodex.kopapi.inspector.TypeInspector
import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.descriptor.MetadataDescriptor
import io.github.perracodex.kopapi.inspector.schema.SchemaProperty
import io.github.perracodex.kopapi.inspector.schema.TypeSchema
import io.github.perracodex.kopapi.inspector.utils.resolveGenerics
import io.github.perracodex.kopapi.utils.Tracer
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
 *          - Traverse Property Type: Uses `TypeInspector` to traverse the property's type.
 *          - Apply Metadata: Incorporates metadata into the property's schema.
 *      - Result: Collects property schemas to be included in the parent object schema.
 *
 * @see [TypeInspector]
 * @see [MetadataDescriptor]
 */
@TypeInspectorAPI
internal class PropertyResolver(private val typeInspector: TypeInspector) {
    private val tracer = Tracer<PropertyResolver>()

    /**
     * Processes the given [property] by traversing it and resolving its schema.
     *
     * @param classKType The [KType] of the class declaring the property.
     * @param property The [KProperty1] to process.
     * @param typeParameterMap A map of type parameter classifiers to actual [KType] for replacement.
     * @return A [SchemaProperty] containing information about the property.
     */
    fun traverse(
        classKType: KType,
        property: KProperty1<out Any, *>,
        typeParameterMap: Map<KClassifier, KType>
    ): Pair<String, SchemaProperty> {
        val metadata: MetadataDescriptor = MetadataDescriptor.of(
            classKType = classKType,
            property = property
        )

        val propertyType: KType = property.returnType.resolveGenerics(
            typeParameterMap = typeParameterMap
        )

        val typeSchema: TypeSchema = typeInspector.traverseType(
            kType = propertyType,
            typeParameterMap = typeParameterMap
        )

        // Create the SchemaProperty with metadata
        return Pair(
            metadata.name, SchemaProperty(
                schema = typeSchema.schema,
                isNullable = metadata.isNullable,
                isRequired = metadata.isRequired,
                originalName = metadata.originalName,
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
        val orderedProperties: MutableList<KProperty1<out Any, *>> = mutableListOf()
        val processedClasses: MutableSet<KClass<*>> = mutableSetOf()
        var currentClass: KClass<*>? = kClass

        while (currentClass != null && currentClass != Any::class && !processedClasses.contains(currentClass)) {
            processedClasses.add(currentClass)

            // 1. Retrieve declared fields using Java reflection to maintain order.
            // Kotlin reflection does not guarantee order for properties.
            val declaredFields: Array<out Field> = currentClass.java.declaredFields

            // 2. Retrieve all properties declared directly within the current class.
            // This includes both properties from the primary constructor and those declared in the class body.
            val declaredMemberProperties: Collection<KProperty1<out Any, *>> = currentClass.declaredMemberProperties

            // 3. Map of property names to their corresponding Kotlin property instances for quick lookup.
            val kotlinPropertiesMap: Map<String, KProperty1<out Any, *>> = declaredMemberProperties.associateBy { it.name }

            // 4. Create a Set to keep track of properties already added, to prevent duplicates.
            val addedPropertyNames: MutableSet<String> = mutableSetOf()

            // 5. Map Java fields to Kotlin properties in the order they are declared in the source code.
            for (field in declaredFields) {
                if (field.isSynthetic) continue

                val propertyName: String = field.name
                if (propertyName.isNotBlank()) {
                    val kotlinProperty: KProperty1<out Any, *>? = kotlinPropertiesMap[propertyName]
                    if (kotlinProperty != null) {
                        // Only include public properties.
                        if (kotlinProperty.visibility == KVisibility.PUBLIC) {
                            // Avoid adding duplicate properties (in case of shadowing).
                            if (orderedProperties.none { it.name == kotlinProperty.name }) {
                                orderedProperties.add(kotlinProperty)
                                addedPropertyNames.add(propertyName)
                            }
                        }
                    }
                }
            }

            // 6. If failed to resolve the properties through the java fields, fallback to declared properties.
            // Note that if a property doesn't have a backing field, it won't be included in the Java fields.
            // This is correct as we don't want to include properties that are not backed by fields, since
            // these are not serialized by default.
            if (orderedProperties.isEmpty()) {
                val additionalProperties: List<KProperty1<out Any, *>> = declaredMemberProperties
                    .filter {
                        it.visibility == KVisibility.PUBLIC
                    }
                orderedProperties.addAll(additionalProperties)
            }

            // Move to the superclass, if any.
            currentClass = currentClass.superclasses.firstOrNull()
        }

        if (orderedProperties.isEmpty()) {
            tracer.error("No properties found for class: $kClass")
        }

        return orderedProperties
    }
}
