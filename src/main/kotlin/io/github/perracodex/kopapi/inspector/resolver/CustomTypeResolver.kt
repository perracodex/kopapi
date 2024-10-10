/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.resolver

import io.github.perracodex.kopapi.inspector.TypeSchemaBuilder
import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.custom.CustomType
import io.github.perracodex.kopapi.inspector.custom.CustomTypeRegistry
import io.github.perracodex.kopapi.inspector.schema.Schema
import io.github.perracodex.kopapi.inspector.schema.TypeSchema
import io.github.perracodex.kopapi.inspector.schema.factory.SchemaFactory
import io.github.perracodex.kopapi.inspector.utils.safeName
import io.github.perracodex.kopapi.utils.Tracer
import io.github.perracodex.kopapi.utils.trimOrNull
import kotlin.reflect.KType

/**
 * - Purpose:
 *      - Processes user-defined custom types.
 * - Action:
 *      - Check Registration: Determines if the type is registered as a [CustomType].
 *      - Construct Schema: If registered, constructs the custom schema.
 *      - Caching: Adds the schema to the `TypeSchemaBuilder` cache to avoid redundant processing.
 *      - Result: Returns the constructed schema.
 *
 * @see [CustomType]
 * @see [TypeSchemaBuilder]
 */
@TypeInspectorAPI
internal class CustomTypeResolver(private val typeSchemaBuilder: TypeSchemaBuilder) {
    private val tracer = Tracer<CustomTypeResolver>()

    /**
     * Process a user defined [CustomType] object.
     * It is expected this method is called if the [KType] is an already registered custom type.
     * If not registered, an error will be logged and a basic object schema will be returned.
     *
     * @param kType The [KType] representing the collection type.
     * @return The resolved [TypeSchema] for the custom type.
     */
    fun process(kType: KType): TypeSchema {
        val typeName = "CustomTypeOf${kType.safeName()}"

        // If attempting to resolve a custom type that does not exist, log an error
        // and return a basic object schema.
        val customType: CustomType = CustomTypeRegistry.find(kType = kType)
            ?: run {
                tracer.error("No custom type found for $kType")
                return TypeSchema.of(
                    name = typeName,
                    kType = kType,
                    schema = SchemaFactory.ofObject()
                )
            }

        // If the custom type has not been processed yet, create a schema for it and cache it.
        if (!typeSchemaBuilder.isCached(kType = kType)) {
            // Map customType.dataType and customType.dataFormat to the Schema.Primitive fields.
            val primitiveSchema: Schema.Primitive = Schema.Primitive(
                type = customType.dataType,
                format = customType.dataFormat.trimOrNull(),
                minLength = customType.minLength,
                maxLength = customType.maxLength,
                minimum = customType.minimum,
                maximum = customType.maximum,
                exclusiveMinimum = customType.exclusiveMinimum,
                exclusiveMaximum = customType.exclusiveMaximum,
                multipleOf = customType.multipleOf,
            )

            val schemaType: TypeSchema = TypeSchema.of(
                name = typeName,
                kType = kType,
                schema = primitiveSchema
            )

            typeSchemaBuilder.addToCache(schema = schemaType)
        }

        // Return a reference to the custom type schema.
        return TypeSchema.of(
            name = typeName,
            kType = kType,
            schema = SchemaFactory.ofReference(schemaName = typeName)
        )
    }
}
