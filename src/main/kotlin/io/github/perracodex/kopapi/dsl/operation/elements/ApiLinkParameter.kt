/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.elements

import io.github.perracodex.kopapi.system.KopapiException

/**
 * Represents the metadata of an [ApiLink] parameter.
 *
 * @property name The name of the parameter.
 * @property value The value of the parameter.
 *
 * @see [ApiLink]
 */
internal data class ApiLinkParameter(
    val name: String,
    val value: String
) {
    init {
        if (name.isBlank()) {
            throw KopapiException("Link parameter name must not be empty.")
        }
        if (value.isBlank()) {
            throw KopapiException("Link parameter value must not be empty.")
        }
    }
}
