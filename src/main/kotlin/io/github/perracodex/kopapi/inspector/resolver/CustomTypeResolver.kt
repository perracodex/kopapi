/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.resolver

import io.github.perracodex.kopapi.inspector.TypeResolver
import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.custom.CustomType
import io.github.perracodex.kopapi.inspector.custom.CustomTypeRegistry
import io.github.perracodex.kopapi.inspector.spec.Spec
import io.github.perracodex.kopapi.inspector.spec.SpecKey
import io.github.perracodex.kopapi.inspector.type.TypeSchema
import io.github.perracodex.kopapi.inspector.type.safeName
import io.github.perracodex.kopapi.utils.Tracer
import kotlin.reflect.KType

/**
 * Resolves user defined [CustomType] objects, into their corresponding [TypeSchema].
 *
 * Responsibilities:
 * - Verify if the custom type is registered, and log an error if not.
 * - Create and cache a schema for the custom type for future reference.
 * - Return a reference to the custom type schema.
 *
 * @see [CustomType]
 * @see [TypeResolver]
 */
@TypeInspectorAPI
internal class CustomTypeResolver(private val typeResolver: TypeResolver) {
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
                    schema = Spec.objectType()
                )
            }

        // If the custom type has not been processed yet,
        // create a schema for it and cache it for future reference.
        if (!typeResolver.isCached(kType = kType)) {
            val schema: MutableMap<String, Any> = mutableMapOf(SpecKey.TYPE() to customType.specType)
            customType.specFormat?.let { schema[SpecKey.FORMAT()] = customType.specFormat }
            customType.minLength?.let { schema[SpecKey.MIN_LENGTH()] = customType.minLength }
            customType.maxLength?.let { schema[SpecKey.MAX_LENGTH()] = customType.maxLength }
            customType.additional?.let { schema.putAll(customType.additional) }
            customType.minimum?.let { schema[SpecKey.MINIMUM()] = customType.minimum }
            customType.maximum?.let { schema[SpecKey.MAXIMUM()] = customType.maximum }
            customType.exclusiveMinimum?.let { schema[SpecKey.EXCLUSIVE_MINIMUM()] = customType.exclusiveMinimum }
            customType.exclusiveMaximum?.let { schema[SpecKey.EXCLUSIVE_MAXIMUM()] = customType.exclusiveMaximum }
            customType.multipleOf?.let { schema[SpecKey.MULTIPLE_OF()] = customType.multipleOf }

            val schemaType: TypeSchema = TypeSchema.of(
                name = typeName,
                kType = kType,
                schema = schema
            )

            typeResolver.addToCache(schema = schemaType)
        }

        // Return a reference to the custom type schema.
        return TypeSchema.of(
            name = typeName,
            kType = kType,
            schema = Spec.reference(schema = typeName)
        )
    }
}
