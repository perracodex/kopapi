/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package swagger

import io.github.perracodex.kopapi.plugin.Kopapi
import io.github.perracodex.kopapi.plugin.KopapiConfig
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals

class BaseUrlAccessTest {

    @BeforeEach
    fun reset() {
        SchemaRegistry.clear()
    }

    @Test
    fun `redirect from default url to index endpoint`() = testApplication {
        installPlugin()

        val client: HttpClient = createClient { followRedirects = false }
        val response: HttpResponse = client.get(urlString = KopapiConfig.DEFAULT_SWAGGER_URL)

        assertEquals(expected = HttpStatusCode.Found, actual = response.status)
        assertEquals(expected = "${KopapiConfig.DEFAULT_SWAGGER_URL}/index.html", actual = response.headers["Location"])
    }

    private fun ApplicationTestBuilder.installPlugin() {
        install(plugin = Kopapi) {}
    }
}
