/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package api.parameters

import com.fasterxml.jackson.databind.JsonNode
import io.github.perracodex.kopapi.composer.SchemaRegistry
import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.operation.elements.ApiParameter
import io.github.perracodex.kopapi.plugin.Kopapi
import io.github.perracodex.kopapi.types.DefaultValue
import io.github.perracodex.kopapi.types.ParameterStyle
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class HeaderParameterTest {

    @BeforeEach
    fun reset() {
        SchemaRegistry.clear()
    }

    @Test
    fun `test header parameter`() = testApplication {
        application {
            // Install Kopapi plugin.
            install(Kopapi) {
                openapiJsonUrl = "openapi/json1"
                openapiYamlUrl = "openapi/yaml2"
                swaggerUrl = "swagger3"
                debugUrl = "openapi/debug4"
            }

            routing {
                get("/path") {
                    // Do nothing.
                } api {
                    headerParameter<String>(name = "headerParam") {
                        description = "A header parameter with various properties"
                        required = true
                        defaultValue = DefaultValue.ofString(value = "defaultHeaderValue")
                        style = ParameterStyle.LABEL
                        deprecated = true
                    }
                    response(status = HttpStatusCode.OK)
                }
            }

            // Get the generated OpenAPI schema in JSON format.
            val schemaJson: String = SchemaRegistry.getOpenApiSchema(
                format = SchemaRegistry.Format.JSON
            )

            // Find the header parameter in the OpenAPI schema.
            val headerParameter: JsonNode? = ParameterTestUtils.findParameter(
                schemaJson = schemaJson,
                path = "/path",
                parameterName = "headerParam"
            )

            // Assert that the header parameter exists and has the correct details.
            assertNotNull(actual = headerParameter, message = "Expected 'headerParam' to be defined.")
            assertEquals(expected = "string", actual = headerParameter["schema"]["type"].asText())
            assertEquals(expected = ApiParameter.Location.HEADER.value, actual = headerParameter["in"].asText())
            assertEquals(expected = "A header parameter with various properties", actual = headerParameter["description"].asText())
            assertEquals(expected = true, actual = headerParameter["required"].asBoolean())
            assertEquals(expected = "defaultHeaderValue", actual = headerParameter["default"].asText())
            assertEquals(expected = ParameterStyle.LABEL.value, actual = headerParameter["style"].asText())
            assertNull(actual = headerParameter["explode"], message = "Expected 'explode' to be null.")
            assertEquals(expected = true, actual = headerParameter["deprecated"].asBoolean())
        }
    }
}