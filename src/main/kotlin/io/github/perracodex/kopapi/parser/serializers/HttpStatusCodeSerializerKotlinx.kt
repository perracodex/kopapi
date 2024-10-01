/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.parser.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import io.ktor.http.*
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
internal object HttpStatusCodeSerializerKotlinx : KSerializer<HttpStatusCode> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "HttpStatusCode",
        kind = PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: HttpStatusCode) {
        encoder.encodeString(value = value.toString())
    }

    override fun deserialize(decoder: Decoder): HttpStatusCode {
        throw UnsupportedOperationException("Deserialization is not supported.")
    }
}

/**
 * Simple Jackson serializer to encode objects as their string representation.
 * Intended for serialization only, so does not support deserialization.
 */
internal object HttpStatusCodeSerializerJackson : JsonSerializer<HttpStatusCode>() {
    override fun serialize(value: HttpStatusCode, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString("${value.value} ${value.description}")
    }
}
