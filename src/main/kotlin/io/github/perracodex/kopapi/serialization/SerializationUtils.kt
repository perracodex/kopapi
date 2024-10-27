/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.serialization

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.perracodex.kopapi.serialization.serializers.*
import io.ktor.http.*
import kotlin.reflect.KType


/**
 * Provides utility functions for serialization.
 */
internal class SerializationUtils {
    /** Configured Jackson Mapper. for debugging purposes. */
    val debugJson: JsonMapper = JsonMapper.builder()
        .addModule(kotlinModule())
        .enable(SerializationFeature.INDENT_OUTPUT)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .serializationInclusion(JsonInclude.Include.ALWAYS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .enable(MapperFeature.SORT_CREATOR_PROPERTIES_FIRST)
        .enable(MapperFeature.SORT_CREATOR_PROPERTIES_BY_DECLARATION_ORDER)
        .disable(MapperFeature.USE_ANNOTATIONS)
        .addModule(serializerModule())
        .build()

    /**
     * Configured Jackson JSON Mapper, strictly for OpenAPI schema serialization.
     */
    private val openApiJsonMapper: JsonMapper = JsonMapper.builder()
        .addModule(kotlinModule())
        .enable(SerializationFeature.INDENT_OUTPUT)
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .enable(MapperFeature.SORT_CREATOR_PROPERTIES_FIRST)
        .enable(MapperFeature.SORT_CREATOR_PROPERTIES_BY_DECLARATION_ORDER)
        .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
        .addModule(serializerModule())
        .build()

    /**
     * Configured Jackson YAML Mapper, strictly for OpenAPI schema serialization.
     */
    private val openApiYamlMapper: YAMLMapper = YAMLMapper.builder()
        .addModule(kotlinModule())
        .enable(SerializationFeature.INDENT_OUTPUT)
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        .enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE)
        .enable(YAMLGenerator.Feature.INDENT_ARRAYS_WITH_INDICATOR)
        .disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .enable(MapperFeature.SORT_CREATOR_PROPERTIES_FIRST)
        .enable(MapperFeature.SORT_CREATOR_PROPERTIES_BY_DECLARATION_ORDER)
        .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
        .disable(YAMLGenerator.Feature.SPLIT_LINES)
        .enable(YAMLGenerator.Feature.ALLOW_LONG_KEYS)
        .addModule(serializerModule())
        .build()

    /** Configures custom serializers for Jackson. */
    private fun serializerModule(): SimpleModule {
        return SimpleModule().apply {
            addSerializer(ContentType::class.java, ContentTypeSerializer)
            addSerializer(HttpMethod::class.java, HttpMethodSerializer)
            addSerializer(HttpStatusCode::class.java, HttpStatusCodeSerializer)
            addSerializer(KType::class.java, KTypeSerializer)
            addSerializer(Url::class.java, UrlSerializer)
        }
    }

    /**
     * Serializes the given object [instance] to a JSON string.
     *
     * @param instance The object instance to serialize.
     * @return The JSON string representation of the object [instance].
     */
    fun toRawJson(instance: Any): String {
        return debugJson.writeValueAsString(instance)
    }

    /**
     * Deserializes the given JSON string [json] to an object of type [T].
     *
     * @param json The JSON string to deserialize.
     * @return The deserialized object of type [T].
     */
    inline fun <reified T> fromRawJson(json: String): T {
        return debugJson.readValue(json)
    }

    /**
     * Serializes the given object [instance] to a JSON string for OpenAPI.
     *
     * @param instance The object instance to serialize.
     * @return The JSON string representation of the object [instance].
     */
    fun toJson(instance: Any): String {
        return openApiJsonMapper.writeValueAsString(instance)
    }

    /**
     * Serializes the given object [instance] to a YAML string for OpenAPI.
     *
     * @param instance The object instance to serialize.
     * @return The YAML string representation of the object [instance].
     */
    fun toYaml(instance: Any): String {
        return openApiYamlMapper.writeValueAsString(instance)
    }
}
