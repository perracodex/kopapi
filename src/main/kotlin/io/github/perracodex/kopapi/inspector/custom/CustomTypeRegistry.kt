/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.custom

import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.spec.SpecKey
import io.github.perracodex.kopapi.inspector.type.TypeDescriptor
import io.github.perracodex.kopapi.inspector.type.TypeSchema
import io.github.perracodex.kopapi.inspector.type.safeName
import io.github.perracodex.kopapi.plugin.KopapiConfig
import io.github.perracodex.kopapi.plugin.builders.CustomTypeBuilder
import io.github.perracodex.kopapi.utils.Tracer
import kotlin.reflect.KType

/**
 * Registry for user defined `custom types` to be used when generating the OpenAPI schema.
 * These can be new unhandled types or existing standard types with custom specifications.
 *
 * @see [KopapiConfig.customType]
 * @see [CustomTypeBuilder]
 * @see [CustomType]
 */
@PublishedApi
internal object CustomTypeRegistry {
    private val tracer = Tracer<CustomTypeRegistry>()

    /** The set of all registered `custom types`. */
    private val registry: MutableSet<CustomType> = mutableSetOf()

    /** Custom types prefix. used to compose [TypeSchema] names. */
    private const val PREFIX = "CUSTOM_TYPE_"

    /**
     * Adds a new `custom types` into the registry.
     *
     * @param customType The [CustomType] instance to be added to the registry.
     */
    fun register(customType: CustomType) {
        registry.add(customType)
    }

    /**
     * Checks if the given [KType] is registered as a `custom type` in the registry.
     *
     * @param kType The target [KType] to check.
     * @return True if [kType] is registered as a `custom type`, false otherwise.
     */
    fun isCustomType(kType: KType): Boolean {
        return registry.any { it.type == kType }
    }

    /**
     * Builds the [TypeSchema] for a given [KType].
     * If the [kType] was not registered it falls back to creating an `unknown object` [TypeSchema].
     *
     * @param kType The [KType] for which to create the [TypeSchema].
     * @return A new [TypeSchema] instance for the given [kType].
     */
    @TypeInspectorAPI
    fun getTypeSchema(kType: KType): TypeSchema {
        registry.find { it.type == kType }?.let { customType ->
            val schema: MutableMap<String, Any> = mutableMapOf(SpecKey.TYPE() to customType.specType)
            customType.specFormat?.let { schema[SpecKey.FORMAT()] = it }
            val name = "$PREFIX${kType.safeName()}"

            return TypeSchema.of(
                name = name,
                kType = kType,
                schema = schema
            )
        } ?: run {
            tracer.error("Custom type not found for $kType.")
            return TypeDescriptor.buildUnknownTypeSchema(kType)
        }
    }
}
