/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.serialization.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import io.ktor.http.*

/**
 * Simple Jackson serializer to encode [HttpStatusCode] objects as their string representation.
 */
internal object HttpStatusCodeSerializer : JsonSerializer<HttpStatusCode>() {
    override fun serialize(value: HttpStatusCode, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString("${value.value} ${value.description}")
    }
}
