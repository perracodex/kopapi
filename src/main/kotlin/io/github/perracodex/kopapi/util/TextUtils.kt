/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.util

/**
 * Trims a nullable string, returning the trimmed result if it is not blank,
 * or `null` if the string is `null` or the trimmed result is empty.
 *
 * @return The trimmed string if not blank, or `null` if the original string is `null` or blank after trimming.
 */
@PublishedApi
internal fun String?.trimOrNull(): String? {
    return this?.trim().takeIf { it?.isNotBlank() == true }
}

/**
 * Trims a nullable string, returning the trimmed result if it is not blank,
 * or a specified default value if the string is `null` or blank after trimming.
 *
 * @param defaultValue The default value to return if the string is `null` or blank after trimming.
 * @return The trimmed string if not blank, or the specified default value.
 */
internal fun String?.trimOrDefault(defaultValue: String): String {
    return this?.trimOrNull() ?: defaultValue
}


/**
 * Removes the specified suffix from the string in a case-insensitive manner.
 *
 * @param suffix The suffix to remove.
 * @return The string without the suffix if it was present; otherwise, the original string.
 */
@Suppress("unused")
internal fun String.removeSuffixIgnoreCase(suffix: String): String {
    return if (this.regionMatches(
            thisOffset = this.length - suffix.length,
            other = suffix,
            otherOffset = 0,
            length = suffix.length,
            ignoreCase = true
        )
    ) {
        this.substring(startIndex = 0, endIndex = this.length - suffix.length)
    } else {
        this
    }
}

/**
 * Sanitizes a string to ensure it adheres to OpenAPI specifications.
 *
 * Rules:
 * 1. If the string is entirely lowercase or entirely uppercase, replace spaces with underscores.
 * 2. If the string has mixed case:
 *    a. If the first character is lowercase, convert to camelCase by removing spaces and capitalizing subsequent words.
 *    b. If the first character is uppercase, convert to PascalCase by removing spaces and capitalizing all words.
 * 3. Replace any invalid characters with underscores.
 *
 * @return A sanitized string suitable for OpenAPI component keys.
 */
@PublishedApi
internal fun String.sanitize(): String {
    val trimmed: String = this.trim()
    if (trimmed.isBlank()) {
        return ""
    }

    // Determine if the string is all lowercase or all uppercase, ignoring non-letter characters.
    val letters: String = trimmed.filter { it.isLetter() }
    val isAllLowerCase: Boolean = letters.isNotEmpty() && letters.all { it.isLowerCase() }
    val isAllUpperCase: Boolean = letters.isNotEmpty() && letters.all { it.isUpperCase() }

    val processed: String = if (isAllLowerCase || isAllUpperCase) {
        // Replace one or more whitespace characters with a single underscore.
        trimmed.replace(regex = "\\s+".toRegex(), replacement = "_")
    } else {
        // Split the string into words based on whitespace.
        val words: List<String> = trimmed.split(regex = "\\s+".toRegex())

        // Check the case of the first character of the first word.
        if (words.first().firstOrNull()?.isLowerCase() == true) {
            // camelCase: first word lowercase, subsequent words capitalized.
            val firstWord: String = words.first().replaceFirstChar { it.lowercase() }
            val subsequentWords: String = words.drop(n = 1).joinToString(separator = "") {
                it.replaceFirstChar { char -> char.uppercase() }
            }
            firstWord + subsequentWords
        } else {
            // PascalCase: all words capitalized.
            words.joinToString(separator = "") {
                it.replaceFirstChar { char -> char.uppercase() }
            }
        }
    }

    // Replace any character not matching the allowed pattern with an underscore.
    val allowedPattern = Regex(pattern = "[a-zA-Z0-9._-]")
    return processed.map { char ->
        if (allowedPattern.matches(char.toString())) char else '_'
    }.joinToString(separator = "")
}
