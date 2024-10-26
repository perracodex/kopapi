/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.parameter

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.github.perracodex.kopapi.dsl.operation.elements.ApiParameter
import io.github.perracodex.kopapi.schema.ElementSchema

/**
 * Represents an API operation parameter in the OpenAPI specification.
 *
 * @property name The name of the parameter, which must match the parameter name in the API endpoint.
 * @property location Specifies where the parameter is found: path, query, header, or cookie.
 * @property description Optional human-readable explanation of the parameter's purpose.
 * @property required Whether if this parameter is mandatory. Path parameters must always be required.
 * @property allowReserved Whether reserved characters (e.g., `?`, `/`) are allowed. Only applicable to query parameters.
 * @property style Defines the serialization style of the parameter.
 * @property explode Determines how arrays and objects are serialized. Only applicable to query and cookie parameters.
 * @property deprecated Indicates whether the parameter is deprecated and should be avoided.
 * @property schema The schema defining the structure and data type of the parameter, such as string, integer, or object.
 * @property defaultValue Optional default value for the parameter.
 */
@ComposerAPI
internal data class ParameterObject(
    @JsonProperty("name") val name: String,
    @JsonProperty("in") val location: ApiParameter.Location,
    @JsonProperty("description") val description: String?,
    @JsonProperty("required") val required: Boolean,
    @JsonProperty("allowReserved") val allowReserved: Boolean?,
    @JsonProperty("style") val style: String?,
    @JsonProperty("explode") val explode: Boolean?,
    @JsonProperty("deprecated") val deprecated: Boolean?,
    @JsonProperty("schema") val schema: ElementSchema,
    @JsonProperty("default") val defaultValue: Any?
)
