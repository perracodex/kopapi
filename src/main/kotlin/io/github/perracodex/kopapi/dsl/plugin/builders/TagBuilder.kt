/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.plugin.builders

import io.github.perracodex.kopapi.dsl.markers.ConfigurationDsl
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiTag
import io.github.perracodex.kopapi.system.Tracer
import io.ktor.utils.io.*

/**
 * Builder constructing top level tags for the API.
 */
@KtorDsl
@ConfigurationDsl
public class TagBuilder {
    private val tracer = Tracer<TagBuilder>()

    /** The internal set to enforce uniqueness of tags. */
    private val tags: MutableSet<ApiTag> = LinkedHashSet()

    /**
     * Adds a tag to the set of tags for the API.
     *
     * #### Sample Usage
     * ```
     * tags {
     *      add(name = "Items", description = "Operations related to items.")
     *      add(name = "Users", description = "Operations related to users.")
     * }
     *```
     *
     * @param name The name of the tag.
     * @param description Optional description of the tag.
     */
    public fun add(name: String, description: String? = null) {
        val tagName: String = name.trim()
        if (tagName.isBlank()) {
            tracer.warning("Tag name cannot be blank.")
            return
        }

        // Replace any existing tag with the same name.
        val tag = ApiTag(name = tagName, description = description)
        tags.removeIf { it.name.equals(other = tagName, ignoreCase = true) }
        tags.add(tag)
    }

    /**
     * Builds and returns the immutable set of [ApiTag] items.
     *
     * @return A read-only set of tags.
     */
    internal fun build(): Set<ApiTag> {
        return tags.toSet()
    }

    /** Clears the internal set of tags. */
    override fun toString(): String = tags.toString()
}
