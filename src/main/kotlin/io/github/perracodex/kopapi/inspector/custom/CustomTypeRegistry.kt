/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.custom

import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.plugin.KopapiConfig
import io.github.perracodex.kopapi.plugin.dsl.builders.CustomTypeBuilder
import kotlin.reflect.KType

/**
 * Registry for user defined `custom types` to be used when generating the OpenAPI schema.
 * These can be new unhandled types or existing standard types with custom specifications.
 *
 * @see [KopapiConfig.addType]
 * @see [CustomTypeBuilder]
 * @see [CustomType]
 */
@PublishedApi
@TypeInspectorAPI
internal object CustomTypeRegistry {
    /** The set of all registered `custom types`. */
    private val registry: MutableSet<CustomType> = mutableSetOf()

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
     * Finds a `custom type` in the registry by its [KType].
     *
     * @param kType The [KType] to search for.
     * @return The [CustomType] instance if found, null otherwise.
     */
    fun find(kType: KType): CustomType? {
        return registry.find { it.type == kType }
    }
}
