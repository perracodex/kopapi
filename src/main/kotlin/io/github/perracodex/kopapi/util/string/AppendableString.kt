/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.util.string

import kotlin.reflect.KProperty

/**
 * A base class for strings that automatically append values with a specified separator.
 * This class uses delegated properties to enable direct assignment of string values.
 *
 * Note that assignment of a blank value will not append to the string,
 * and the value will remain unchanged. Use [clear] to reset the content of the string.
 *
 * @param initialValue Optional initial value of the string, which will be appended as the starting content.
 */
internal abstract class AppendableString(initialValue: String = "") : CharSequence {
    /** The internal string builder used to accumulate values. */
    protected val builder: StringBuilder = StringBuilder(initialValue)

    /**
     * Gets the current accumulated value of the string.
     * This operator function allows the use of delegated properties for retrieving the value.
     *
     * @param thisRef The reference to the object using the delegated property.
     * @param property The metadata for the property being delegated.
     * @return The current string value of the builder.
     */
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return builder.toString()
    }

    /**
     * Appends a new value to the existing string.
     * This operator function allows the use of delegated properties for setting the value.
     *
     * @param thisRef The reference to the object using the delegated property.
     * @param property The metadata for the property being delegated.
     * @param value The new string value to append. If the value is not blank, it will be appended.
     */
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        if (value.isNotBlank()) {
            invoke(value = value.trim())
        }
    }

    /**
     * Clears the current content and sets it to an empty string.
     */
    fun clear() {
        builder.clear()
    }

    /**
     * Appends the specified string [value] to the current builder content
     * using the specified separator logic.
     *
     * @param value The string to append.
     */
    protected abstract operator fun invoke(value: String)

    override val length: Int get() = builder.length

    override fun get(index: Int): Char = builder[index]

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = builder.subSequence(startIndex, endIndex)

    override fun toString(): String = builder.toString()
}
