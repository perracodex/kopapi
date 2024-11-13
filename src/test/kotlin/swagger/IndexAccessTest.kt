/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package swagger

import io.github.perracodex.kopapi.dsl.plugin.builder.SwaggerBuilder
import io.github.perracodex.kopapi.plugin.Kopapi
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IndexAccessTest {

    @BeforeEach
    fun reset() {
        SchemaRegistry.clear()
    }

    @Test
    fun `direct access to index endpoint`() = testApplication {
        installPlugin()

        val response: HttpResponse = client.get(urlString = "${SwaggerBuilder.DEFAULT_SWAGGER_URL}/index.html")
        assertEquals(expected = HttpStatusCode.OK, actual = response.status)

        val content: String = response.bodyAsText()
        assertTrue(actual = content.contains(other = "<body>"))
        assertTrue(actual = content.contains(other = "swagger-initializer.js"))
    }

    private fun ApplicationTestBuilder.installPlugin() {
        install(plugin = Kopapi) {}
    }
}
