/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.parser.spec

/**
 * Provides reusable entries for specifying types and structures in data definitions.
 */
internal object Spec {
    val objectType: MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.OBJECT()
    )
    val string: MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.STRING()
    )
    val int32: MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.INTEGER(),
        SpecKey.FORMAT() to SpecFormat.INT32()
    )
    val int64: MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.INTEGER(),
        SpecKey.FORMAT() to SpecFormat.INT64()
    )
    val float: MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.NUMBER(),
        SpecKey.FORMAT() to SpecFormat.FLOAT()
    )
    val double: MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.NUMBER(),
        SpecKey.FORMAT() to SpecFormat.DOUBLE()
    )
    val uuid: MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.STRING(),
        SpecKey.FORMAT() to SpecFormat.UUID()
    )
    val date: MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.STRING(),
        SpecKey.FORMAT() to SpecFormat.DATE()
    )
    val dateTime: MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.STRING(),
        SpecKey.FORMAT() to SpecFormat.DATETIME()
    )
    val time: MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.STRING(),
        SpecKey.FORMAT() to SpecFormat.TIME()
    )
    val uri: MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.STRING(),
        SpecKey.FORMAT() to SpecFormat.URI()
    )
    val byte: MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.STRING(),
        SpecKey.FORMAT() to SpecFormat.BYTE()
    )
    val char: MutableMap<String, Any> = mutableMapOf(
        SpecKey.TYPE() to SpecType.STRING(),
        SpecKey.MIN_LENGTH() to 1,
        SpecKey.MAX_LENGTH() to 1
    )
    val boolean: MutableMap<String, Any> = mutableMapOf(
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
}
