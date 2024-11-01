/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package plugin.basic

import io.github.perracodex.kopapi.plugin.Kopapi
import io.github.perracodex.kopapi.schema.SchemaRegistry
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
                openapiJsonUrl = "openapi/json1"
                openapiYamlUrl = "openapi/yaml2"
                swaggerUrl = "swagger3"
                debugUrl = "openapi/debug4"
                redocUrl = "openapi/redoc5"
            }

            val plugin: PluginInstance = plugin(Kopapi)
            assertNotNull(
                actual = plugin,
                message = "Expected Kopapi plugin to be non-null."
            )

            assertEquals(
                expected = "openapi/json1",
                actual = SchemaRegistry.apiConfiguration?.openapiJsonUrl,
                message = "Expected JSON URL to match."
            )
            assertEquals(
                expected = "openapi/yaml2",
                actual = SchemaRegistry.apiConfiguration?.openapiYamlUrl,
                message = "Expected YAML URL to match."
            )
            assertEquals(
                expected = "swagger3",
                actual = SchemaRegistry.apiConfiguration?.swaggerUrl,
                message = "Expected Swagger URL to match."
            )
            assertEquals(
                expected = "openapi/debug4",
                actual = SchemaRegistry.apiConfiguration?.debugUrl,
                message = "Expected debug URL to match."
            )
            assertEquals(
                expected = "openapi/redoc5",
                actual = SchemaRegistry.apiConfiguration?.redocUrl,
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
                openapiJsonUrl = "openapi/json1"
                openapiYamlUrl = "openapi/yaml2"
                swaggerUrl = "swagger3"
                debugUrl = "openapi/debug4"
                redocUrl = "openapi/redoc5"
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
