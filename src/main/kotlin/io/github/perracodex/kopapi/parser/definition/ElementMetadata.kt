/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.parser.definition

import io.github.perracodex.kopapi.parser.annotation.ObjectTypeParserAPI
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.javaType

/**
 * Represents metadata for an element basic properties.
 *
 * @property name The name of the element.
 * @property originalName The original name of the element, or null if it is the same as [name].
 * @property isRequired Indicates whether the element is required. Defaults to true.
 * @property isTransient Indicates whether the element is transient and should be ignored. Defaults to false.
 */
@ObjectTypeParserAPI
internal data class ElementMetadata(
    val name: String,
    val originalName: String? = null,
    val isRequired: Boolean = true,
    val isTransient: Boolean = false
) {
    companion object {
        /**
         * Resolves the name for the give [kClass], considering serializer annotations if present.
         *
         * @param kClass The [KClass] to process for name resolution.
         * @return The resolved class name.
         */
        fun getClassName(kClass: KClass<*>): String {
            return geElementName(target = kClass).first
        }

        /**
         * Creates an [ElementMetadata] by parsing the annotations from the given [property].
         *
         * @param property The [KProperty1] to extract metadata from.
         * @return The constructed [ElementMetadata] instance.
         */
        @Suppress("DuplicatedCode")
        fun of(property: KProperty1<*, *>): ElementMetadata {
            val elementName: Pair<String, String?> = geElementName(target = property)

            val isTransient: Boolean = property.findAnnotation<kotlinx.serialization.Transient>() != null ||
                    property.findAnnotation<com.fasterxml.jackson.annotation.JsonIgnore>() != null

            val isRequired: Boolean = !property.returnType.isMarkedNullable &&
                    property.findAnnotation<com.fasterxml.jackson.annotation.JsonProperty>() == null

            return ElementMetadata(
                name = elementName.first,
                originalName = elementName.second,
                isRequired = isRequired,
                isTransient = isTransient
            )
        }

        /**
         * Resolves the name of the given [target], either from specific annotations if present,
         * or from the target's own name.
         *
         * @param target The target ([KClass] or [KProperty1]) to process for name resolution.
         * @return The resolved name from annotations or the target's own name if no annotation is found.
         * @throws IllegalArgumentException if the target is not a supported type.
         */
        private fun geElementName(target: Any): Pair<String, String?> {
            val originalName: String = when (target) {
                is KClass<*> -> target.safeName()
                is KProperty1<*, *> -> target.name
                else -> throw IllegalArgumentException("Unsupported target type: ${target::class.simpleName}")
            }

            // List of pairs containing annotation lookup functions and the way to extract the relevant value.
            val annotationCheckers: Set<(Any) -> String?> = setOf(
                { element ->
                    (element as? KClass<*>)?.findAnnotation<kotlinx.serialization.SerialName>()?.value
                },
                { element ->
                    (element as? KProperty1<*, *>)?.findAnnotation<kotlinx.serialization.SerialName>()?.value
                },
                { element ->
                    (element as? KClass<*>)?.findAnnotation<com.fasterxml.jackson.annotation.JsonTypeName>()?.value
                },
                { element ->
                    (element as? KProperty1<*, *>)?.findAnnotation<com.fasterxml.jackson.annotation.JsonProperty>()?.value
                }
            )

            // Iterate over the annotation checkers to find the first non-blank name.
            annotationCheckers.forEach { checker ->
                checker(target)?.let { serialName ->
                    if (serialName.isNotBlank() && serialName != originalName) {
                        return Pair(serialName, originalName)
                    }
                }
            }

            return Pair(originalName, null)
        }

        /**
         * Extension function to safely get a class name.
         * If The name cannot be determined, it creates a fallback name based on the class type.
         */
        private fun KClass<*>.safeName(): String {
            return this.simpleName
                ?: this.qualifiedName?.substringAfterLast(delimiter = '.')
                ?: "UnknownClass_${this.toString().replace(Regex(pattern = "[^A-Za-z0-9_]"), replacement = "_")}"
        }
    }
}

/**
 * Extension function to return the Java type name of a KType.
 */
@ObjectTypeParserAPI
internal fun KType.nativeName(): String {
    return this.javaType.typeName
}
