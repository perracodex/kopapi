/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.schema.factory

import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.schema.Schema
import io.github.perracodex.kopapi.types.ApiFormat
import io.github.perracodex.kopapi.types.ApiType

/**
 * Provides factory methods for creating [Schema] instances.
 *
 * @see [ApiType]
 * @see [ApiFormat]
 */
@TypeInspectorAPI
internal object SchemaFactory {
    fun ofObject(): Schema.Object {
        return Schema.Object(properties = mutableMapOf())
    }

    fun ofString(): Schema.Primitive {
        return Schema.Primitive(schemaType = ApiType.STRING)
    }

    fun ofChar(): Schema.Primitive {
        return Schema.Primitive(schemaType = ApiType.STRING, minLength = 1, maxLength = 1)
    }

    fun ofInt32(): Schema.Primitive {
        return Schema.Primitive(schemaType = ApiType.INTEGER, format = ApiFormat.INT32())
    }

    fun ofInt64(): Schema.Primitive {
        return Schema.Primitive(schemaType = ApiType.INTEGER, format = ApiFormat.INT64())
    }

    fun ofFloat(): Schema.Primitive {
        return Schema.Primitive(schemaType = ApiType.NUMBER, format = ApiFormat.FLOAT())
    }

    fun ofDouble(): Schema.Primitive {
        return Schema.Primitive(schemaType = ApiType.NUMBER, format = ApiFormat.DOUBLE())
    }

    fun ofUuid(): Schema.Primitive {
        return Schema.Primitive(schemaType = ApiType.STRING, format = ApiFormat.UUID())
    }

    fun ofDate(): Schema.Primitive {
        return Schema.Primitive(schemaType = ApiType.STRING, format = ApiFormat.DATE())
    }

    fun ofDateTime(): Schema.Primitive {
        return Schema.Primitive(schemaType = ApiType.STRING, format = ApiFormat.DATETIME())
    }

    fun ofTime(): Schema.Primitive {
        return Schema.Primitive(schemaType = ApiType.STRING, format = ApiFormat.TIME())
    }

    fun ofUri(): Schema.Primitive {
        return Schema.Primitive(schemaType = ApiType.STRING, format = ApiFormat.URI())
    }

    fun ofByte(): Schema.Primitive {
        return Schema.Primitive(schemaType = ApiType.STRING, format = ApiFormat.BYTE())
    }

    fun ofBoolean(): Schema.Primitive {
        return Schema.Primitive(schemaType = ApiType.BOOLEAN)
    }

    /**
     * Creates a specification entry for an enumeration of values.
     *
     * @param values The list of values to enumerate.
     * @return The [Schema.Enum] for the enumeration.
     */
    fun ofEnum(values: List<String>): Schema.Enum {
        return Schema.Enum(values = values)
    }

    /**
     * Creates a specification entry for an array of items.
     *
     * @param items The schema items in the collection.
     * @return  The [Schema.Array] for the array.
     */
    fun ofArray(items: Schema): Schema.Array {
        return Schema.Array(items = items)
    }

    /**
     * Creates a specification entry for a collection of items (e.g., `List` or `Set`).
     *
     * @param items The schema items in the collection.
     * @return The [Schema.Array] for the collection.
     */
    fun ofCollection(items: Schema): Schema.Array {
        return Schema.Array(items = items)
    }

    /**
     * Creates a specification entry for an object type with additional properties.
     *
     * @param value The type schema details to be set as the additional properties.
     * @return A [Schema.AdditionalProperties] representing the object with additional properties.
     */
    fun ofAdditionalProperties(value: Schema): Schema.AdditionalProperties {
        return Schema.AdditionalProperties(additionalProperties = value)
    }

    /**
     * Creates a specification entry for a reference to a schema.
     *
     * @param schemaName The name of the schema to reference.
     * @return The [Schema.Reference]] instance for the schema reference.
     */
    fun ofReference(schemaName: String): Schema.Reference {
        return Schema.Reference(schemaName = schemaName)
    }
}
