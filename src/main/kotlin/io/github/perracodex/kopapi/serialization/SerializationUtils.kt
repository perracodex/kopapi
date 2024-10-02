/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.serialization

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.perracodex.kopapi.serialization.serializers.*
import io.ktor.http.*
import kotlin.reflect.KType

/**
 * Provides utility functions for serialization.
 */
internal object SerializationUtils {
    /**
     * Configured Jackson ObjectMapper.
     */
    private val jsonJackson: ObjectMapper = jacksonObjectMapper()
        .registerKotlinModule()
        .configure(SerializationFeature.INDENT_OUTPUT, true)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .setSerializationInclusion(JsonInclude.Include.ALWAYS)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(SimpleModule().apply {
            addSerializer(ContentType::class.java, ContentTypeSerializerJackson)
            addSerializer(HttpMethod::class.java, HttpMethodSerializerJackson)
            addSerializer(HttpStatusCode::class.java, HttpStatusCodeSerializerJackson)
            addSerializer(KType::class.java, KTypeSerializerJackson)
            addSerializer(Url::class.java, UrlSerializerJackson)
        })

    /**
     * Serializes the given object [instance] to a JSON string.
     *
     * @param instance The object instance to serialize.
     * @return The JSON string representation of the object [instance].
     */
    inline fun <reified T> toJson(instance: T): String {
        return jsonJackson.writeValueAsString(instance)
    }
}
