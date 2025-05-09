/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.introspection.descriptor

import io.github.perracodex.kopapi.introspection.annotation.TypeIntrospectorApi
import io.github.perracodex.kopapi.introspection.descriptor.TypeDescriptor.isArray
import io.github.perracodex.kopapi.introspection.descriptor.TypeDescriptor.isCollection
import io.github.perracodex.kopapi.introspection.descriptor.TypeDescriptor.isIterable
import io.github.perracodex.kopapi.introspection.descriptor.TypeDescriptor.isPrimitiveArray
import io.github.perracodex.kopapi.introspection.descriptor.TypeDescriptor.isTypedArray
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KType
import kotlin.reflect.full.isSuperclassOf

/**
 * Provides functionality to evaluate types and determine their characteristics.
 */
@TypeIntrospectorApi
internal object TypeDescriptor {
    /**
     * Determines whether the given [KType] represents a `Collection` as `List`, `Set`, etc.
     *
     * @param classifier The [KClassifier] of the `KType` to evaluate.
     * @return True if determined is a `Collection`, otherwise False.
     *
     * @see [isIterable]
     * @see [isArray]
     * @see [isTypedArray]
     * @see [isPrimitiveArray]
     */
    fun isCollection(classifier: KClassifier): Boolean {
        return classifier is KClass<*> && Collection::class.isSuperclassOf(derived = classifier)
    }

    /**
     * Determines whether the given [KType] represents an `Iterable` type.
     * Note that collections like `List` and `Set` are also `Iterable`.
     * This is for custom iterable types that do not inherit from `Collection`.
     *
     * @param classifier The [KClassifier] of the `KType` to evaluate.
     * @return True if determined is an `Iterable`, otherwise False.
     *
     * @see [isCollection]
     * @see [isArray]
     * @see [isTypedArray]
     * @see [isPrimitiveArray]
     */
    fun isIterable(classifier: KClassifier): Boolean {
        return classifier is KClass<*> && Iterable::class.isSuperclassOf(derived = classifier)
    }

    /**
     * Determines whether the given [kType] represents any array type in Kotlin,
     * for both, primitive arrays (e.g., `IntArray`, `DoubleArray`), and typed  arrays `Array<T>`.
     *
     * @param kType The [KType] to evaluate.
     * @return True if corresponds to any array type, otherwise False.
     *
     * @see [isTypedArray]
     * @see [isPrimitiveArray]
     * @see [isCollection]
     * @see [isIterable]
     */
    fun isArray(kType: KType): Boolean {
        val classifier: KClassifier = kType.classifier ?: return false
        return isPrimitiveArray(classifier = classifier) || isTypedArray(kType = kType)
    }

    /**
     * Determines whether the given [kType] represents a typed array `Array<T>`,
     *
     * Unlike standard generic classes like `List`, array types in Kotlin are
     * represented by distinct classes for each primitive type and a typed array `Array<T>`
     * class for reference types.
     * This distinction means that identifying an array type requires checking against
     * all possible array classifiers, both primitive and generic.
     *
     * @param kType The [KType] to evaluate.
     * @return True if corresponds to typed array `Array<T>`, otherwise False.
     *
     * @see [isArray]
     * @see [isPrimitiveArray]
     * @see [isCollection]
     * @see [isIterable]
     */
    fun isTypedArray(kType: KType): Boolean {
        val classifier: KClassifier = kType.classifier ?: return false
        return !isPrimitiveArray(classifier = classifier) &&
                kType.arguments.firstOrNull() != null &&
                ((classifier as? KClass<*>)?.javaObjectType?.isArray == true)
    }

    /**
     * Determines whether the given [KClassifier] represents a specialized primitive array type.
     *
     * Kotlin provides specialized array classes for each primitive type (e.g., `IntArray`, `FloatArray`),
     * which are distinct from the typed [Array] class used for reference types.
     *
     * @param classifier The [KClassifier] of the [KType] to evaluate.
     * @return True if the [classifier] is one of Kotlin's primitive array types, otherwise False.
     *
     * @see [isArray]
     * @see [isTypedArray]
     * @see [isCollection]
     * @see [isIterable]
     */
    fun isPrimitiveArray(classifier: KClassifier): Boolean {
        return classifier == IntArray::class || classifier == ByteArray::class ||
                classifier == ShortArray::class || classifier == FloatArray::class ||
                classifier == DoubleArray::class || classifier == LongArray::class ||
                classifier == CharArray::class || classifier == BooleanArray::class ||
                classifier == UIntArray::class || classifier == ULongArray::class ||
                classifier == UByteArray::class || classifier == UShortArray::class
    }

    /**
     * Determines whether the given [KClassifier] represents a `Map` type.
     *
     * @param classifier The [KClassifier] of the [KType] to evaluate.
     * @return True if the [classifier] is a `Map`, otherwise False.
     */
    fun isMap(classifier: KClassifier): Boolean {
        return classifier is KClass<*> && Map::class.isSuperclassOf(derived = classifier)
    }
}
