/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.api.elements

import io.github.perracodex.kopapi.core.KopapiException
import io.github.perracodex.kopapi.dsl.api.builders.ApiMetadataBuilder
import io.github.perracodex.kopapi.dsl.api.elements.ApiParameter.Location
import io.github.perracodex.kopapi.dsl.api.types.ParameterStyle
import kotlin.reflect.KClassifier
import kotlin.reflect.KType

/**
 * Represents the metadata of an API endpoint parameter.
 *
 * @property type The [KType] of the parameter, specifying the Kotlin type.
 * @property name The name of the parameter as it appears in the API endpoint.
 * @property location The [Location] of the parameter, indicating where in the request it is included.
 * @property description A human-readable description of the parameter.
 * @property required Indicates whether the parameter is mandatory.
 * @property defaultValue The default value for the parameter, used if no value is provided.
 * @property explode Specifies whether arrays and objects should be exploded (true) or not (false).
 * @property style Describes how the parameter value is formatted when sent in a request.
 * @property deprecated Indicates whether the parameter is deprecated and should be avoided.
 *
 * @see [ApiMetadataBuilder.headerParameter]
 * @see [ApiMetadataBuilder.queryParameter]
 * @see [ApiMetadataBuilder.pathParameter]
 * @see [ApiMetadataBuilder.cookieParameter]
 */
internal data class ApiParameter(
    val type: KType,
    val location: Location,
    val name: String,
    val description: String?,
    val required: Boolean,
    val defaultValue: Any?,
    val explode: Boolean?,
    val style: ParameterStyle?,
    val deprecated: Boolean
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
    enum class Location(val value: String) {
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
