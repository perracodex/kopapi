/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.type

import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.spec.Spec
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KType
import kotlin.reflect.full.isSuperclassOf
import kotlin.uuid.Uuid

/**
 * Provides functionality to evaluate and map Kotlin types, including primitive types
 * and their corresponding schemas.
 */
@TypeInspectorAPI
internal object TypeDescriptor {
    /**
     * Determines whether the given [KType] represents a [Collection] as [List], [Set], etc.
     *
     * @param classifier The [KClassifier] of the [KType] to evaluate.
     * @return True if determined is a [Collection], otherwise False.
     *
     * @see [isArray]
     * @see [isGenericsArray]
     * @see [isPrimitiveArray]
     */
    fun isCollection(classifier: KClassifier): Boolean {
        return classifier is KClass<*> && Collection::class.isSuperclassOf(classifier)
    }

    /**
     * Determines whether the given [kType] represents any array type in Kotlin,
     * for both, primitive arrays (e.g., [IntArray], [DoubleArray]), and generics [Array] types
     *
     * @param kType The [KType] to evaluate.
     * @return True if corresponds to any array type, otherwise False.
     *
     * @see [isGenericsArray]
     * @see [isPrimitiveArray]
     * @see [isCollection]
     */
    fun isArray(kType: KType): Boolean {
        val classifier: KClassifier = kType.classifier ?: return false
        return isPrimitiveArray(classifier = classifier)
                || isGenericsArray(kType = kType)
    }

    /**
     * Determines whether the given [kType] represents a generics [Array] type,
     *
     * Unlike standard generic classes like [List], array types in Kotlin are
     * represented by distinct classes for each primitive type and a generic [Array]
     * class for reference types.
     * This distinction means that identifying an array type requires checking against
     * all possible array classifiers, both primitive and generic.
     *
     * @param kType The [KType] to evaluate.
     * @return True if corresponds to generics [Array] type, otherwise False.
     *
     * @see [isArray]
     * @see [isPrimitiveArray]
     * @see [isCollection]
     */
    fun isGenericsArray(kType: KType): Boolean {
        val classifier: KClassifier = kType.classifier ?: return false
        return !isPrimitiveArray(classifier = classifier) &&
                kType.arguments.firstOrNull() != null &&
                ((classifier as? KClass<*>)?.javaObjectType?.isArray ?: false)
    }

    /**
     * Determines whether the given [KClassifier] represents a specialized primitive array type.
     *
     * Kotlin provides specialized array classes for each primitive type (e.g., [IntArray], [FloatArray]),
     * which are distinct from the generic [Array] class used for reference types.
     *
     * @param classifier The [KClassifier] of the [KType] to evaluate.
     * @return True if the [classifier] is one of Kotlin's primitive array types, otherwise False.
     *
     * @see [isArray]
     * @see [isGenericsArray]
     * @see [isCollection]
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
     * Constructs a mutable map representing the schema for the given primitive type.
     *
     * @param kClass The [KClass] representing the primitive type.
     * @return A mutable map representing the schema for the primitive type,
     * or null if the type is not a primitive.
     */
    fun mapPrimitiveType(kClass: KClass<*>): MutableMap<String, Any>? {
        return when (kClass) {
            // Basic Kotlin Types.
            String::class, CharSequence::class -> Spec.string()
            Char::class -> Spec.char()
            Boolean::class -> Spec.boolean()
            Int::class -> Spec.int32()
            Long::class -> Spec.int64()
            Double::class -> Spec.double()
            Float::class -> Spec.float()
            Short::class -> Spec.int32()
            Byte::class -> Spec.int32()
            UInt::class -> Spec.int32()
            ULong::class -> Spec.int64()
            UShort::class -> Spec.int32()
            UByte::class -> Spec.int32()

            // Primitive Arrays.
            IntArray::class, ShortArray::class, UIntArray::class, UShortArray::class -> Spec.array(spec = Spec.int32())
            LongArray::class, ULongArray::class -> Spec.array(spec = Spec.int64())
            FloatArray::class -> Spec.array(spec = Spec.float())
            DoubleArray::class -> Spec.array(spec = Spec.double())
            BooleanArray::class -> Spec.array(spec = Spec.boolean())
            CharArray::class -> Spec.array(spec = Spec.char())
            ByteArray::class, UByteArray::class -> Spec.array(spec = Spec.byte())

            // UUID Types.
            Uuid::class, UUID::class -> Spec.uuid()

            // Kotlin Date/Time Types.
            kotlinx.datetime.LocalDate::class -> Spec.date()
            kotlinx.datetime.LocalDateTime::class -> Spec.dateTime()
            kotlinx.datetime.Instant::class -> Spec.dateTime()
            kotlinx.datetime.LocalTime::class -> Spec.time()

            // Java Date/Time Types.
            java.time.OffsetDateTime::class -> Spec.dateTime()
            java.time.ZonedDateTime::class -> Spec.dateTime()
            java.time.LocalTime::class -> Spec.time()
            java.time.LocalDate::class -> Spec.date()
            java.time.LocalDateTime::class -> Spec.dateTime()
            java.time.Instant::class -> Spec.dateTime()
            java.util.Date::class -> Spec.dateTime()
            java.sql.Date::class -> Spec.date()

            // Big Numbers.
            BigDecimal::class -> Spec.double()
            BigInteger::class -> Spec.int64()

            // URL and URI.
            io.ktor.http.Url::class -> Spec.uri()
            java.net.URL::class -> Spec.uri()
            java.net.URI::class -> Spec.uri()

            else -> null // Return null if it's not a primitive type.
        }
    }
}