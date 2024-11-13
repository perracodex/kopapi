/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package api.parameters

import com.fasterxml.jackson.databind.JsonNode
import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.operation.element.ApiParameter
import io.github.perracodex.kopapi.dsl.parameter.headerParameter
import io.github.perracodex.kopapi.plugin.Kopapi
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.github.perracodex.kopapi.type.DefaultValue
import io.github.perracodex.kopapi.type.OpenApiFormat
import io.github.perracodex.kopapi.type.ParameterStyle
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
    fun `header parameter`() = testApplication {
        application {
            install(Kopapi)

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
                    response(status = HttpStatusCode.OK) {
                        link(name = "GetEmployeeDetails") {
                            operationId = "getEmployeeDetails"
                            parameter(name = "employee_id", value = "\$request. path. employee_id")
                        }
                    }
                }
            }

            // Get the generated OpenAPI schema in JSON format.
            val schemaJson: String = SchemaRegistry.getOpenApiSchema(
                format = OpenApiFormat.JSON,
                cacheAllFormats = false
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
            assertEquals(expected = "defaultHeaderValue", actual = headerParameter["schema"]["default"].asText())
            assertEquals(expected = ParameterStyle.LABEL.value, actual = headerParameter["style"].asText())
            assertNull(actual = headerParameter["explode"], message = "Expected 'explode' to be null.")
            assertEquals(expected = true, actual = headerParameter["deprecated"].asBoolean())
        }
    }
}
