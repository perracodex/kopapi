/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl

import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Represents a set of tags with enforced case-insensitive uniqueness,
 * trimming of entries, and filtering out blank strings.
 *
 * @param tags The initial tags to include.
 */
public class Tags(vararg tags: String) : Set<String> {
    private val internalSet: MutableSet<String> = TreeSet(String.CASE_INSENSITIVE_ORDER).apply {
        addAll(tags.map { it.trim() }.filter { it.isNotBlank() })
    }

    /**
     * Adds all tags from the provided [Tags] instance.
     *
     * @param other The [Tags] instance to merge.
     */
    public fun addAll(other: Tags) {
        internalSet.addAll(other)
    }

    /**
     * Adds all tags from the provided [Collection] of strings.
     *
     * @param tags The collection of tags to add.
     */
    public fun addAll(tags: Collection<String>) {
        internalSet.addAll(tags.map { it.trim() }.filter { it.isNotBlank() })
    }

    override val size: Int
        get() = internalSet.size

    override fun isEmpty(): Boolean = internalSet.isEmpty()

    override fun contains(element: String): Boolean = internalSet.contains(element)

    override fun containsAll(elements: Collection<String>): Boolean = internalSet.containsAll(elements)

    override fun iterator(): Iterator<String> = internalSet.iterator()

    override fun toString(): String = internalSet.toString()
}

/**
 * A property delegate that manages a [Tags] instance, allowing
 * multiple assignments to append tags rather than replace them.
 */
internal class TagsDelegate(initialTags: Tags = Tags()) : ReadWriteProperty<ApiMetadata, Tags> {
    private val internalTags: Tags = Tags(*initialTags.toTypedArray())

    override fun getValue(thisRef: ApiMetadata, property: KProperty<*>): Tags = internalTags

    override fun setValue(thisRef: ApiMetadata, property: KProperty<*>, value: Tags) {
        internalTags.addAll(value)
    }
}
