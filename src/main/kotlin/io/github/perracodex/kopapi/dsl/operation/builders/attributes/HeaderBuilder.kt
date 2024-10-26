/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.attributes

import io.github.perracodex.kopapi.dsl.markers.OperationDsl
import io.github.perracodex.kopapi.dsl.operation.builders.response.ResponseBuilder
import io.github.perracodex.kopapi.dsl.operation.elements.ApiHeader
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.utils.io.*

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
@KtorDsl
@OperationDsl
public class HeaderBuilder(
    public val name: String,
    public var required: Boolean = false,
    public var deprecated: Boolean = false
) {
    public var description: String by MultilineString()

    init {
        if (name.isBlank()) {
            throw KopapiException("Header name must not be empty.")
        }
    }

    /**
     * Builds an [ApiHeader] instance from the current builder state.
     *
     * @return The constructed [ApiHeader] instance.
     */
    internal fun build(): ApiHeader {
        return ApiHeader(
            name = name.trim(),
            description = description.trimOrNull(),
            required = required,
            deprecated = deprecated.takeIf { it }
        )
    }
}
