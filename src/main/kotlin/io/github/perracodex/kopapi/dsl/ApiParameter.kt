/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl

import io.github.perracodex.kopapi.dsl.ApiParameter.Location
import io.github.perracodex.kopapi.dsl.types.ParameterStyle
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
 * @see [ApiMetadata.headerParameter]
 * @see [ApiMetadata.queryParameter]
 * @see [ApiMetadata.pathParameter]
 * @see [ApiMetadata.cookieParameter]
 */
internal data class ApiParameter(
    val type: KType,
    val location: Location,
    val name: String,
    val description: String? = null,
    val required: Boolean = true,
    val defaultValue: Any? = null,
    val explode: Boolean? = null,
    val style: ParameterStyle? = null,
    val deprecated: Boolean = false
) {
    init {
        require(name.isNotBlank()) { "Name must not be empty." }
        require(type.classifier != Any::class) { "Parameters cannot be of type 'Any'. Define an explicit type." }
        require(type.classifier != Unit::class) { "Parameters cannot be of type 'Unit'. Define an explicit type." }
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
