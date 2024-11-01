/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.schema.factory

import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.schema.facets.ElementSchema
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import kotlin.reflect.KClass
import kotlin.uuid.Uuid

/**
 * Factory for constructing primitive schemas.
 */
@TypeInspectorAPI
internal object PrimitiveFactory {
    /**
     * Constructs a new [ElementSchema] representing the given primitive type.
     *
     * @param kClass The [KClass] representing the primitive type.
     * @return A [ElementSchema] for the primitive type, or null if the type is not a primitive.
     */
    fun newSchema(kClass: KClass<*>): ElementSchema? {
        return when (kClass) {
            // Basic Kotlin Types.
            Byte::class -> SchemaFactory.ofInt32()
            Boolean::class -> SchemaFactory.ofBoolean()
            Double::class -> SchemaFactory.ofDouble()
            Char::class -> SchemaFactory.ofChar()
            CharSequence::class -> SchemaFactory.ofString()
            Float::class -> SchemaFactory.ofFloat()
            Int::class -> SchemaFactory.ofInt32()
            Long::class -> SchemaFactory.ofInt64()
            Short::class -> SchemaFactory.ofInt32()
            String::class -> SchemaFactory.ofString()
            UByte::class -> SchemaFactory.ofInt32()
            UInt::class -> SchemaFactory.ofInt32()
            ULong::class -> SchemaFactory.ofInt64()
            UShort::class -> SchemaFactory.ofInt32()

            // Primitive Arrays.
            BooleanArray::class -> SchemaFactory.ofArray(items = SchemaFactory.ofBoolean())
            ByteArray::class -> SchemaFactory.ofArray(items = SchemaFactory.ofByte())
            CharArray::class -> SchemaFactory.ofArray(items = SchemaFactory.ofChar())
            DoubleArray::class -> SchemaFactory.ofArray(items = SchemaFactory.ofDouble())
            FloatArray::class -> SchemaFactory.ofArray(items = SchemaFactory.ofFloat())
            IntArray::class -> SchemaFactory.ofArray(items = SchemaFactory.ofInt32())
            LongArray::class -> SchemaFactory.ofArray(items = SchemaFactory.ofInt64())
            ShortArray::class -> SchemaFactory.ofArray(items = SchemaFactory.ofInt32())
            UByteArray::class -> SchemaFactory.ofArray(items = SchemaFactory.ofByte())
            UIntArray::class -> SchemaFactory.ofArray(items = SchemaFactory.ofInt32())
            ULongArray::class -> SchemaFactory.ofArray(items = SchemaFactory.ofInt64())
            UShortArray::class -> SchemaFactory.ofArray(items = SchemaFactory.ofInt32())

            // UUID Types.
            Uuid::class, UUID::class -> SchemaFactory.ofUuid()

            // Kotlin Date/Time Types.
            kotlinx.datetime.LocalDate::class -> SchemaFactory.ofDate()
            kotlinx.datetime.LocalDateTime::class -> SchemaFactory.ofDateTime()
            kotlinx.datetime.Instant::class -> SchemaFactory.ofDateTime()
            kotlinx.datetime.LocalTime::class -> SchemaFactory.ofTime()

            // Java Date/Time Types.
            java.time.OffsetDateTime::class -> SchemaFactory.ofDateTime()
            java.time.ZonedDateTime::class -> SchemaFactory.ofDateTime()
            java.time.LocalTime::class -> SchemaFactory.ofTime()
            java.time.LocalDate::class -> SchemaFactory.ofDate()
            java.time.LocalDateTime::class -> SchemaFactory.ofDateTime()
            java.time.Instant::class -> SchemaFactory.ofDateTime()
            java.util.Date::class -> SchemaFactory.ofDateTime()
            java.sql.Date::class -> SchemaFactory.ofDate()

            // Big Numbers.
            BigDecimal::class -> SchemaFactory.ofDouble()
            BigInteger::class -> SchemaFactory.ofInt64()

            // URL and URI.
            io.ktor.http.Url::class -> SchemaFactory.ofUri()
            java.net.URL::class -> SchemaFactory.ofUri()
            java.net.URI::class -> SchemaFactory.ofUri()

            else -> null // Return null if it's not a primitive type.
        }
    }
}
