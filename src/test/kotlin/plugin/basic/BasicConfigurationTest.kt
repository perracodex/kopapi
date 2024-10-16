/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package plugin.basic

import io.github.perracodex.kopapi.core.SchemaRegistry
import io.github.perracodex.kopapi.plugin.Kopapi
import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BasicConfigurationTest {

    @Test
    fun `test plugin basic configuration (enabled)`() = testApplication {
        application {
            // Set some non-default configuration values.
            install(Kopapi) {
                openapiJsonUrl = "openapi/json1"
                openapiYamlUrl = "openapi/yaml2"
                swaggerUrl = "swagger3"
                debugUrl = "openapi/debug4"
            }

            val plugin: PluginInstance = plugin(Kopapi)
            assertNotNull(
                actual = plugin,
                message = "Expected Kopapi plugin to be non-null."
            )

            assertEquals(
                expected = "openapi/json1",
                actual = SchemaRegistry.configuration?.openapiJsonUrl,
                message = "Expected JSON URL to match."
            )
            assertEquals(
                expected = "openapi/yaml2",
                actual = SchemaRegistry.configuration?.openapiYamlUrl,
                message = "Expected YAML URL to match."
            )
            assertEquals(
                expected = "swagger3",
                actual = SchemaRegistry.configuration?.swaggerUrl,
                message = "Expected Swagger URL to match."
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
            }

            val plugin: PluginInstance = plugin(Kopapi)
            assertNotNull(
                actual = plugin,
                message = "Expected Kopapi plugin to be non-null."
            )

            assertNull(
                actual = SchemaRegistry.configuration,
                message = "Expected configuration to be null."
            )
        }
    }
}
