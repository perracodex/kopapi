/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.resolver

import io.github.perracodex.kopapi.inspector.TypeInspector
import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.spec.Spec
import io.github.perracodex.kopapi.inspector.type.ElementMetadata
import io.github.perracodex.kopapi.inspector.type.TypeSchema
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType

/**
 * Provides functionality to resolve enum types into their corresponding [TypeSchema].
 *
 * Responsibilities:
 * - Extracting the names of enum constants from a given enum class.
 * - Creating a [TypeSchema] for the enum type, which includes the enum values.
 * - Caching the created [TypeSchema] to avoid redundant processing.
 */
@TypeInspectorAPI
internal object EnumResolver {
    /**
     * Processes the given [enumClass] and creates a [TypeSchema] for it.
     *
     * @param enumClass The [KClass] representing the enum type.
     * @return The resolved [TypeSchema] for the enum type.
     */
    fun process(enumClass: KClass<*>): TypeSchema {
        val enumValues: List<String> = enumClass.java.enumConstants?.map {
            (it as Enum<*>).name
        } ?: emptyList()

        // Create the TypeSchema for the enum as a separate object.
        val enumClassName: String = ElementMetadata.getClassName(kClass = enumClass)
        val enumKType: KType = enumClass.createType()

        // If the enum type has not been processed yet,
        // create a schema for it and cache it for future reference.
        if (!TypeInspector.isCached(kType = enumKType)) {
            val typeSchema: TypeSchema = TypeSchema.of(
                name = enumClassName,
                kType = enumKType,
                schema = Spec.enum(values = enumValues)
            )

            TypeInspector.addToCache(schema = typeSchema)
        }

        // Return a reference to the enum schema.
        return TypeSchema.of(
            name = enumClassName,
            kType = enumKType,
            schema = Spec.reference(schema = enumClassName)
        )
    }
}