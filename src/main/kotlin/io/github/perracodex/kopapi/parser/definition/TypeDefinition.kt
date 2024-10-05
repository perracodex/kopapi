/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.parser.definition

import io.github.perracodex.kopapi.parser.annotation.TypeInspectorAPI
import kotlin.reflect.KType

/**
 * Data class type definitions for components.
 *
 * @param name The unique name representing the type.
 * @param type The [KType] full qualified name.
 * @param definition The type definition as a map.
 */
internal data class TypeDefinition private constructor(
    val name: String,
    val type: String,
    val definition: MutableMap<String, Any>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TypeDefinition) return false
        return type == other.type
    }

    override fun hashCode(): Int {
        return 31 * type.hashCode()
    }

    companion object {
        /**
         * Factory method to create a [TypeDefinition] instance.
         *
         * @param name The name representing the type.
         * @param kType The [KType] full qualified name.
         * @param definition The type definition as a map.
         * @return A new instance of [TypeDefinition].
         */
        @TypeInspectorAPI
        fun of(name: String, kType: KType, definition: MutableMap<String, Any>): TypeDefinition {
            val qualifiedName: String = kType.nativeName()
            return TypeDefinition(name = name, type = qualifiedName, definition = definition)
        }
    }
}
