/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package api.parameters

import com.fasterxml.jackson.databind.JsonNode
import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.operation.element.ApiParameter
import io.github.perracodex.kopapi.dsl.parameter.queryParameter
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

class QueryParameterTest {

    @BeforeEach
    fun reset() {
        SchemaRegistry.release()
    }

    @Test
    fun `query string parameter`() = testApplication {
        application {
            install(Kopapi)

            routing {
                get("/path") {
                    // Do nothing.
                } api {
                    queryParameter<String>(name = "queryParam") {
                        description = "A query parameter with various properties"
                        required = true
                        allowReserved = true
                        defaultValue = DefaultValue.ofString(value = "defaultValue")
                        style = ParameterStyle.SIMPLE
                        explode = false
                        deprecated = true
                    }
                    response(status = HttpStatusCode.OK)
                }
            }

            // Get the generated OpenAPI schema in JSON format.
            val schemaJson: String = SchemaRegistry.getOpenApiSchema(
                format = OpenApiFormat.JSON,
                cacheAllFormats = false
            )

            // Find the query parameter in the OpenAPI schema.
            val queryParameter: JsonNode? = ParameterTestUtils.findParameter(
                schemaJson = schemaJson,
                path = "/path",
                parameterName = "queryParam"
            )

            // Assert that the query parameter exists and has the correct details.
            assertNotNull(actual = queryParameter, message = "Expected 'queryParam' to be defined.")
            assertEquals(expected = "string", actual = queryParameter["schema"]["type"].asText())
            assertEquals(expected = ApiParameter.Location.QUERY.value, actual = queryParameter["in"].asText())
            assertEquals(expected = "A query parameter with various properties", actual = queryParameter["description"].asText())
            assertEquals(expected = true, actual = queryParameter["required"].asBoolean())
            assertEquals(expected = true, actual = queryParameter["allowReserved"].asBoolean())
            assertEquals(expected = "defaultValue", actual = queryParameter["schema"]["default"].asText())
            assertEquals(expected = ParameterStyle.SIMPLE.value, actual = queryParameter["style"].asText())
            assertNull(actual = queryParameter["explode"], message = "Expected 'explode' to be null.")
            assertEquals(expected = true, actual = queryParameter["deprecated"].asBoolean())
        }
    }
}
