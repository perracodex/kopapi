/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package plugin.servers

import io.github.perracodex.kopapi.core.SchemaProvider
import io.github.perracodex.kopapi.plugin.Kopapi
import io.github.perracodex.kopapi.plugin.dsl.elements.ApiServerConfig
import io.github.perracodex.kopapi.plugin.dsl.elements.ApiServerVariable
import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ServerConfigurationTest {

    @Test
    fun `test plugin server configuration`() = testApplication {
        application {
            install(Kopapi) {
                servers {
                    add(urlString = "http://localhost:8080") {
                        description = "Local server for development."
                    }

                    add(urlString = "https://{environment}.example.com") {
                        description = "The server for the API with environment variable."
                        variable(name = "environment") {
                            description = "Specifies the environment (production, staging, etc.)."
                            defaultValue = "production"
                            choices = setOf("production", "staging", "development")
                        }
                        variable(name = "version") {
                            description = "The version of the API."
                            defaultValue = "v1"
                            choices = setOf("v1", "v2")
                        }
                    }

                    add(urlString = "https://{region}.api.example.com") {
                        description = "Server for the API by region."
                        variable(name = "region") {
                            description = "Specifies the region for the API (us, eu)."
                            defaultValue = "us"
                            choices = setOf("us", "eu")
                        }
                    }
                }
            }

            val plugin: PluginInstance = plugin(Kopapi)
            assertNotNull(
                actual = plugin,
                message = "Expected Kopapi plugin to be non-null."
            )

            // Validate the servers exist.
            val servers: Set<ApiServerConfig>? = SchemaProvider.apiServers
            assertNotNull(
                actual = servers,
                message = "Expected server configurations to be non-null."
            )
            assertEquals(
                expected = 3,
                actual = servers.size,
                message = "Expected 3 server configurations."
            )

            // Validate server configurations.
            val serverList: List<ApiServerConfig> = servers.toList()

            validateServerConfig(
                server = serverList[0],
                expectedUrl = "http://localhost:8080",
                expectedDescription = "Local server for development."
            )

            validateServerConfig(
                server = serverList[1],
                expectedUrl = "https://{environment}.example.com",
                expectedDescription = "The server for the API with environment variable.",
                expectedVariables = mapOf(
                    "environment" to VariableExpectation(
                        defaultValue = "production",
                        choices = setOf("production", "staging", "development")
                    ),
                    "version" to VariableExpectation(
                        defaultValue = "v1",
                        choices = setOf("v1", "v2")
                    )
                )
            )

            validateServerConfig(
                server = serverList[2],
                expectedUrl = "https://{region}.api.example.com",
                expectedDescription = "Server for the API by region.",
                expectedVariables = mapOf(
                    "region" to VariableExpectation(
                        defaultValue = "us",
                        choices = setOf("us", "eu")
                    )
                )
            )
        }
    }

    /**
     * Helper method to validate a server configuration.
     */
    private fun validateServerConfig(
        server: ApiServerConfig?,
        expectedUrl: String,
        expectedDescription: String,
        expectedVariables: Map<String, VariableExpectation>? = null
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

        expectedVariables?.forEach { (variableName, expectation) ->
            val variable: ApiServerVariable? = server.variables[variableName]
            assertNotNull(
                actual = variable,
                message = "Expected '$variableName' variable to be present."
            )
            assertEquals(
                expected = expectation.defaultValue,
                actual = variable.defaultValue,
                message = "Expected default value for '$variableName'."
            )
            assertEquals(
                expected = expectation.choices,
                actual = variable.choices,
                message = "Expected choices for '$variableName'."
            )
        }
    }

    /**
     * Data class to hold expectations for a server variable.
     */
    private data class VariableExpectation(
        val defaultValue: String,
        val choices: Set<String>
    )
}