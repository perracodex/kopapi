/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.schema.factory

import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorApi
import io.github.perracodex.kopapi.schema.facets.ElementSchema
import io.github.perracodex.kopapi.types.ApiFormat
import io.github.perracodex.kopapi.types.ApiType

/**
 * Provides factory methods for creating [ElementSchema] instances.
 *
 * @see [ApiType]
 * @see [ApiFormat]
 */
@TypeInspectorApi
internal object SchemaFactory {
    /** Creates a specification entry for an `object` type. */
    fun ofObject(): ElementSchema.Object {
        return ElementSchema.Object(objectProperties = mutableMapOf())
    }

    /** Creates a specification entry for a `string` primitive type. */
    fun ofString(): ElementSchema.Primitive {
        return ElementSchema.Primitive(schemaType = ApiType.STRING, format = null)
    }

    /** Creates a specification entry for a `char` primitive type. */
    fun ofChar(): ElementSchema.Primitive {
        return ElementSchema.Primitive(schemaType = ApiType.STRING, format = null, minLength = 1, maxLength = 1)
    }

    /** Creates a specification entry for an `integer` primitive type. */
    fun ofInt32(): ElementSchema.Primitive {
        return ElementSchema.Primitive(schemaType = ApiType.INTEGER, format = ApiFormat.INT32())
    }

    /** Creates a specification entry for a `long` primitive type. */
    fun ofInt64(): ElementSchema.Primitive {
        return ElementSchema.Primitive(schemaType = ApiType.INTEGER, format = ApiFormat.INT64())
    }

    /** Creates a specification entry for a `float` primitive type. */
    fun ofFloat(): ElementSchema.Primitive {
        return ElementSchema.Primitive(schemaType = ApiType.NUMBER, format = ApiFormat.FLOAT())
    }

    /** Creates a specification entry for a `double` primitive type. */
    fun ofDouble(): ElementSchema.Primitive {
        return ElementSchema.Primitive(schemaType = ApiType.NUMBER, format = ApiFormat.DOUBLE())
    }

    /** Creates a specification entry for a `uuid` primitive type. */
    fun ofUuid(): ElementSchema.Primitive {
        return ElementSchema.Primitive(schemaType = ApiType.STRING, format = ApiFormat.UUID())
    }

    /** Creates a specification entry for a `date` primitive type. */
    fun ofDate(): ElementSchema.Primitive {
        return ElementSchema.Primitive(schemaType = ApiType.STRING, format = ApiFormat.DATE())
    }

    /** Creates a specification entry for a `time` primitive type. */
    fun ofDateTime(): ElementSchema.Primitive {
        return ElementSchema.Primitive(schemaType = ApiType.STRING, format = ApiFormat.DATETIME())
    }

    /** Creates a specification entry for a `time` primitive type. */
    fun ofTime(): ElementSchema.Primitive {
        return ElementSchema.Primitive(schemaType = ApiType.STRING, format = ApiFormat.TIME())
    }

    /** Creates a specification entry for a `uri` primitive type. */
    fun ofUri(): ElementSchema.Primitive {
        return ElementSchema.Primitive(schemaType = ApiType.STRING, format = ApiFormat.URI())
    }

    /** Creates a specification entry for a `byte` primitive type. */
    fun ofByte(): ElementSchema.Primitive {
        return ElementSchema.Primitive(schemaType = ApiType.STRING, format = ApiFormat.BYTE())
    }

    /** Creates a specification entry for a `boolean` primitive type. */
    fun ofBoolean(): ElementSchema.Primitive {
        return ElementSchema.Primitive(schemaType = ApiType.BOOLEAN, format = null)
    }

    /**
     * Creates a specification entry for an `enumeration` of values.
     *
     * @param values The list of values to enumerate.
     * @return The [ElementSchema.Enum] for the enumeration.
     */
    fun ofEnum(values: List<String>): ElementSchema.Enum {
        return ElementSchema.Enum(values = values)
    }

    /**
     * Creates a specification entry for an `array` of items.
     *
     * @param items The schema items in the collection.
     * @return  The [ElementSchema.Array] for the array.
     */
    fun ofArray(items: ElementSchema): ElementSchema.Array {
        return ElementSchema.Array(items = items)
    }

    /**
     * Creates a specification entry for a `collection` of items (e.g., `List` or `Set`).
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
     * Creates a specification entry for a `reference` to a schema.
     *
     * @param schemaName The name of the schema to reference.
     * @return The [ElementSchema.Reference]] instance for the schema reference.
     */
    fun ofReference(schemaName: String): ElementSchema.Reference {
        return ElementSchema.Reference(schemaName = schemaName)
    }
}
