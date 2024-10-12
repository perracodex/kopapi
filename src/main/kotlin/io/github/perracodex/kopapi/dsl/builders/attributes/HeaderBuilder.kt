/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.builders.attributes

import io.github.perracodex.kopapi.dsl.builders.response.ResponseBuilder
import io.github.perracodex.kopapi.dsl.elements.ApiHeader
import io.github.perracodex.kopapi.utils.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull

/**
 * Builds a response header for an API endpoint.
 *
 * @property name The name of the header.
 * @property description A human-readable description of the header.
 * @property required Indicates whether the header is mandatory.
 * @property deprecated Indicates whether the header is deprecated and should be avoided.
 *
 * @see [ResponseBuilder]
 * @see [HeaderBuilder]
 */
public data class HeaderBuilder(
    val name: String,
    var required: Boolean = false,
    var deprecated: Boolean = false
) {
    init {
        require(name.isNotBlank()) { "Name must not be empty." }
    }

    public var description: String by MultilineString()

    /**
     * Builds an [ApiHeader] instance from the current builder state.
     *
     * @return The constructed [ApiHeader] instance.
     */
    internal fun build(): ApiHeader {
        return ApiHeader(
            name = name,
            description = description.trimOrNull(),
            required = required,
            deprecated = deprecated
        )
    }
}
