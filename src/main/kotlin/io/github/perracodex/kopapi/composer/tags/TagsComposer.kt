/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.tags

import io.github.perracodex.kopapi.composer.OpenAPiSchema
import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.github.perracodex.kopapi.dsl.operation.elements.ApiOperation
import java.util.*

/**
 * Composes the `Tags` section of the OpenAPI schema.
 */
@ComposerAPI
internal class TagsComposer(
    private val apiOperations: Set<ApiOperation>
) {
    /**
     * Collects all tags from the API operations.
     *
     * @return A list of string tags, sorted alphabetically. `null` if no tags are found.
     */
    fun compose(): List<OpenAPiSchema.Tag>? {
        val allTags: SortedSet<String> = apiOperations.flatMap { apiOperations ->
            apiOperations.tags ?: emptySet()
        }.distinct().toSortedSet()

        return allTags.map {
            OpenAPiSchema.Tag(name = it)
        }.takeIf { it.isNotEmpty() }
    }
}
