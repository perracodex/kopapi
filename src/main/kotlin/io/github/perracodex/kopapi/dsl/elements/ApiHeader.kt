/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.elements

import io.github.perracodex.kopapi.utils.MultilineString

/**
 * Represents the metadata of a response header.
 *
 * @property name The name of the header.
 * @property description A human-readable description of the header.
 * @property required Indicates whether the header is mandatory.
 * @property deprecated Indicates whether the header is deprecated and should be avoided.
 *
 * @see [ApiResponse]
 */
public data class ApiHeader(
    val name: String,
    var required: Boolean = false,
    var deprecated: Boolean = false
) {
    init {
        require(name.isNotBlank()) { "Name must not be empty." }
    }

    public var description: String by MultilineString()
}
