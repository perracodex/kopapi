/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package plugin.info

import io.github.perracodex.kopapi.dsl.plugin.element.ApiInfo
import io.github.perracodex.kopapi.plugin.Kopapi
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.github.perracodex.kopapi.serialization.SerializationUtils
import io.ktor.server.application.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Suppress("SameParameterValue")
class ApiInfoJsonTest {

    @BeforeEach
    fun reset() {
        SchemaRegistry.release()
    }

    @Test
    fun `test plugin API info raw Json`() = testApplication {
        application {
            install(Kopapi) {
                info {
                    title = "Test API"
                    description = "This is a test API."
                    version = "2.0.0"
                    termsOfService = "https://example.com/terms"

                    contact {
                        name = "John Doe"
                        url = "https://example.com/support"
                        email = "support@example.com"
                    }

                    license {
                        name = "MIT"
                        url = "https://opensource.org/licenses/MIT"
                    }
                }
            }

            val plugin: PluginInstance = plugin(Kopapi)
            assertNotNull(
                actual = plugin,
                message = "Expected Kopapi plugin to be non-null."
            )

            // Validate the API info exists.
            val jsonApiInfo: Set<String> = SchemaRegistry.getDebugSection(section = SchemaRegistry.Section.API_INFO)
            assertTrue(
                actual = jsonApiInfo.isNotEmpty(),
                message = "Expected API info to be empty."
            )

            val apiInfo: ApiInfo = SerializationUtils().fromRawJson(json = jsonApiInfo.first())
            validateApiInfo(
                info = apiInfo,
                expectedTitle = "Test API",
                expectedDescription = "This is a test API.",
                expectedVersion = "2.0.0",
                expectedTermsOfService = "https://example.com/terms"
            )
            validateContactInfo(
                contact = apiInfo.contact,
                expectedName = "John Doe",
                expectedUrl = "https://example.com/support",
                expectedEmail = "support@example.com"
            )
            validateLicenseInfo(
                license = apiInfo.license,
                expectedName = "MIT",
                expectedUrl = "https://opensource.org/licenses/MIT"
            )
        }
    }

    /**
     * Helper method to validate the API info configuration.
     */
    private fun validateApiInfo(
        info: ApiInfo?,
        expectedTitle: String,
        expectedDescription: String,
        expectedVersion: String,
        expectedTermsOfService: String?
    ) {
        assertNotNull(
            actual = info,
            message = "Expected API info to be non-null."
        )
        assertEquals(
            expected = expectedTitle,
            actual = info.title,
            message = "Expected API title to match."
        )
        assertEquals(
            expected = expectedDescription,
            actual = info.description,
            message = "Expected API description to match."
        )
        assertEquals(
            expected = expectedVersion,
            actual = info.version,
            message = "Expected API version to match."
        )
        assertEquals(
            expected = expectedTermsOfService,
            actual = info.termsOfService,
            message = "Expected terms of service URL to match."
        )
    }

    /**
     * Helper method to validate the contact information.
     */
    private fun validateContactInfo(
        contact: ApiInfo.Contact?,
        expectedName: String?,
        expectedUrl: String?,
        expectedEmail: String?
    ) {
        assertNotNull(
            actual = contact,
            message = "Expected contact info to be non-null."
        )
        assertEquals(
            expected = expectedName,
            actual = contact.name,
            message = "Expected contact name to match."
        )
        assertEquals(
            expected = expectedUrl,
            actual = contact.url,
            message = "Expected contact URL to match."
        )
        assertEquals(
            expected = expectedEmail,
            actual = contact.email,
            message = "Expected contact email to match."
        )
    }

    /**
     * Helper method to validate the license information.
     */
    private fun validateLicenseInfo(
        license: ApiInfo.License?,
        expectedName: String?,
        expectedUrl: String?
    ) {
        assertNotNull(
            actual = license,
            message = "Expected license info to be non-null."
        )
        assertEquals(
            expected = expectedName,
            actual = license.name,
            message = "Expected license name to match."
        )
        assertEquals(
            expected = expectedUrl,
            actual = license.url,
            message = "Expected license URL to match."
        )
    }
}
