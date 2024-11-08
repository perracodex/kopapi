/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package plugin.basic

import io.github.perracodex.kopapi.dsl.plugin.builders.ApiDocsBuilder
import io.github.perracodex.kopapi.plugin.Kopapi
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.github.perracodex.kopapi.types.SwaggerDocExpansion
import io.github.perracodex.kopapi.types.SwaggerOperationsSorter
import io.github.perracodex.kopapi.types.SwaggerSyntaxTheme
import io.github.perracodex.kopapi.types.SwaggerUiTheme
import io.ktor.server.application.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BasicConfigurationTest {

    @BeforeEach
    fun reset() {
        SchemaRegistry.clear()
    }

    @Test
    fun `test plugin basic configuration (enabled)`() = testApplication {
        val openApiUrlValue = "api"
        val swaggerUrlValue = "swagger123"
        val redocUrlValue = "openapi/redoc5"
        val debugUrlValue = "openapi/debug4"

        application {
            // Set some non-default configuration values.
            install(Kopapi) {
                debugUrl = debugUrlValue
                enableLogging = false

                apiDocs {
                    openapiUrl = openApiUrlValue
                    redocUrl = redocUrlValue

                    swagger {
                        url = swaggerUrlValue
                        persistAuthorization = true
                        withCredentials = true
                        docExpansion = SwaggerDocExpansion.LIST
                        displayRequestDuration = true
                        displayOperationId = true
                        operationsSorter = SwaggerOperationsSorter.METHOD
                        uiTheme = SwaggerUiTheme.DARK
                        syntaxTheme = SwaggerSyntaxTheme.IDEA
                        includeErrors = true
                    }
                }
            }

            val plugin: PluginInstance = plugin(Kopapi)
            assertNotNull(
                actual = plugin,
                message = "Expected Kopapi plugin to be non-null."
            )

            assertEquals(
                expected = "/$openApiUrlValue/${ApiDocsBuilder.DEFAULT_OPENAPI_FILENAME}${ApiDocsBuilder.YAML_EXTENSION}",
                actual = SchemaRegistry.apiConfiguration?.apiDocs?.openapiYamlUrl,
                message = "Expected YAML URL to match."
            )
            assertEquals(
                expected = "/$openApiUrlValue/${ApiDocsBuilder.DEFAULT_OPENAPI_FILENAME}${ApiDocsBuilder.JSON_EXTENSION}",
                actual = SchemaRegistry.apiConfiguration?.apiDocs?.openapiJsonUrl,
                message = "Expected JSON URL to match."
            )
            assertEquals(
                expected = "/$swaggerUrlValue",
                actual = SchemaRegistry.apiConfiguration?.apiDocs?.swagger?.url,
                message = "Expected Swagger URL to match."
            )
            assertEquals(
                expected = "/$debugUrlValue",
                actual = SchemaRegistry.apiConfiguration?.debugUrl,
                message = "Expected debug URL to match."
            )
            assertEquals(
                expected = "/$redocUrlValue",
                actual = SchemaRegistry.apiConfiguration?.apiDocs?.redocUrl,
                message = "Expected Redoc URL to match."
            )
        }
    }

    @Test
    fun `test plugin basic configuration (disabled)`() = testApplication {
        application {
            // Set some non-default configuration values.
            install(Kopapi) {
                enabled = false
            }

            val plugin: PluginInstance = plugin(Kopapi)
            assertNotNull(
                actual = plugin,
                message = "Expected Kopapi plugin to be non-null."
            )

            assertNull(
                actual = SchemaRegistry.apiConfiguration,
                message = "Expected API configuration to be null."
            )
        }
    }
}
