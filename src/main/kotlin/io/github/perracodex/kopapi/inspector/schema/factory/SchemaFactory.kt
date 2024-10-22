/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.schema.factory

import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.schema.ElementSchema
import io.github.perracodex.kopapi.types.ApiFormat
import io.github.perracodex.kopapi.types.ApiType

/**
 * Provides factory methods for creating [ElementSchema] instances.
 *
 * @see [ApiType]
 * @see [ApiFormat]
 */
@TypeInspectorAPI
internal object SchemaFactory {
    fun ofObject(): ElementSchema.Object {
        return ElementSchema.Object(properties = mutableMapOf())
    }

    fun ofString(): ElementSchema.Primitive {
        return ElementSchema.Primitive(schemaType = ApiType.STRING)
    }

    fun ofChar(): ElementSchema.Primitive {
        return ElementSchema.Primitive(schemaType = ApiType.STRING, minLength = 1, maxLength = 1)
    }

    fun ofInt32(): ElementSchema.Primitive {
        return ElementSchema.Primitive(schemaType = ApiType.INTEGER, format = ApiFormat.INT32())
    }

    fun ofInt64(): ElementSchema.Primitive {
        return ElementSchema.Primitive(schemaType = ApiType.INTEGER, format = ApiFormat.INT64())
    }

    fun ofFloat(): ElementSchema.Primitive {
        return ElementSchema.Primitive(schemaType = ApiType.NUMBER, format = ApiFormat.FLOAT())
    }

    fun ofDouble(): ElementSchema.Primitive {
        return ElementSchema.Primitive(schemaType = ApiType.NUMBER, format = ApiFormat.DOUBLE())
    }

    fun ofUuid(): ElementSchema.Primitive {
        return ElementSchema.Primitive(schemaType = ApiType.STRING, format = ApiFormat.UUID())
    }

    fun ofDate(): ElementSchema.Primitive {
        return ElementSchema.Primitive(schemaType = ApiType.STRING, format = ApiFormat.DATE())
    }

    fun ofDateTime(): ElementSchema.Primitive {
        return ElementSchema.Primitive(schemaType = ApiType.STRING, format = ApiFormat.DATETIME())
    }

    fun ofTime(): ElementSchema.Primitive {
        return ElementSchema.Primitive(schemaType = ApiType.STRING, format = ApiFormat.TIME())
    }

    fun ofUri(): ElementSchema.Primitive {
        return ElementSchema.Primitive(schemaType = ApiType.STRING, format = ApiFormat.URI())
    }

    fun ofByte(): ElementSchema.Primitive {
        return ElementSchema.Primitive(schemaType = ApiType.STRING, format = ApiFormat.BYTE())
    }

    fun ofBoolean(): ElementSchema.Primitive {
        return ElementSchema.Primitive(schemaType = ApiType.BOOLEAN)
    }

    /**
     * Creates a specification entry for an enumeration of values.
     *
     * @param values The list of values to enumerate.
     * @return The [ElementSchema.Enum] for the enumeration.
     */
    fun ofEnum(values: List<String>): ElementSchema.Enum {
        return ElementSchema.Enum(values = values)
    }

    /**
     * Creates a specification entry for an array of items.
     *
     * @param items The schema items in the collection.
     * @return  The [ElementSchema.Array] for the array.
     */
    fun ofArray(items: ElementSchema): ElementSchema.Array {
        return ElementSchema.Array(items = items)
    }

    /**
     * Creates a specification entry for a collection of items (e.g., `List` or `Set`).
     *
     * @param items The schema items in the collection.
     * @return The [ElementSchema.Array] for the collection.
     */
    fun ofCollection(items: ElementSchema): ElementSchema.Array {
        return ElementSchema.Array(items = items)
    }

    /**
     * Creates a specification entry for an object type with additional properties.
     *
     * @param value The type schema details to be set as the additional properties.
     * @return A [ElementSchema.AdditionalProperties] representing the object with additional properties.
     */
    fun ofAdditionalProperties(value: ElementSchema): ElementSchema.AdditionalProperties {
        return ElementSchema.AdditionalProperties(additionalProperties = value)
    }

    /**
     * Creates a specification entry for a reference to a schema.
     *
     * @param schemaName The name of the schema to reference.
     * @return The [ElementSchema.Reference]] instance for the schema reference.
     */
    fun ofReference(schemaName: String): ElementSchema.Reference {
        return ElementSchema.Reference(schemaName = schemaName)
    }
}
