/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.resolver

import io.github.perracodex.kopapi.inspector.TypeInspector
import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.spec.SpecKey
import io.github.perracodex.kopapi.inspector.type.ElementMetadata
import io.github.perracodex.kopapi.inspector.type.TypeSchema
import io.github.perracodex.kopapi.utils.Tracer
import java.lang.reflect.Field
import kotlin.reflect.*
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.superclasses

/**
 * Handles traversing of object properties, including traversing their types and handling metadata.
 *
 * Responsibilities:
 * - Processing properties by traversing them to generate their schema.
 * - Handling metadata such as annotations, nullability, etc.
 * - Ensuring that obtained properties are sorted as per the primary constructor's parameter order.
 */
@TypeInspectorAPI
internal object PropertyResolver {
    private val tracer = Tracer<PropertyResolver>()

    /**
     * Processes the given [property] by traversing it and resolving its schema.
     *
     * @param property The [KProperty1] to process.
     * @param typeParameterMap A map of type parameter classifiers to actual [KType] for replacement.
     * @return A [Pair] containing the resolved property name and the resolved schema map.
     */
    fun traverse(
        property: KProperty1<out Any, *>,
        typeParameterMap: Map<KClassifier, KType>
    ): Pair<String, Map<String, Any>> {
        val metadata: ElementMetadata = ElementMetadata.of(property = property)

        val propertyType: KType = TypeInspector.replaceTypeIfNeeded(
            type = property.returnType,
            typeParameterMap = typeParameterMap
        )

        val typeSchema: TypeSchema = TypeInspector.traverse(
            kType = propertyType,
            typeParameterMap = typeParameterMap
        )

        typeSchema.schema.apply {
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

        return metadata.name to typeSchema.schema
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

            // 6. Append remaining Kotlin properties that were not mapped via Java fields.
            // Only include public properties.
            val additionalProperties: List<KProperty1<out Any, *>> = declaredMemberProperties
                .filter {
                    it.visibility == KVisibility.PUBLIC &&
                            it.name !in addedPropertyNames
                }
            orderedProperties.addAll(additionalProperties)

            // Move to the superclass, if any.
            currentClass = currentClass.superclasses.firstOrNull()
        }

        if (orderedProperties.isEmpty()) {
            tracer.error("No properties found for class: $kClass")
        }

        return orderedProperties
    }
}
