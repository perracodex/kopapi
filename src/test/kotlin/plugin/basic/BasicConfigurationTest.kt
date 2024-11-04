/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package plugin.basic

import io.github.perracodex.kopapi.plugin.Kopapi
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.github.perracodex.kopapi.types.SwaggerOperationsSorter
import io.github.perracodex.kopapi.types.SwaggerSyntaxTheme
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
        application {
            // Set some non-default configuration values.
            install(Kopapi) {
                debugUrl = "openapi/debug4"
                enableLogging = false

                apiDocs {
                    openapiYamlUrl = "openapi/yaml2"
                    openapiJsonUrl = "openapi/json1"
                    redocUrl = "openapi/redoc5"

                    swagger {
                        url = "swagger3"
                        withCredentials = true
                        operationsSorter = SwaggerOperationsSorter.METHOD
                        syntaxTheme = SwaggerSyntaxTheme.IDEA
                    }
                }
            }

            val plugin: PluginInstance = plugin(Kopapi)
            assertNotNull(
                actual = plugin,
                message = "Expected Kopapi plugin to be non-null."
            )

            assertEquals(
                expected = "openapi/json1",
                actual = SchemaRegistry.apiConfiguration?.apiDocs?.openapiJsonUrl,
                message = "Expected JSON URL to match."
            )
            assertEquals(
                expected = "openapi/yaml2",
                actual = SchemaRegistry.apiConfiguration?.apiDocs?.openapiYamlUrl,
                message = "Expected YAML URL to match."
            )
            assertEquals(
                expected = "swagger3",
                actual = SchemaRegistry.apiConfiguration?.apiDocs?.swagger?.url,
                message = "Expected Swagger URL to match."
            )
            assertEquals(
                expected = "openapi/debug4",
                actual = SchemaRegistry.apiConfiguration?.debugUrl,
                message = "Expected debug URL to match."
            )
            assertEquals(
                expected = "openapi/redoc5",
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
                debugUrl = "openapi/debug4"
                enableLogging = false

                apiDocs {
                    openapiYamlUrl = "openapi/yaml2"
                    openapiJsonUrl = "openapi/json1"
                    redocUrl = "openapi/redoc5"

                    swagger {
                        url = "swagger3"
                        withCredentials = true
                        operationsSorter = SwaggerOperationsSorter.METHOD
                        syntaxTheme = SwaggerSyntaxTheme.IDEA
                    }
                }
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
