/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package swagger

import io.github.perracodex.kopapi.plugin.Kopapi
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals

class SwaggerUrlTest {

    @BeforeEach
    fun reset() {
        SchemaRegistry.clear()
    }

    @Test
    fun `change default url`() = testApplication {
        val newUrl = "hello-swagger"
        installPlugin(urlString = newUrl)

        val client: HttpClient = createClient { followRedirects = false }
        val response: HttpResponse = client.get(urlString = newUrl)

        assertEquals(expected = HttpStatusCode.Found, actual = response.status)
        assertEquals(expected = "/$newUrl/index.html", actual = response.headers["Location"])
    }

    private fun ApplicationTestBuilder.installPlugin(urlString: String) {
        install(plugin = Kopapi) {
            apiDocs {
                swaggerUrl = urlString
            }
        }
    }
}
