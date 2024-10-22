/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.elements

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import io.github.perracodex.kopapi.dsl.operation.builders.ApiOperationBuilder
import io.github.perracodex.kopapi.dsl.operation.elements.ApiParameter.Location
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.types.ParameterStyle
import kotlin.reflect.KClassifier
import kotlin.reflect.KType

/**
 * Represents the metadata of an API endpoint parameter.
 *
 * @property type The [KType] of the parameter, specifying the Kotlin type.
 * @property name The name of the parameter as it appears in the API endpoint.
 * @property description A human-readable description of the parameter.
 * @property location The [Location] of the parameter, indicating where in the request it is included.
 * @property required Indicates whether the parameter is mandatory.
 * @property defaultValue The default value for the parameter, used if no value is provided.
 * @property style Describes how the parameter value is formatted when sent in a request.
 * @property explode Specifies whether arrays and objects should be exploded (true) or not (false).
 * @property deprecated Indicates whether the parameter is deprecated and should be avoided.
 *
 * @see [ApiOperationBuilder.headerParameter]
 * @see [ApiOperationBuilder.queryParameter]
 * @see [ApiOperationBuilder.pathParameter]
 * @see [ApiOperationBuilder.cookieParameter]
 */
internal data class ApiParameter(
    @JsonIgnore val type: KType,
    val name: String,
    val description: String?,
    @JsonProperty("in") val location: Location,
    val required: Boolean,
    @JsonProperty("default") val defaultValue: Any?,
    val style: ParameterStyle?,
    val explode: Boolean?,
    val deprecated: Boolean?
) {
    init {
        if (name.isBlank()) {
            throw KopapiException("Parameter name must not be empty.")
        }

        val classifier: KClassifier? = type.classifier
        if (classifier == Any::class || classifier == Unit::class || classifier == Nothing::class) {
            throw KopapiException("Route Parameter cannot be of type '${type.classifier}'. Define an explicit type.")
        }
    }

    /**
     * Enum describing possible locations of a parameter within an API request.
     *
     * @property value The string value of the location.
     */
    enum class Location(@JsonValue val value: String) {
        /** Parameter is included in the URL path. */
        PATH(value = "path"),

        /** Parameter is included in the URL query string. */
        QUERY(value = "query"),

        /** Parameter is included in the header of the request. */
        HEADER(value = "header"),

        /** Parameter is sent within a cookie. */
        COOKIE(value = "cookie"),
    }
}
