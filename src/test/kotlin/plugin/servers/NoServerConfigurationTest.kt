/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package plugin.servers

import io.github.perracodex.kopapi.core.SchemaProvider
import io.github.perracodex.kopapi.plugin.Kopapi
import io.github.perracodex.kopapi.plugin.dsl.builders.ServerBuilder
import io.github.perracodex.kopapi.plugin.dsl.elements.ApiServerConfig
import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class NoServerConfigurationTest {

    @Test
    fun `test plugin no server configuration, expecting a default server`() = testApplication {
        application {
            install(Kopapi) {
                // No server configuration.
                // It should add a default server configuration.
            }

            val plugin: PluginInstance = plugin(Kopapi)
            assertNotNull(
                actual = plugin,
                message = "Expected Kopapi plugin to be non-null."
            )

            val servers: Set<ApiServerConfig>? = SchemaProvider.apiServers
            assertNotNull(
                actual = servers,
                message = "Expected server configurations to be non-null."
            )

            assertEquals(
                expected = 1,
                actual = servers.size,
                message = "Expected 1 default server configuration."
            )

            val defaultServer: ApiServerConfig = ServerBuilder.defaultServer()
            validateServerConfig(
                server = servers.first(),
                expectedUrl = defaultServer.url.toString(),
                expectedDescription = defaultServer.description
            )
        }
    }

    /**
     * Helper method to validate a server configuration.
     */
    private fun validateServerConfig(
        server: ApiServerConfig?,
        expectedUrl: String,
        expectedDescription: String?,
    ) {
        assertNotNull(
            actual = server,
            message = "Expected server configuration to be non-null."
        )
        assertEquals(
            expected = expectedUrl,
            actual = server.url.toString(),
            message = "Expected server URL to match."
        )
        assertEquals(
            expected = expectedDescription,
            actual = server.description,
            message = "Expected server description to match."
        )
    }
}