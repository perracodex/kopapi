/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.utils

import kotlin.reflect.KClassifier
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection

/**
 * Resolves the current [KType] by substituting any type parameters with their actual types
 * based on the provided [bindings].
 *
 * If no matching type is found in the [bindings], the original [KType] is returned unchanged.
 *
 * This is used primarily for resolving generics where type parameters such as `T`, `K` are
 * mapped to their concrete types. If the current [KType] has a classifier that exists
 * within the [bindings], it will be replaced by the corresponding concrete [KType].
 * Otherwise, the original [KType] is returned unchanged.
 *
 * #### Usage Example:
 * ```kotlin
 * val resolvedType = someKType.resolveArgumentBinding(typeArgumentBindings)
 * ```
 * - If the type is a generic like `Page<T>` and `T -> Employee` is present in the bindings,
 *   it will replace `T` with `Employee`.
 *
 * @param bindings A map where each key is a [KClassifier] representing a generic type
 * parameter (e.g., `T`, `K`), and the corresponding value is the [KType] to substitute for the type parameter.
 * @return The substituted [KType] if a corresponding type exists in the [bindings];
 * otherwise, the original [KType].
 *
 * @see [KTypeProjection.resolveTypeBinding]
 */
internal fun KType.resolveArgumentBinding(bindings: Map<KClassifier, KType>): KType {
    return classifier?.let {
        bindings.getOrDefault(key = classifier, defaultValue = this)
    } ?: this
}

/**
 * Resolves the [KType] within this [KTypeProjection] by applying the provided [bindings].
 *
 * If no matching type is found in the [bindings], the original [KType] is returned unchanged.
 *
 * This function is specifically designed for resolving generic type arguments that are represented
 * as projections (like `in T`, `out T`, or `*`). It retrieves the [KType] from the projection and
 * then resolves any potential generic type parameters using the bindings.
 *
 * #### Usage Example:
 * ```kotlin
 * val keyType = mapKTypeProjection.resolveTypeBinding(typeArgumentBindings)
 * ```
 * - If the projection refers to a generic type like `T`, and `T -> Employee` is in the bindings,
 *   it will resolve `T` to `Employee`.
 *
 * @param bindings A map where each key is a [KClassifier] representing a generic type
 * parameter (e.g., `T`, `K`), and the corresponding value is the [KType] to substitute for the type parameter.
 * @return The resolved [KType] for the projection, or `null` if the projection doesn't have a type.
 *
 * @see [KType.resolveArgumentBinding]
 */
internal fun KTypeProjection.resolveTypeBinding(bindings: Map<KClassifier, KType>): KType? {
    return this.type?.resolveArgumentBinding(bindings)
}
