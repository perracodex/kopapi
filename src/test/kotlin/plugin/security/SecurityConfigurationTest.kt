/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package plugin.security

import io.github.perracodex.kopapi.dsl.operation.element.ApiSecurityScheme
import io.github.perracodex.kopapi.plugin.Kopapi
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.ktor.server.application.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Suppress("SameParameterValue")
class SecurityConfigurationTest {

    @BeforeEach
    fun reset() {
        SchemaRegistry.clear()
    }

    @Test
    fun `test plugin Security configuration`() = testApplication {
        application {
            install(Kopapi) {
                oauth2Security("oauth2") {
                    description = "OAuth2 security scheme."

                    authorizationCode {
                        authorizationUrl = "https://example.com/auth"
                        tokenUrl = "https://example.com/token"
                        refreshUrl = "https://example.com/refresh"
                        scope(name = "read:employees", description = "Read Data")
                        scope(name = "write:employees", description = "Modify Data")
                    }

                    clientCredentials {
                        tokenUrl = "https://example.com/token"
                        scope(name = "admin:tools", description = "Administrate Tools")
                    }

                    implicit {
                        authorizationUrl = "https://example.com/auth"
                        scope(name = "view:projects", description = "View Projects")
                    }

                    password {
                        tokenUrl = "https://example.com/token"
                        scope(name = "access:reports", description = "Access Reports")
                    }
                }
            }

            val plugin: PluginInstance = plugin(Kopapi)
            assertNotNull(
                actual = plugin,
                message = "Expected Kopapi plugin to be non-null."
            )

            // Validate the security configuration exists.
            val security: Set<ApiSecurityScheme>? = SchemaRegistry.apiConfiguration?.apiSecuritySchemes
            assertNotNull(
                actual = security,
                message = "Expected security configuration to be non-null."
            )
            assertTrue(
                actual = security.isNotEmpty(),
                message = "Expected security configuration to be non-empty."
            )

            val scheme: ApiSecurityScheme = security.first()
            assertTrue(
                actual = scheme is ApiSecurityScheme.OAuth2,
                message = "Expected security scheme to be an OAuth2 scheme."
            )
            assertEquals(
                expected = "oauth2",
                actual = scheme.schemeName,
                message = "Expected security scheme name to match."
            )
            assertEquals(
                expected = "OAuth2 security scheme.",
                actual = scheme.description,
                message = "Expected security scheme description to match."
            )

            // Verify flows exist.
            val flows: ApiSecurityScheme.OAuth2.OAuthFlows = scheme.flows
            assertNotNull(
                actual = flows,
                message = "Expected OAuth2 flows to be non-null."
            )
            assertTrue(
                actual = flows.hasAtLeastOneFlow(),
                message = "Expected at least one OAuth2 flow to be specified."
            )
            assertNotNull(
                actual = flows.authorizationCode,
                message = "Expected authorization code flow to be non-null."
            )
            assertNotNull(
                actual = flows.clientCredentials,
                message = "Expected client credentials flow to be non-null."
            )
            assertNotNull(
                actual = flows.implicit,
                message = "Expected implicit flow to be non-null."
            )
            assertNotNull(
                actual = flows.password,
                message = "Expected password flow to be non-null."
            )

            // Verify scopes exist.
            val authCodeScopes: Map<String, String> = flows.authorizationCode.scopes
            assertEquals(
                expected = 2,
                actual = authCodeScopes.size,
                message = "Expected authorization code scopes to match."
            )
            assertEquals(
                expected = "Read Data",
                actual = authCodeScopes["read:employees"],
                message = "Expected read:employees scope to match."
            )
            assertEquals(
                expected = "Modify Data",
                actual = authCodeScopes["write:employees"],
                message = "Expected write:employees scope to match."
            )

            // Verify flow properties.
            assertEquals(
                expected = "https://example.com/auth",
                actual = flows.authorizationCode.authorizationUrl,
                message = "Expected authorization URL to match."
            )
            assertEquals(
                expected = "https://example.com/token",
                actual = flows.authorizationCode.tokenUrl,
                message = "Expected token URL to match."
            )
            assertEquals(
                expected = "https://example.com/refresh",
                actual = flows.authorizationCode.refreshUrl,
                message = "Expected refresh URL to match."
            )
        }
    }
}
