/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.utils

/**
 * Appends the specified string [value] to this instance,
 * preceded by [separator] only if the builder is not empty
 * and does not already end with the specified [separator].
 *
 * @param value The string to append.
 * @param separator The separator to use before appending [value]. Defaults to a single space.
 * @return This instance with the value appended.
 */
private fun StringBuilder.appendWithSeparator(value: String, separator: String = " "): StringBuilder {
    val end: Int = this.length
    val sepLength: Int = separator.length
    val startsWithSeparator: Boolean = (end >= sepLength)
            && (this.substring(startIndex = end - sepLength, endIndex = end) == separator)

    if (this.isNotEmpty() && !startsWithSeparator) {
        this.append(separator)
    }

    this.append(value)
    return this
}

/**
 * Appends the specified string [value], preceding a space
 * unless the builder is empty or already has a preceding space.
 *
 * @param value The string to append.
 * @param separator The separator to use before appending [value]. Defaults to a single space.
 * @return This instance with the value appended.
 */
@PublishedApi
internal fun StringBuilder.appendSpaced(value: String, separator: String = " "): StringBuilder =
    appendWithSeparator(value = value, separator = separator)

/**
 * Appends the specified string [value], preceding a newline
 * unless the builder is empty or already has a preceding newline.
 *
 * @param value The string to append.
 * @param newlineSeparator The value to use as the newline separator. Defaults to the Unix newline character.
 * @return This instance with the value appended.
 */
@PublishedApi
internal fun StringBuilder.appendLine(value: String, newlineSeparator: String = "\n"): StringBuilder =
    appendWithSeparator(value = value, separator = newlineSeparator)
