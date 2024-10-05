/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.type

import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import kotlin.reflect.KType

/**
 * Represents a type schema for an inspected type.
 *
 * @param name The name representing the type. Usually the class or property name without qualifiers.
 * @param type The [KType] full qualified name.
 * @param schema The processed schema data for the type.
 */
internal data class TypeSchema private constructor(
    val name: String,
    val type: String,
    val schema: MutableMap<String, Any>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TypeSchema) return false
        return type == other.type
    }

    override fun hashCode(): Int {
        return 31 * type.hashCode()
    }

    companion object {
        /**
         * Factory method to create a [TypeSchema] instance.
         *
         * @param name The name representing the type. Usually the class or property name without qualifiers.
         * @param kType The [KType] full qualified name.
         * @param schema The processed schema data to add to the [TypeSchema].
         * @return A new instance of [TypeSchema].
         */
        @TypeInspectorAPI
        fun of(name: String, kType: KType, schema: MutableMap<String, Any>): TypeSchema {
            val qualifiedName: String = kType.nativeName()
            return TypeSchema(name = name, type = qualifiedName, schema = schema)
        }
    }
}
