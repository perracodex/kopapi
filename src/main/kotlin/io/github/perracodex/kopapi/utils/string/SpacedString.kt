/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.utils.string

/**
 * A string that appends values with a space separator.
 * Useful for concatenating words or phrases separated by spaces.
 *
 * Note that assignment of a blank value will not append to the string,
 * and the value will remain unchanged. Use [clear] to reset the content of the string.
 *
 * @param initialValue Optional initial value of the string, which will be appended as the starting content.
 * @param spaceSeparator Optional value to use as the space separator. Defaults to a single space.
 */
internal class SpacedString(initialValue: String = "", private val spaceSeparator: String = " ") :
    AppendableString(initialValue = initialValue) {
    /**
     * Appends the specified string [value] to the current builder content with a space separator.
     * Ensures that a space is added only if the builder content is not empty and does not already end with a space.
     *
     * @param value The string to append to the builder.
     */
    override operator fun invoke(value: String) {
        builder.appendSpaced(value = value, separator = spaceSeparator)
    }
}
