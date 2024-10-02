/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl

import io.github.perracodex.kopapi.dsl.ApiParameter.Location
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
 */
@ConsistentCopyVisibility
public data class ApiParameter @PublishedApi internal constructor(
    val type: KType,
    val location: Location,
    val name: String,
    val description: String? = null,
    val required: Boolean = true,
    val defaultValue: Any? = null,
    val explode: Boolean? = null,
    val style: Style? = null,
    val deprecated: Boolean = false
) {
    init {
        require(name.isNotEmpty()) {
            "Name must not be empty."
        }
    }

    /**
     * Enum describing possible locations of a parameter within an API request.
     *
     * @property value The string value of the location.
     */
    public enum class Location(public val value: String) {
        /** Parameter is included in the URL path. */
        PATH(value = "path"),

        /** Parameter is included in the URL query string. */
        QUERY(value = "query"),

        /** Parameter is included in the header of the request. */
        HEADER(value = "header"),

        /** Parameter is sent within a cookie. */
        COOKIE(value = "cookie"),
    }

    /**
     * Enum defining possible serialization styles for parameters.
     */
    public enum class Style {
        /** Commonly used for query parameters, formatted as key-value pairs. */
        FORM,

        /** Commonly used for headers, presented in a simple, compact format. */
        SIMPLE,

        /** Serializes arrays by separating values with spaces. e.g., "a b c" */
        SPACE_DELIMITED,

        /** Serializes arrays by separating values with pipes. e.g., "a|b|c" */
        PIPE_DELIMITED,

        /** Prefixes parameters with a period and uses dot-separated values. e.g., ".a.b.c" */
        LABEL,

        /** Uses semicolons to separate object properties in paths. e.g., ";a=1;b=2" */
        MATRIX
    }
}
