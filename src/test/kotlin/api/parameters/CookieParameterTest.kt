/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package api.parameters

import com.fasterxml.jackson.databind.JsonNode
import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.operation.elements.ApiParameter
import io.github.perracodex.kopapi.plugin.Kopapi
import io.github.perracodex.kopapi.schema.SchemaRegistry
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

class CookieParameterTest {

    @BeforeEach
    fun reset() {
        SchemaRegistry.clear()
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
                    response(status = HttpStatusCode.OK)
                }
            }

            // Get the generated OpenAPI schema in JSON format.
            val schemaJson: String = SchemaRegistry.getOpenApiSchema(
                format = SchemaRegistry.Format.JSON
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
            assertEquals(expected = "defaultCookieValue", actual = cookieParameter["default"].asText())
            assertEquals(expected = ParameterStyle.PIPE_DELIMITED.value, actual = cookieParameter["style"].asText())
            assertNull(actual = cookieParameter["explode"], message = "Expected 'explode' to be null.")
            assertNull(actual = cookieParameter["deprecated"], message = "Expected 'deprecated' to be null.")
        }
    }
}
