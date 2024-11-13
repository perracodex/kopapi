/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.util

import java.util.*

/**
 * Returns the receiver array if it is not empty, otherwise returns `null`.
 */
@JvmName(name = "iterableArrayOrNull")
internal fun <T> Array<T>.orNull(): Array<T>? = if (isEmpty()) null else this

/**
 * Returns the receiver list if it is not empty, otherwise returns `null`.
 */
@JvmName(name = "iterableListOrNull")
internal fun <T> List<T>.orNull(): List<T>? = ifEmpty { null }

/**
 * Returns the receiver map if it is not empty, otherwise returns `null`.
 */
@JvmName(name = "iterableMapOrNull")
internal fun <K, V> Map<K, V>.orNull(): Map<K, V>? = ifEmpty { null }

/**
 * Returns the receiver mutable map if it is not empty, otherwise returns `null`.
 */
@JvmName(name = "iterableMutableMapOrNull")
internal fun <K, V> MutableMap<K, V>.orNull(): MutableMap<K, V>? = ifEmpty { null }

/**
 * Returns the receiver mutable set if it is not empty, otherwise returns `null`.
 */
@JvmName(name = "iterableMutableSetOrNull")
internal fun <T> MutableSet<T>.orNull(): MutableSet<T>? = ifEmpty { null }

/**
 * Returns the receiver set if it is not empty, otherwise returns `null`.
 */
@PublishedApi
@JvmName(name = "iterableSetOrNull")
internal fun <T> Set<T>.orNull(): Set<T>? = ifEmpty { null }

/**
 * Returns the receiver map if it is not empty, otherwise returns `null`.
 */
@JvmName(name = "iterableSortedMapOrNull")
internal fun <K, V> SortedMap<K, V>.orNull(): SortedMap<K, V>? = ifEmpty { null }
