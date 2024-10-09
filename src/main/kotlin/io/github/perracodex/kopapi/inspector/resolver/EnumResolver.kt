/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.resolver

import io.github.perracodex.kopapi.inspector.TypeSchemaResolver
import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.descriptor.MetadataDescriptor
import io.github.perracodex.kopapi.inspector.schema.TypeSchema
import io.github.perracodex.kopapi.inspector.schema.factory.SchemaFactory
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType

/**
 * - Purpose:
 *      - Handles enumeration types.
 * - Action:
 *      - Extract Enum Values: Retrieves the list of possible enum values.
 *      - Construct Schema: Builds the enum schema, including the list of values.
 *      - Caching: Adds the schema to the `TypeSchemaResolver` cache if not already present.
 *      - Result: Constructs and returns the enum schema.
 *
 * @see [TypeSchemaResolver]
 */
@TypeInspectorAPI
internal class EnumResolver(private val typeSchemaResolver: TypeSchemaResolver) {
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
        val enumClassName: String = MetadataDescriptor.getClassName(kClass = enumClass)
        val enumKType: KType = enumClass.createType()

        // If the enum type has not been processed yet,
        // create a schema for it and cache it for future reference.
        if (!typeSchemaResolver.isCached(kType = enumKType)) {
            val typeSchema: TypeSchema = TypeSchema.of(
                name = enumClassName,
                kType = enumKType,
                schema = SchemaFactory.ofEnum(values = enumValues)
            )

            typeSchemaResolver.addToCache(schema = typeSchema)
        }

        // Return a reference to the enum schema.
        return TypeSchema.of(
            name = enumClassName,
            kType = enumKType,
            schema = SchemaFactory.ofReference(schemaName = enumClassName)
        )
    }
}
