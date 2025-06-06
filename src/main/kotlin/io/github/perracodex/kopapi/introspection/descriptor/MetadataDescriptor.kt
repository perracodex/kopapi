/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.introspection.descriptor

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import io.github.perracodex.kopapi.annotation.SchemaAnnotationAttributes
import io.github.perracodex.kopapi.annotation.SchemaAnnotationParser
import io.github.perracodex.kopapi.introspection.annotation.TypeIntrospectorApi
import io.github.perracodex.kopapi.system.Tracer
import io.github.perracodex.kopapi.util.cleanName
import io.github.perracodex.kopapi.util.safeName
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
 * @property attributes The parsed annotation `@Schema` for the element, if present.
 * @property renamedFrom The original name of the element before renaming. It is `null` if the name was not changed.
 * @property isRequired Indicates whether the element is required. Default: `true`.
 * @property isNullable Indicates whether the element is nullable. Default: `false`.
 * @property isTransient Indicates whether the element should be ignored. Default: `false`.
 */
@TypeIntrospectorApi
internal data class MetadataDescriptor(
    val name: String,
    val attributes: SchemaAnnotationAttributes? = null,
    val renamedFrom: String? = null,
    val isRequired: Boolean = true,
    val isNullable: Boolean = false,
    val isTransient: Boolean = false
) {
    companion object {
        private val tracer: Tracer = Tracer<MetadataDescriptor>()

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

            val attributes: SchemaAnnotationAttributes? = SchemaAnnotationParser.parse(element = property)

            val isTransient: Boolean = property.isTransient()
            val isNullable: Boolean = property.isNullable()
            val isRequired: Boolean = !isTransient && determineIfRequired(
                classKType = classKType,
                property = property,
                elementName = elementName
            )

            return MetadataDescriptor(
                name = elementName.name,
                attributes = attributes,
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
        private fun determineIfRequired(
            classKType: KType,
            property: KProperty1<out Any, *>,
            elementName: ElementName
        ): Boolean {
            tracer.debug("Determining if property is required: $property")

            return runCatching {
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
            }.getOrElse {
                // If there is an error (e.g., no serializer found)
                // fallback to checking the primary constructor.
                tracer.debug(
                    message = "Unable to determine if property is required by annotation or serializer. " +
                            "Falling back to primary constructor.",
                )

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
            return runCatching {
                kClass.primaryConstructor?.parameters?.find { argument ->
                    argument.name == property.name
                }?.isOptional == false // Assuming true if the parameter is not found.
            }.onFailure {
                tracer.error(
                    message = "Unable to determine if property is required by constructor. Field: $property",
                    cause = it
                )
            }.getOrDefault(defaultValue = true)  // Assuming true as a fallback.
        }
    }
}
