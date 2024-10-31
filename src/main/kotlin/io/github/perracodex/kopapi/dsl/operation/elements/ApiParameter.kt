/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.elements

import com.fasterxml.jackson.annotation.JsonValue
import io.github.perracodex.kopapi.dsl.operation.builders.operation.ApiOperationBuilder
import io.github.perracodex.kopapi.dsl.operation.elements.ApiParameter.Location
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.types.DefaultValue
import io.github.perracodex.kopapi.types.ParameterStyle
import kotlin.reflect.KClassifier
import kotlin.reflect.KType

/**
 * Represents the metadata of an API endpoint parameter.
 *
 * @property type The [KType] of the parameter.
 * @property name The name of the parameter as it appears in the API endpoint.
 * @property description Optional human-readable explanation of the parameter's purpose.
 * @property location The [Location] of the parameter, indicating where in the request it is included.
 * @property required Whether if this parameter is mandatory. Path parameters must always be required.
 * @property allowReserved Whether reserved characters (e.g., `?`, `/`) are allowed. Only applicable to query parameters.
 * @property defaultValue Optional default value for the parameter.
 * @property style Defines the serialization style of the parameter.
 * @property explode Determines how arrays and objects are serialized. Only applicable to query and cookie parameters.
 * @property deprecated Indicates whether the parameter is deprecated and should be avoided.
 *
 * @see [ApiOperationBuilder.headerParameter]
 * @see [ApiOperationBuilder.queryParameter]
 * @see [ApiOperationBuilder.pathParameter]
 * @see [ApiOperationBuilder.cookieParameter]
 */
internal data class ApiParameter(
    val type: KType,
    val name: String,
    val description: String?,
    val location: Location,
    val required: Boolean,
    val allowReserved: Boolean?,
    val defaultValue: DefaultValue?,
    val style: ParameterStyle?,
    val explode: Boolean?,
    val deprecated: Boolean?
) {
    init {
        if (name.isBlank()) {
            throw KopapiException("Parameter name must not be empty.")
        }

        // Ensure non-supported types are not used.
        val classifier: KClassifier? = type.classifier
        if (classifier == Any::class || classifier == Unit::class || classifier == Nothing::class) {
            throw KopapiException("Parameter cannot of type '${type.classifier}'. Define an explicit type.")
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
