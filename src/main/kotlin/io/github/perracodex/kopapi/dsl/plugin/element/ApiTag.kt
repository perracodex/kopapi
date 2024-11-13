/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.plugin.element

/**
 * Represents an OpenAPI tag object.
 *
 * @param name The name of the tag.
 * @param description An optional description of the tag.
 */
internal data class ApiTag(
    val name: String,
    val description: String? = null
)
