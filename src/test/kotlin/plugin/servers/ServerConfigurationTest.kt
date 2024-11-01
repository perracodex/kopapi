/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package plugin.servers

import io.github.perracodex.kopapi.dsl.plugin.elements.ApiServerConfig
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiServerVariable
import io.github.perracodex.kopapi.plugin.Kopapi
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.ktor.server.application.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ServerConfigurationTest {

    @BeforeEach
    fun reset() {
        SchemaRegistry.clear()
    }

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
                        variable(name = "environment", defaultValue = "production") {
                            choices = setOf("production", "staging", "development")
                            description = "Specifies the environment (production, staging, etc.)."
                        }
                        variable(name = "version", defaultValue = "v1") {
                            choices = setOf("v1", "v2")
                            description = "The version of the API."
                        }
                    }

                    add(urlString = "https://{region}.api.example.com") {
                        description = "Server for the API by region."
                        variable(name = "region", defaultValue = "us") {
                            choices = setOf("us", "eu")
                            description = "Specifies the region for the API (us, eu)."
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
            val servers: Set<ApiServerConfig>? = SchemaRegistry.apiConfiguration?.apiServers
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
            actual = server.url,
            message = "Expected server URL to match."
        )
        assertEquals(
            expected = expectedDescription,
            actual = server.description,
            message = "Expected server description to match."
        )

        expectedVariables?.forEach { (variableName, expectation) ->
            val variable: ApiServerVariable? = server.variables?.get(variableName)
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
