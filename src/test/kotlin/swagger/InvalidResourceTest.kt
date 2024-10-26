/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package swagger

import io.github.perracodex.kopapi.composer.SchemaRegistry
import io.github.perracodex.kopapi.plugin.Kopapi
import io.github.perracodex.kopapi.plugin.KopapiConfig
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals

class InvalidResourceTest {

    @BeforeEach
    fun reset() {
        SchemaRegistry.clear()
    }

    @Test
    fun `access invalid resource`() = testApplication {
        installPlugin()

        val response: HttpResponse = client.get(urlString = "${KopapiConfig.DEFAULT_SWAGGER_URL}/hello-world")
        assertEquals(actual = HttpStatusCode.NotFound, expected = response.status)
    }

    private fun ApplicationTestBuilder.installPlugin() {
        install(plugin = Kopapi) {}
    }
}
