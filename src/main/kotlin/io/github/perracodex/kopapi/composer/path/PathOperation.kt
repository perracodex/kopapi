/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer.path

import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.github.perracodex.kopapi.dsl.operation.elements.ApiOperation
import io.github.perracodex.kopapi.dsl.operation.elements.ApiParameter
import io.github.perracodex.kopapi.dsl.operation.elements.ApiRequestBody
import io.github.perracodex.kopapi.dsl.operation.elements.ApiResponse

/**
 * Represents an individual API Operation (HTTP method) within a path item.
 *
 * Each operation includes details such as summary, description, tags, parameters,
 * request body, responses, and security requirements.
 *
 * @property summary A brief summary of what the operation does.
 * @property description A detailed description of the operation, which can span multiple lines.
 * @property tags A set of tags for API documentation control. Tags can be used to group operations.
 * @property parameters A set of [ApiParameter] objects defining the parameters for the operation.
 * @property requestBody The [ApiRequestBody] object defining the request body for the operation.
 * @property responses A set of [ApiResponse] objects defining the possible responses for the operation.
 * @property security A list of security requirement maps, each specifying a security scheme and its scopes.
 *                    An empty list (`security: []`) disables security for this operation.
 */
@ComposerAPI
internal data class PathOperation(
    val summary: String?,
    val description: String?,
    val tags: Set<String>?,
    val parameters: Set<ApiParameter>?,
    val requestBody: ApiRequestBody?,
    val responses: Set<ApiResponse>?,
    var security: List<Map<String, List<String>>>? = null
) {
    companion object {
        /**
         * Creates an [PathOperation] instance from an [ApiOperation] object.
         *
         * This factory method facilitates the transformation of an [ApiOperation] into
         * an [PathOperation] suitable for inclusion in the OpenAPI schema.
         *
         * @param apiOperation The [ApiOperation] object containing the operation's metadata.
         * @return An [PathOperation] instance populated with data from [apiOperation].
         */
        fun fromApiOperation(apiOperation: ApiOperation): PathOperation {
            return PathOperation(
                summary = apiOperation.summary,
                description = apiOperation.description,
                tags = apiOperation.tags,
                parameters = apiOperation.parameters,
                requestBody = apiOperation.requestBody,
                responses = apiOperation.responses
            )
        }
    }
}