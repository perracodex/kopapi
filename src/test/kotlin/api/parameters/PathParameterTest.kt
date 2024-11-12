/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package api.parameters

import com.fasterxml.jackson.databind.JsonNode
import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.dsl.operation.elements.ApiParameter
import io.github.perracodex.kopapi.dsl.parameters.pathParameter
import io.github.perracodex.kopapi.plugin.Kopapi
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.github.perracodex.kopapi.types.ApiFormat
import io.github.perracodex.kopapi.types.ApiType
import io.github.perracodex.kopapi.types.OpenApiFormat
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.uuid.Uuid

class PathParameterTest {

    @BeforeEach
    fun reset() {
        SchemaRegistry.clear()
    }

    @Test
    fun `path parameter with simple types`() = testApplication {
        application {
            install(Kopapi)

            routing {
                get("/path/{id}") {
                    // Do nothing.
                } api {
                    pathParameter<Uuid>(name = "id") {
                        description = "The ID of the resource."
                        deprecated = false
                    }
                    response(status = HttpStatusCode.OK)
                }
            }

            // Get the generated OpenAPI schema in JSON format.
            val schemaJson: String = SchemaRegistry.getOpenApiSchema(
                format = OpenApiFormat.JSON,
                cacheAllFormats = false
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
            assertEquals(expected = ApiType.STRING.value, actual = idParameter["schema"]["type"].asText())
            assertEquals(expected = ApiFormat.UUID.value, actual = idParameter["schema"]["format"]?.asText())
            assertEquals(expected = true, actual = idParameter["required"].asBoolean())
        }
    }
}
