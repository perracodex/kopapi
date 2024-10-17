/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.tags

import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.github.perracodex.kopapi.dsl.operation.elements.ApiOperation
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiConfiguration
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiTag
import java.util.*

/**
 * Composes the `Tags` section of the OpenAPI schema.
 */
@ComposerAPI
internal class TagsComposer(
    private val apiConfiguration: ApiConfiguration,
    private val apiOperations: Set<ApiOperation>
) {
    /**
     * Collects and merges tags from both API operations and top-level configuration.
     * Prioritizes top-level tags for descriptions and ensures uniqueness by name (case-insensitive).
     *
     * @return A list of [ApiTag] items, sorted alphabetically by name, possibly empty.
     */
    fun compose(): List<ApiTag>? {
        // Collect tags from API operations, ignoring descriptions.
        val operationTags: Map<String, ApiTag> = collectOperationTags()
            .associateBy({ it.name.lowercase() }, { it })

        // Collect tags from top-level configuration, which may include descriptions.
        val configTags: Map<String, ApiTag> = collectConfigurationTags()
            .associateBy({ it.name.lowercase() }, { it })

        // Merge operation tags into config tags to prioritize top-level definitions.
        val mergedTags: SortedSet<ApiTag> = configTags.toMutableMap().apply {
            putAll(operationTags)  // Adds operation tags that aren't already defined at the top level.
        }.values.toSortedSet(compareBy { it.name })

        return mergedTags.takeIf { it.isNotEmpty() }?.toList()
    }

    /**
     * Collects tags from all operations, normalizing to [ApiTag] without descriptions.
     *
     * @return A set of [ApiTag] items with names from operations.
     */
    private fun collectOperationTags(): Set<ApiTag> {
        return apiOperations.flatMap { operation ->
            operation.tags ?: emptySet()
        }.map { ApiTag(name = it) }.toSet()
    }

    /**
     * Collects tags from API configuration, including descriptions.
     *
     * @return A set of [ApiTag] items with names and descriptions from top-level configuration.
     */
    private fun collectConfigurationTags(): Set<ApiTag> {
        return apiConfiguration.apiTags ?: emptySet()
    }
}
