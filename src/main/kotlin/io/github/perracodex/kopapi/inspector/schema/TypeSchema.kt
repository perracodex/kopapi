/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.schema

import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.descriptor.ElementName
import io.github.perracodex.kopapi.inspector.schema.factory.SchemaFactory
import io.github.perracodex.kopapi.inspector.utils.nativeName
import kotlin.reflect.KType

/**
 * Represents a type schema for an inspected type.
 *
 * @property name The current name of the type. If renamed, this reflects the updated name.
 * @property renamedFrom The original name of the type before renaming. It is `null` if the name was not changed.
 * @property type The fully qualified type name, typically obtained from [KType.nativeName].
 * @property schema The processed [Schema] specification for the type.
 */
internal data class TypeSchema private constructor(
    val name: String,
    val renamedFrom: String?,
    val type: String,
    val schema: Schema
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
         * @param name The name representing the type, usually the class or property name without qualifiers.
         * @param kType The [KType] representing the type.
         * @param schema The processed [Schema] specification for the type.
         * @return A new instance of [TypeSchema].
         */
        @TypeInspectorAPI
        fun of(name: ElementName, kType: KType, schema: Schema): TypeSchema {
            val qualifiedName: String = kType.nativeName()
            return TypeSchema(
                name = name.name,
                renamedFrom = name.renamedFrom,
                type = qualifiedName,
                schema = schema
            )
        }

        /**
         * Constructs a [TypeSchema] representing an unknown type.
         *
         * @param kType The unknown [KType] to build a schema for.
         * @return A new instance of [TypeSchema] representing the unknown type.
         */
        @TypeInspectorAPI
        fun ofUnknown(
            kType: KType,
        ): TypeSchema {
            return of(
                name = ElementName(name = "Unknown_$kType"),
                kType = kType,
                schema = SchemaFactory.ofObject()
            )
        }
    }
}
