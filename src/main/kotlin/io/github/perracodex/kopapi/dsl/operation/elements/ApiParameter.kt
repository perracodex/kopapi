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
import io.github.perracodex.kopapi.types.PathType
import kotlin.reflect.KClassifier
import kotlin.reflect.KType

/**
 * Represents the metadata of an API endpoint parameter.
 *
 * @property complexType The [KType] of the parameter, used for non-`path` parameters.
 * @property pathType The [PathType] of the parameter, specifying the type for `path` parameters.
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
    val complexType: KType?,
    val pathType: PathType?,
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

        // Validate the parameter based on its location.
        when (location) {
            Location.PATH -> validatePathParameter()
            else -> validateNonPathParameter()
        }
    }

    /**
     * Validates the path parameter.
     * Ensures that `pathType` is not null and that `type` is null for path parameters.
     */
    private fun validatePathParameter() {
        if (pathType == null) {
            throw KopapiException("Path parameters must have a `PathType` defined.")
        }
        if (complexType != null) {
            throw KopapiException("Path parameters should not have a KType defined.")
        }
    }

    /**
     * Validates the non-path parameter.
     * Ensures that `type` is not null and that `pathType` is null for non-path parameters.
     * Also checks for unsupported types like `Any`, `Unit`, and `Nothing`.
     */
    private fun validateNonPathParameter() {
        if (complexType == null) {
            throw KopapiException("Non-path parameters must have a KType defined.")
        }
        if (pathType != null) {
            throw KopapiException("Non-path parameters should not have a `PathType` defined.")
        }

        // Ensure unsupported types are not used for non-path parameters.
        val classifier: KClassifier? = complexType.classifier
        if (classifier == Any::class || classifier == Unit::class || classifier == Nothing::class) {
            throw KopapiException("Parameter cannot be of unsupported type '${complexType.classifier}'. Define a valid type.")
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
