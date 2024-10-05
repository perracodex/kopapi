/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.spec

import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI

/**
 * Provides reusable entries for specifying types and structures in data definitions.
 */
@TypeInspectorAPI
internal object Spec {
    fun objectType(): MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.OBJECT()
    )

    fun string(): MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.STRING()
    )

    fun int32(): MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.INTEGER(),
        SpecKey.FORMAT() to SpecFormat.INT32()
    )

    fun int64(): MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.INTEGER(),
        SpecKey.FORMAT() to SpecFormat.INT64()
    )

    fun float(): MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.NUMBER(),
        SpecKey.FORMAT() to SpecFormat.FLOAT()
    )

    fun double(): MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.NUMBER(),
        SpecKey.FORMAT() to SpecFormat.DOUBLE()
    )

    fun uuid(): MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.STRING(),
        SpecKey.FORMAT() to SpecFormat.UUID()
    )

    fun date(): MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.STRING(),
        SpecKey.FORMAT() to SpecFormat.DATE()
    )

    fun dateTime(): MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.STRING(),
        SpecKey.FORMAT() to SpecFormat.DATETIME()
    )

    fun time(): MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.STRING(),
        SpecKey.FORMAT() to SpecFormat.TIME()
    )

    fun uri(): MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.STRING(),
        SpecKey.FORMAT() to SpecFormat.URI()
    )

    fun byte(): MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.STRING(),
        SpecKey.FORMAT() to SpecFormat.BYTE()
    )

    fun char(): MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.STRING(),
        SpecKey.MIN_LENGTH() to 1,
        SpecKey.MAX_LENGTH() to 1
    )

    fun boolean(): MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.BOOLEAN()
    )

    /**
     * Creates a specification entry for an enumeration of values.
     *
     * @param values The list of values to enumerate.
     * @return The specification entry for the enumeration.
     */
    fun enum(values: List<String>): MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.STRING(),
        SpecKey.ENUM() to values
    )

    /**
     * Creates a specification entry for an array of items.
     *
     * @param spec The type of items in the array.
     * @return The specification entry for the array.
     */
    fun array(spec: MutableMap<String, Any>): MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.ARRAY(),
        SpecKey.ITEMS() to spec
    )

    /**
     * Creates a specification entry for a collection of items (e.g., List or Set).
     *
     * @param value The type of items in the collection.
     * @return The specification entry for the collection.
     */
    fun collection(value: MutableMap<String, Any>): MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.ARRAY(),
        SpecKey.ITEMS() to value
    )

    /**
     * Creates a specification entry for an object with properties.
     *
     * @param value The map representing the properties of the object.
     * @return The specification entry for the object with properties.
     */
    fun properties(value: MutableMap<String, Any>): MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.OBJECT(),
        SpecKey.PROPERTIES() to value
    )

    /**
     * Creates a specification entry for an object type with additional properties.
     *
     * @param value The type definition for the additional properties.
     * @return A `MutableMap<String, Any>` representing the object with additional properties.
     */
    fun additionalProperties(value: MutableMap<String, Any>): MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.OBJECT(),
        SpecKey.ADDITIONAL_PROPERTIES() to value
    )

    /**
     * Creates a specification entry for a reference to a schema.
     *
     * @param schema The name of the schema to reference.
     * @return The specification entry for the schema reference.
     */
    fun reference(schema: String): MutableMap<String, Any> {
        return mutableMapOf(SpecKey.REFERENCE() to "#/components/schemas/$schema")
    }
}
