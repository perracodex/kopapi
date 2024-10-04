/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.utils

/**
 * Trim a nullable string, returning null if the trimmed result is empty.
 */
@PublishedApi
internal fun String?.trimOrNull(): String? {
    return this?.trim().takeIf { it?.isNotBlank() == true }
}
