/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package api.parameters

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object ParameterTestUtils {

    fun findParameter(
        schemaJson: String,
        path: String,
        parameterName: String
    ): JsonNode? {
        // Parse the OpenAPI schema JSON.
        val objectMapper: ObjectMapper = jacksonObjectMapper()
        val rootNode: JsonNode = objectMapper.readTree(schemaJson)

        // Navigate to the specified path and check for parameters.
        val parametersNode: JsonNode = rootNode["pathItems"]?.get(path)?.get("get")?.get("parameters")
            ?: return null

        // Find the parameter by name.
        return parametersNode.firstOrNull { it["name"].asText() == parameterName }
    }
}
