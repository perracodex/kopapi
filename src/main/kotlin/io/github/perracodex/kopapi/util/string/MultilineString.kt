/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.util.string

/**
 * A string that appends values with a newline separator.
 * Ideal for constructing multi-line text where each value appears on a new line.
 *
 * Note that assignment of a blank value will not append to the string,
 * and the value will remain unchanged. Use [clear] to reset the content of the string.
 *
 * @param initialValue Optional initial value of the string, which will be appended as the starting content.
 * @param newlineSeparator Optional value to use as the newline separator. Defaults to the Unix newline character.
 */
internal class MultilineString(initialValue: String = "", private val newlineSeparator: String = "\n") :
    AppendableString(initialValue = initialValue) {
    /**
     * Appends the specified string [value] to the current builder content with a newline separator.
     * Ensures that a newline is added only if the builder content is not empty and does not already end with a newline.
     *
     * @param value The string to append to the builder.
     */
    override operator fun invoke(value: String) {
        builder.appendLine(value = value, newlineSeparator = newlineSeparator)
    }
}
