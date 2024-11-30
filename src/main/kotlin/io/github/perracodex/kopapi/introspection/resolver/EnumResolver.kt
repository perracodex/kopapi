/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.introspection.resolver

import io.github.perracodex.kopapi.introspection.TypeIntrospector
import io.github.perracodex.kopapi.introspection.annotation.TypeIntrospectorApi
import io.github.perracodex.kopapi.introspection.descriptor.ElementName
import io.github.perracodex.kopapi.introspection.descriptor.MetadataDescriptor
import io.github.perracodex.kopapi.introspection.schema.TypeSchema
import io.github.perracodex.kopapi.introspection.schema.factory.SchemaFactory
import io.github.perracodex.kopapi.system.Tracer
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType

/**
 * - Purpose:
 *      - Handles enumeration types.
 * - Action:
 *      - Extract Enum Values: Retrieves the list of possible enum values.
 *      - Construct Schema: Builds the enum schema, including the list of values.
 *      - Caching: Adds the schema to the `TypeIntrospector` cache if not already present.
 *      - Result: Constructs and returns the enum schema.
 *
 * @see [TypeIntrospector]
 */
@TypeIntrospectorApi
internal class EnumResolver(private val introspector: TypeIntrospector) {
    private val tracer: Tracer = Tracer<EnumResolver>()

    /**
     * Processes the given [enumClass] and creates a [TypeSchema] for it.
     *
     * @param enumClass The [KClass] representing the enum type.
     * @return The resolved [TypeSchema] for the enum type.
     */
    fun process(enumClass: KClass<*>): TypeSchema {
        tracer.debug("Processing enum type: $enumClass.")

        val enumValues: List<String> = enumClass.java.enumConstants?.map {
            (it as Enum<*>).name
        } ?: emptyList()

        // Create the TypeSchema for the enum as a separate object.
        val enumClassName: ElementName = MetadataDescriptor.getClassName(kClass = enumClass)
        val enumKType: KType = enumClass.createType()

        // If the enum type has not been processed yet,
        // create a schema for it and cache it for future reference.
        if (!introspector.isCached(kType = enumKType)) {
            tracer.debug("Creating schema for enum type: $enumClass.")

            val typeSchema: TypeSchema = TypeSchema.of(
                name = enumClassName,
                kType = enumKType,
                schema = SchemaFactory.ofEnum(values = enumValues)
            )

            introspector.addToCache(schema = typeSchema)
        }

        // Return a reference to the enum schema.
        return TypeSchema.of(
            name = enumClassName,
            kType = enumKType,
            schema = SchemaFactory.ofReference(schemaName = enumClassName.name, referencedType = enumKType)
        )
    }
}
