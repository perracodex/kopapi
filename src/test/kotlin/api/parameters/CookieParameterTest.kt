/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package api.parameters

import com.fasterxml.jackson.databind.JsonNode
import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.operation.element.ApiParameter
import io.github.perracodex.kopapi.dsl.parameter.cookieParameter
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

class CookieParameterTest {

    @BeforeEach
    fun reset() {
        SchemaRegistry.release()
    }

    @Test
    fun `cookie parameter`() = testApplication {
        application {
            install(Kopapi)

            routing {
                get("/path") {
                    // Do nothing.
                } api {
                    summary = "A simple API endpoint"
                    cookieParameter<String>(name = "cookieParam") {
                        description = "A cookie parameter with various properties"
                        required = true
                        defaultValue = DefaultValue.ofString(value = "defaultCookieValue")
                        style = ParameterStyle.PIPE_DELIMITED
                        explode = true
                        deprecated = false
                    }
                    response(status = HttpStatusCode.OK) {
                        links {
                            add(name = "GetEmployeeDetails") {
                                operationRef = "/api/v1/employee/{employee_id}"
                                description = "Retrieve information about this employee."
                                parameter(name = "employee_id", value = "\$request. path. employee_id")
                            }
                        }
                    }
                    noSecurity()
                }
            }

            // Get the generated OpenAPI schema in JSON format.
            val schemaJson: String = SchemaRegistry.getOpenApiSchema(
                format = OpenApiFormat.JSON,
                cacheAllFormats = false
            )

            // Find the cookie parameter in the OpenAPI schema.
            val cookieParameter: JsonNode? = ParameterTestUtils.findParameter(
                schemaJson = schemaJson,
                path = "/path",
                parameterName = "cookieParam"
            )

            // Assert that the cookie parameter exists and has the correct details.
            assertNotNull(actual = cookieParameter, message = "Expected 'cookieParam' to be defined.")
            assertEquals(expected = "string", actual = cookieParameter["schema"]["type"].asText())
            assertEquals(expected = ApiParameter.Location.COOKIE.value, actual = cookieParameter["in"].asText())
            assertEquals(expected = "A cookie parameter with various properties", actual = cookieParameter["description"].asText())
            assertEquals(expected = true, actual = cookieParameter["required"].asBoolean())
            assertEquals(expected = "defaultCookieValue", actual = cookieParameter["schema"]["default"].asText())
            assertEquals(expected = ParameterStyle.PIPE_DELIMITED.value, actual = cookieParameter["style"].asText())
            assertNull(actual = cookieParameter["explode"], message = "Expected 'explode' to be null.")
            assertNull(actual = cookieParameter["deprecated"], message = "Expected 'deprecated' to be null.")
        }
    }
}
