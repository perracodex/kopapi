/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.parser.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Simple serializer to encode objects as their string representation.
 * Intended for serialization only, so does not support deserialization.
 */
internal object AnySerializer : KSerializer<Any> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "Any",
        kind = PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: Any) {
        encoder.encodeString(value = value.toString())
    }

    override fun deserialize(decoder: Decoder): Any {
        throw UnsupportedOperationException("Deserialization is not supported.")
    }
}
