/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.descriptor

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.utils.cleanName
import io.github.perracodex.kopapi.inspector.utils.safeName
import io.github.perracodex.kopapi.system.Tracer
import kotlinx.serialization.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor

/**
 * Represents metadata for an element basic properties.
 *
 * @property name The current name of the element. If renamed, this reflects the updated name.
 * @property renamedFrom The original name of the element before renaming. It is `null` if the name was not changed.
 * @property isRequired Indicates whether the element is required. Defaults to true.
 * @property isNullable Indicates whether the element is nullable. Defaults to false.
 * @property isTransient Indicates whether the element should be ignored. Defaults to false.
 */
@TypeInspectorAPI
internal data class MetadataDescriptor(
    val name: String,
    val renamedFrom: String? = null,
    val isRequired: Boolean = true,
    val isNullable: Boolean = false,
    val isTransient: Boolean = false
) {
    companion object {
        private val tracer = Tracer<MetadataDescriptor>()

        /**
         * Resolves the name for the give [kClass], considering serializer annotations if present.
         *
         * @param kClass The [KClass] to process for name resolution.
         * @return The resolved class name.
         */
        fun getClassName(kClass: KClass<*>): ElementName {
            return geElementName(target = kClass)
        }

        /**
         * Creates an [MetadataDescriptor] by inspecting the annotations from the given [property].
         *
         * @param classKType The [KType] of the class declaring the property.
         * @param property The [KProperty1] to extract metadata from.
         * @return The constructed [MetadataDescriptor] instance.
         */
        fun of(
            classKType: KType,
            property: KProperty1<out Any, *>
        ): MetadataDescriptor {
            val elementName: ElementName = geElementName(target = property)

            val isTransient: Boolean = property.isTransient()
            val isNullable: Boolean = property.isNullable()
            val isRequired: Boolean = !isTransient && determineIfRequired(
                classKType = classKType,
                property = property,
                elementName = elementName
            )

            return MetadataDescriptor(
                name = elementName.name,
                renamedFrom = elementName.renamedFrom,
                isRequired = isRequired,
                isNullable = isNullable,
                isTransient = isTransient
            )
        }

        /**
         * Extension function to determines if a property is transient,
         * this is if the property should be ignored.
         *
         * @return True if the property is transient, false otherwise.
         */
        private fun KProperty1<out Any, *>.isTransient(): Boolean {
            return this.hasAnnotation<Transient>() ||
                    this.hasAnnotation<JsonIgnore>()
        }

        /**
         * Extension function to determines if a property is nullable.
         *
         * @return True if the property is nullable, false otherwise.
         */
        private fun KProperty1<out Any, *>.isNullable(): Boolean {
            return this.returnType.isMarkedNullable
        }

        /**
         * Resolves the name of the given [target], either from specific annotations if present,
         * or from the target's own name.
         *
         * @param target The target ([KClass] or [KProperty1]) to process for name resolution.
         * @return The resolved name from annotations or the target's own name if no annotation is found.
         * @throws IllegalArgumentException if the target is not a supported type.
         */
        private fun geElementName(target: Any): ElementName {
            val originalName: String = when (target) {
                is KClass<*> -> target.safeName()
                is KProperty1<*, *> -> target.name
                else -> {
                    tracer.error("Unable to resolve element name. Unsupported target type: $target")
                    return createFallbackName(target = target)
                }
            }

            if (originalName.isBlank()) {
                tracer.error("Unable to resolve element name. Empty or blank name for target: $target")
                return createFallbackName(target = target)
            }

            // List of pairs containing annotation lookup functions and the way to extract the relevant value.
            val annotationCheckers: Set<(Any) -> String?> = setOf(
                { element ->
                    (element as? KClass<*>)?.findAnnotation<SerialName>()?.value
                },
                { element ->
                    (element as? KProperty1<*, *>)?.findAnnotation<SerialName>()?.value
                },
                { element ->
                    (element as? KClass<*>)?.findAnnotation<JsonTypeName>()?.value
                },
                { element ->
                    (element as? KProperty1<*, *>)?.findAnnotation<JsonProperty>()?.value
                }
            )

            // Iterate over the annotation checkers to find the first non-blank name.
            annotationCheckers.forEach { checker ->
                checker(target)?.let { serialName ->
                    if (serialName.isNotBlank() && serialName != originalName) {
                        return ElementName(name = serialName, renamedFrom = originalName)
                    }
                }
            }

            return ElementName(name = originalName)
        }

        /**
         * Creates a fallback name for an element based on the target's type.
         */
        private fun createFallbackName(target: Any): ElementName {
            return ElementName(name = "UnknownElement_${target.toString().cleanName()}")
        }

        /**
         * Determines if a property is required, first by checking for annotations,
         * and if not found, by querying the class serializer.
         *
         * @param classKType The [KType] of the class declaring the property.
         * @param property The [KProperty1] to determine if it is required.
         * @param elementName The resolved element name.
         * @return True if the property is required, false otherwise.
         */
        @OptIn(ExperimentalSerializationApi::class)
        private fun determineIfRequired(
            classKType: KType,
            property: KProperty1<out Any, *>,
            elementName: ElementName
        ): Boolean {
            return try {
                // First check if the property is annotated.
                // If no annotation is found, use the class kotlinx serializer.
                when {
                    property.hasAnnotation<Required>() -> true // kotlinx's @Required
                    property.hasAnnotation<JsonIgnore>() -> false // Jackson's @JsonIgnore
                    else -> {
                        val classSerializer: KSerializer<Any?> = serializer(type = classKType)
                        val index: Int = classSerializer.descriptor.getElementIndex(name = elementName.name)
                        !classSerializer.descriptor.isElementOptional(index = index)
                    }
                }
            } catch (e: Exception) {
                // If there is an error (e.g., no serializer found)
                // fallback to checking the primary constructor.
                determineIfRequiredByConstructor(
                    kClass = classKType.classifier as KClass<*>,
                    property = property,
                )
            }
        }

        /**
         * Falls back to determine if a property is required by checking the primary constructor's parameters.
         * If the parameter is not found (e.g. is a body parameter), it is assumed to mandatory.
         *
         * @param kClass The [KClass] of the class declaring the property.
         * @param property The [KProperty1] to determine if it is required.
         */
        private fun determineIfRequiredByConstructor(
            kClass: KClass<*>,
            property: KProperty1<out Any, *>,
        ): Boolean {
            return try {
                kClass.primaryConstructor?.parameters?.find { argument ->
                    argument.name == property.name
                }?.let { !it.isOptional } ?: true // Assuming true if the parameter is not found.
            } catch (e: Exception) {
                tracer.error(
                    message = "Unable to determine if property is required by constructor. Field: $property",
                    cause = e
                )
                true // Assuming true as a fallback.
            }
        }
    }
}
