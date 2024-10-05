/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.parser.resolver

import io.github.perracodex.kopapi.parser.TypeInspector
import io.github.perracodex.kopapi.parser.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.parser.definition.ElementMetadata
import io.github.perracodex.kopapi.parser.definition.TypeDefinition
import io.github.perracodex.kopapi.parser.spec.Spec
import kotlin.reflect.KClass
import kotlin.reflect.full.createType

/**
 * Provides functionality to parse and resolve enum types into their corresponding [TypeDefinition].
 *
 * Responsibilities:
 * - Extracting the names of enum constants from a given enum class.
 * - Creating a [TypeDefinition] for the enum type, which includes the enum values.
 * - Caching the created [TypeDefinition] to avoid redundant processing.
 */
@TypeInspectorAPI
internal object EnumResolver {
    /**
     * Processes the given [enumClass] and creates a [TypeDefinition] for it.
     *
     * @param enumClass The [KClass] representing the enum type.
     * @return The resolved [TypeDefinition] for the enum type.
     */
    fun process(enumClass: KClass<*>): TypeDefinition {
        val enumValues: List<String> = enumClass.java.enumConstants?.map {
            (it as Enum<*>).name
        } ?: emptyList()

        // Create the TypeDefinition for the enum as a separate object.
        val enumClassName: String = ElementMetadata.getClassName(kClass = enumClass)
        val definition: TypeDefinition = TypeDefinition.of(
            name = enumClassName,
            kType = enumClass.createType(),
            definition = Spec.enum(values = enumValues)
        )

        // Add the enum definition to the object definitions if it's not already present.
        TypeInspector.addToCache(definition = definition)

        // Return a reference to the enum definition
        return TypeDefinition.of(
            name = enumClassName,
            kType = enumClass.createType(),
            definition = Spec.reference(schema = enumClassName)
        )
    }
}
