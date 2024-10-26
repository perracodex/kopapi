/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package api.parameters

import com.fasterxml.jackson.databind.JsonNode
import io.github.perracodex.kopapi.composer.SchemaRegistry
import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.operation.elements.ApiParameter
import io.github.perracodex.kopapi.plugin.Kopapi
import io.github.perracodex.kopapi.types.PathType
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PathParameterTest {

    @BeforeEach
    fun reset() {
        SchemaRegistry.clear()
    }

    @Test
    fun `path parameter with simple types`() = testApplication {
        application {
            // Set some non-default configuration values.
            install(Kopapi) {
                openapiJsonUrl = "openapi/json1"
                openapiYamlUrl = "openapi/yaml2"
                swaggerUrl = "swagger3"
                debugUrl = "openapi/debug4"
            }

            routing {
                get("/path/{id}") {
                    // Do nothing.
                } api {
                    pathParameter<PathType.String>(name = "id") {
                        description = "The ID of the resource."
                        deprecated = false
                    }
                    response(status = HttpStatusCode.OK)
                }
            }

            // Get the generated OpenAPI schema in JSON format.
            val schemaJson: String = SchemaRegistry.getOpenApiSchema(
                format = SchemaRegistry.Format.JSON
            )

            val idParameter: JsonNode? = ParameterTestUtils.findParameter(
                schemaJson = schemaJson,
                path = "/path/{id}",
                parameterName = "id"
            )

            // Assert that the parameter exists and has the correct details.
            assertNotNull(actual = idParameter, message = "Expected 'id' parameter to be defined.")
            assertEquals(expected = ApiParameter.Location.PATH.value, actual = idParameter["in"].asText())
            assertEquals(expected = "The ID of the resource.", actual = idParameter["description"].asText())
            assertEquals(expected = PathType.String.apiType.value, actual = idParameter["schema"]["type"].asText())
            assertEquals(expected = PathType.String.apiFormat?.value, actual = idParameter["schema"]["format"]?.asText())
            assertEquals(expected = true, actual = idParameter["required"].asBoolean())
        }
    }
}
