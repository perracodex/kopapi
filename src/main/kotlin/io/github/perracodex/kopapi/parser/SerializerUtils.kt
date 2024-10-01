/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.parser

import io.github.perracodex.kopapi.parser.serializers.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.serializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import kotlin.reflect.KType

/**
 * Provides utility functions for serialization.
 */
internal object SerializerUtils {
    /**
     * Contextual serializers for concrete types that are not serializable by default.
     */
    private val serializers: SerializersModule = SerializersModule {
        contextual(Any::class, AnySerializer)
        contextual(ContentType::class, ContentTypeSerializer)
        contextual(HttpMethod::class, HttpMethodSerializer)
        contextual(HttpStatusCode::class, HttpStatusCodeSerializer)
        contextual(KType::class, KTypeSerializer)
        contextual(Url::class, UrlSerializer)
    }

    /**
     * Configured JSON serializer.
     */
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
        isLenient = true
        serializersModule = serializers
    }

    /**
     * Serializes an object to a JSON string.
     *
     * @param value The object to serialize.
     * @return The JSON string representation of the object.
     */
    inline fun <reified T> getJson(value: T): String {
        return json.encodeToString(
            serializer = serializersModule.serializer(),
            value = value
        )
    }
}
