/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.type

import kotlin.reflect.KClassifier
import kotlin.reflect.KType

/**
 * [KType] extension function to substitute the current [KType] with a corresponding type
 * from the provided [typeParameterMap] if its classifier exists within the map.
 *
 * Primarily used to resolve `Generics` type parameters to their actual concrete types during
 * type inspection and schema generation. By replacing type parameters with their mapped types,
 * it ensures accurate and consistent type resolution across the entire type hierarchy.
 *
 * #### Usage Scenarios
 * 1. **Single Type Parameter Substitution:**
 *    Consider a generic class `Page<T>`. When inspecting an instance like `Page<Employee>`,
 *    this function replaces the type parameter `T` with `Employee`, facilitating the correct
 *    schema generation for `Page<Employee>`.
 *
 * 2. **Multiple Type Parameters Substitution:**
 *    Consider a generic class `Container<T, U>` with concrete classes `Employee` and `Department`.
 *    When inspecting an instance like `Container<Employee, Department>`, this function replaces
 *    the type parameters `T` and `U` with `Employee` and `Department` respectively, facilitating
 *    the correct schema generation for `Container<Employee, Department>`.
 *
 * #### Detailed Explanation
 * - **Purpose:**
 *   - The function replaces generic type parameters (`T`, `K`, etc.) with their actual concrete types.
 *
 * - **How It Works:**
 *   - The function checks if the classifier of the current `KType` exists in the provided `typeParameterMap`.
 *   - If a match is found, it substitutes the generic type with the corresponding concrete type from the map.
 *   - If no match is found, it returns the original `KType`, ensuring that non-generic or already-resolved types remain unaffected.
 *
 * @receiver The [KType] instance on which the substitution is to be performed.
 * @param typeParameterMap A map where each key is a [KClassifier] representing a `Generics` type
 * parameter (e.g., `T`, `K`), and the corresponding value is the [KType] to substitute in place of the type parameter.
 * @return The substituted [KType] if its classifier is present in the [typeParameterMap];
 * otherwise, returns the original [KType].
 */
internal fun KType.resolveGenerics(typeParameterMap: Map<KClassifier, KType>): KType {
    val classifier: KClassifier? = this.classifier
    return if (classifier in typeParameterMap) {
        typeParameterMap[classifier]!!
    } else {
        this
    }
}
