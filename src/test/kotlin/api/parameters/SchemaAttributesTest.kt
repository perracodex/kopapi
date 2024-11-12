/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package api.parameters

import com.fasterxml.jackson.databind.JsonNode
import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.parameters.cookieParameter
import io.github.perracodex.kopapi.plugin.Kopapi
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.github.perracodex.kopapi.types.DefaultValue
import io.github.perracodex.kopapi.types.OpenApiFormat
import io.github.perracodex.kopapi.types.ParameterStyle
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SchemaAttributesTest {

    @BeforeEach
    fun reset() {
        SchemaRegistry.clear()
    }

    @Test
    fun `test parameter with various types and schema attributes`() = testApplication {
        application {
            install(Kopapi)

            routing {
                get("/path") {
                    // Do nothing.
                } api {
                    summary = "Testing various cookie parameter types"

                    // String type with string-specific attributes
                    cookieParameter<String>(name = "stringParam") {
                        description = "A string cookie parameter"
                        required = true
                        defaultValue = DefaultValue.ofString(value = "defaultString")
                        style = ParameterStyle.PIPE_DELIMITED
                        explode = true

                        schema {
                            format = "custom"
                            minLength = 5
                            maxLength = 10
                            pattern = "^[A-Za-z0-9_-]{5,10}$"
                            contentEncoding = "utf-8"
                            contentMediaType = "application/json"
                        }
                    }

                    // Integer type with numeric-specific attributes
                    cookieParameter<Int>(name = "intParam") {
                        description = "An integer cookie parameter"
                        required = true
                        defaultValue = DefaultValue.ofInt(value = 10)
                        style = ParameterStyle.PIPE_DELIMITED
                        explode = true

                        schema {
                            minimum = 0
                            maximum = 100
                            exclusiveMinimum = 1
                            exclusiveMaximum = 99
                            multipleOf = 5
                        }
                    }

                    // Array type with array-specific attributes
                    cookieParameter<List<String>>(name = "arrayParam") {
                        description = "An array cookie parameter"
                        required = true
                        defaultValue = DefaultValue.ofString(value = "defaultValue")
                        style = ParameterStyle.PIPE_DELIMITED
                        explode = true

                        schema {
                            minItems = 1
                            maxItems = 5
                            uniqueItems = true
                        }
                    }

                    response(status = HttpStatusCode.OK)
                }
            }

            val schemaJson: String = SchemaRegistry.getOpenApiSchema(
                format = OpenApiFormat.JSON,
                cacheAllFormats = false
            )

            // Validate "stringParam" attributes
            val stringParam: JsonNode? = ParameterTestUtils.findParameter(
                schemaJson = schemaJson,
                path = "/path",
                parameterName = "stringParam"
            )
            assertNotNull(actual = stringParam, message = "Expected 'stringParam' to be defined.")
            assertEquals(expected = "string", stringParam["schema"]["type"].asText())
            assertEquals(expected = "^[A-Za-z0-9_-]{5,10}$", stringParam["schema"]["pattern"].asText())
            assertEquals(expected = 5, stringParam["schema"]["minLength"].asInt())
            assertEquals(expected = 10, stringParam["schema"]["maxLength"].asInt())
            assertEquals(expected = "custom", stringParam["schema"]["format"].asText())

            // Validate "intParam" attributes
            val intParam: JsonNode? = ParameterTestUtils.findParameter(
                schemaJson = schemaJson,
                path = "/path",
                parameterName = "intParam"
            )
            assertNotNull(actual = intParam, message = "Expected 'intParam' to be defined.")
            assertEquals(expected = "integer", intParam["schema"]["type"].asText())
            assertEquals(expected = 0, intParam["schema"]["minimum"].asInt())
            assertEquals(expected = 100, intParam["schema"]["maximum"].asInt())
            assertEquals(expected = 1, intParam["schema"]["exclusiveMinimum"].asInt())
            assertEquals(expected = 99, intParam["schema"]["exclusiveMaximum"].asInt())
            assertEquals(expected = 5, intParam["schema"]["multipleOf"].asInt())

            // Validate "arrayParam" attributes
            val arrayParam: JsonNode? = ParameterTestUtils.findParameter(
                schemaJson = schemaJson,
                path = "/path",
                parameterName = "arrayParam"
            )
            assertNotNull(actual = arrayParam, message = "Expected 'arrayParam' to be defined.")
            assertEquals(expected = "array", arrayParam["schema"]["type"].asText())
            assertEquals(expected = 1, arrayParam["schema"]["minItems"].asInt())
            assertEquals(expected = 5, arrayParam["schema"]["maxItems"].asInt())
            assertEquals(expected = true, arrayParam["schema"]["uniqueItems"].asBoolean())
        }
    }
}
