/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.parser

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.perracodex.kopapi.core.KopapiPluginConfig
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
     * Configured JSON serializer.
     */
    private val jsonKotlinx = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
        isLenient = true
        serializersModule = SerializersModule {
            contextual(Any::class, AnySerializerKotlinx)
            contextual(ContentType::class, ContentTypeSerializerKotlinx)
            contextual(HttpMethod::class, HttpMethodSerializerKotlinx)
            contextual(HttpStatusCode::class, HttpStatusCodeSerializerKotlinx)
            contextual(KType::class, KTypeSerializerKotlinx)
            contextual(Url::class, UrlSerializerKotlinx)
        }
    }

    /**
     * Configured Jackson ObjectMapper.
     */
    private val jsonJackson: ObjectMapper = jacksonObjectMapper()
        .registerKotlinModule()
        .configure(SerializationFeature.INDENT_OUTPUT, true)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(SimpleModule().apply {
            addSerializer(ContentType::class.java, ContentTypeSerializerJackson)
            addSerializer(HttpMethod::class.java, HttpMethodSerializerJackson)
            addSerializer(HttpStatusCode::class.java, HttpStatusCodeSerializerJackson)
            addSerializer(KType::class.java, KTypeSerializerJackson)
            addSerializer(Url::class.java, UrlSerializerJackson)
        })

    /**
     * Serializes an object to a JSON string.
     *
     * @param value The object to serialize.
     * @param serializer The serializer to use.
     * @return The JSON string representation of the object.
     */
    inline fun <reified T> getJson(value: T, serializer: KopapiPluginConfig.Serializer): String {
        return when (serializer) {
            KopapiPluginConfig.Serializer.KOTLINX -> {
                jsonKotlinx.encodeToString(
                    serializer = serializersModule.serializer(),
                    value = value
                )
            }

            KopapiPluginConfig.Serializer.JACKSON -> {
                jsonJackson.writeValueAsString(value)
            }
        }
    }
}
