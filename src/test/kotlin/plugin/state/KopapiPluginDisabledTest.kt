/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package plugin.state

import io.github.perracodex.kopapi.dsl.operation.element.ApiSecurityScheme
import io.github.perracodex.kopapi.dsl.plugin.element.ApiInfo
import io.github.perracodex.kopapi.dsl.plugin.element.ApiServerConfig
import io.github.perracodex.kopapi.plugin.Kopapi
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.ktor.server.application.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class KopapiPluginDisabledTest {

    @BeforeEach
    fun reset() {
        SchemaRegistry.release()
    }

    @Test
    fun `test plugin disabled - apiInfo and servers are discarded`() = testApplication {
        application {
            // Install the Kopapi plugin with `enabled = false`
            install(Kopapi) {
                enabled = false  // Plugin is disabled.

                // Configure the API information.
                info {
                    title = "Disabled API"
                    description = "This API should not be registered because the plugin is disabled."
                    version = "1.0.0"
                }

                // Configure servers.
                servers {
                    add("https://disabled.example.com") {
                        description = "Server for the disabled plugin."
                    }
                }
            }

            val plugin: PluginInstance = plugin(Kopapi)
            assertNotNull(
                actual = plugin,
                message = "Expected Kopapi plugin to be non-null even when disabled."
            )

            // Ensure that ApiInfo is not set (should be null)
            val apiInfo: ApiInfo? = SchemaRegistry.apiConfiguration?.apiInfo
            assertNull(
                actual = apiInfo,
                message = "Expected ApiInfo to be null when plugin is disabled."
            )

            // Ensure that servers are not set (should be null)
            val servers: Set<ApiServerConfig>? = SchemaRegistry.apiConfiguration?.apiServers
            assertNull(
                actual = servers,
                message = "Expected servers to be null when plugin is disabled."
            )

            // Ensure that top-level security schemes are not set (should be null)
            val securitySchemes: Set<ApiSecurityScheme>? = SchemaRegistry.apiConfiguration?.apiSecuritySchemes
            assertNull(
                actual = securitySchemes,
                message = "Expected security schemes to be null when plugin is disabled."
            )
        }
    }
}
