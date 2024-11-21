/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.dsl.plugin.builder

import io.github.perracodex.kopapi.dsl.marker.KopapiDsl
import io.github.perracodex.kopapi.dsl.plugin.element.ApiTag
import io.github.perracodex.kopapi.system.Tracer
import io.github.perracodex.kopapi.util.orNull

/**
 * Builder constructing top level tags for the API.
 */
@KopapiDsl
public class TagBuilder internal constructor() {
    private val tracer: Tracer = Tracer<TagBuilder>()

    /** The internal set to enforce uniqueness of tags. */
    private val tags: MutableSet<ApiTag> = LinkedHashSet()

    /**
     * Adds a tag to the set of tags for the API.
     *
     * #### Usage
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
     * Returns the registered tags.
     */
    internal fun build(): Set<ApiTag>? = tags.orNull()?.toSet()

    /** Provides a string representation of the tags. */
    override fun toString(): String = tags.toString()
}
