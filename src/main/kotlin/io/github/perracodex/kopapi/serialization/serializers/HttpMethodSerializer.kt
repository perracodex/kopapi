/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.serialization.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import io.ktor.http.*

/**
 * Simple Jackson serializer to encode [HttpMethod] objects as their string representation.
 */
internal object HttpMethodSerializerJackson : JsonSerializer<HttpMethod>() {
    override fun serialize(value: HttpMethod, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.value)
    }
}
