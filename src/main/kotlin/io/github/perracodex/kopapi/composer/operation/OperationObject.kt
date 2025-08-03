/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.composer.operation

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.composer.annotation.ComposerApi
import io.github.perracodex.kopapi.composer.parameter.ParameterObject
import io.github.perracodex.kopapi.composer.request.RequestBodyObject
import io.github.perracodex.kopapi.composer.response.ResponseObject
import io.github.perracodex.kopapi.dsl.plugin.element.ApiServerConfig

/**
 * Represents an individual API Operation (HTTP method) within a path item.
 *
 * Each operation includes details such as summary, description, tags, parameters,
 * request body, responses, and security requirements.
 *
 * @property tags A set of tags for API documentation control. Tags can be used to group operations.
 * @property summary A brief summary of what the operation does.
 * @property description A detailed description of the operation, which can span multiple lines.
 * @property operationId A unique identifier for the operation, which must be unique across all operations.
 * @property parameters A set of [ParameterObject] instances defining the parameters for the operation.
 * @property requestBody The [RequestBodyObject] instance defining the request body for the operation.
 * @property responses A map of `status codes` to [ResponseObject] instances.
 * @property security A list of security requirement maps, each specifying a security scheme and its scopes.
 *                    An empty list (`security: []`) signifies that the operation is publicly accessible.
 * @property servers A list of server configurations at the operation level.
 */
@ComposerApi
internal data class OperationObject(
    @field:JsonProperty("tags") val tags: Set<String>?,
    @field:JsonProperty("summary") val summary: String?,
    @field:JsonProperty("description") val description: String?,
    @field:JsonProperty("operationId") val operationId: String?,
    @field:JsonProperty("parameters") val parameters: Set<ParameterObject>?,
    @field:JsonProperty("requestBody") val requestBody: RequestBodyObject?,
    @field:JsonProperty("responses") val responses: Map<String, ResponseObject>?,
    @field:JsonProperty("security") val security: List<Map<String, List<String>>>?,
    @field:JsonProperty("servers") val servers: Set<ApiServerConfig>?
)
