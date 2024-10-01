/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.parser.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.KType

/**
 * Simple serializer to encode objects as their string representation.
 * Intended for serialization only, so does not support deserialization.
 */
internal object KTypeSerializerKotlinx : KSerializer<KType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "KType",
        kind = PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: KType) {
        encoder.encodeString(value = value.toString())
    }

    override fun deserialize(decoder: Decoder): KType {
        throw UnsupportedOperationException("Deserialization is not supported.")
    }
}

/**
 * Simple Jackson serializer to encode objects as their string representation.
 * Intended for serialization only, so does not support deserialization.
 */
internal object KTypeSerializerJackson : JsonSerializer<KType>() {
    override fun serialize(value: KType, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.toString())
    }
}
