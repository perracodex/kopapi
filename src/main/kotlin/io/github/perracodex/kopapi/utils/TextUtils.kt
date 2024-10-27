/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.utils

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
 * Sanitizes the name of the component key to ensure it adheres to OpenAPI specifications.
 *
 * Rules:
 * 1. If the string is entirely lowercase or entirely uppercase, replace spaces with underscores.
 * 2. If the string has mixed case, remove all spaces to produce CamelCase.
 * 3. Replace any invalid characters with underscores.
 *
 * @return A sanitized string suitable for OpenAPI component keys.
 */
internal fun String.normalizeComponentKey(): String {
    val trimmed: String = this.trim()
    if (trimmed.isBlank()) {
        return ""
    }

    // Determine if the string is all lowercase or all uppercase, ignoring non-letter characters.
    val letters: String = trimmed.filter { it.isLetter() }
    val isAllLowerCase: Boolean = letters.isNotEmpty() && letters.all { it.isLowerCase() }
    val isAllUpperCase: Boolean = letters.isNotEmpty() && letters.all { it.isUpperCase() }

    // Process based on the case.
    val processed: String = when {
        isAllLowerCase || isAllUpperCase -> {
            // Replace one or more whitespace characters with a single underscore.
            trimmed.replace(regex = "\\s+".toRegex(), replacement = "_")
        }

        else -> {
            // For mixed case, remove all whitespace to produce CamelCase.
            trimmed.split(regex = "\\s+".toRegex())
                .joinToString(separator = "") { word ->
                    word.replaceFirstChar { letter ->
                        if (letter.isLowerCase()) letter.titlecase() else letter.toString()
                    }
                }
        }
    }

    // Replace any character not matching the allowed pattern with an underscore.
    val allowedPattern = Regex(pattern = "[a-zA-Z0-9._-]")
    return processed.map {
        if (allowedPattern.matches(it.toString())) it else '_'
    }.joinToString(separator = "")
}
