/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package swagger

import io.github.perracodex.kopapi.plugin.Kopapi
import io.github.perracodex.kopapi.plugin.KopapiConfig
import io.github.perracodex.kopapi.plugin.Swagger
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals

class InitializerTest {

    @BeforeEach
    fun reset() {
        SchemaRegistry.clear()
    }

    @Test
    fun `verify initializer`() = testApplication {
        installPlugin()

        val response: HttpResponse = client.get(urlString = "${KopapiConfig.DEFAULT_SWAGGER_URL}/swagger-initializer.js")
        assertEquals(actual = HttpStatusCode.OK, expected = response.status)

        val expected: String = Swagger.getSwaggerInitializer(KopapiConfig.DEFAULT_SWAGGER_URL)
        val content: String = response.bodyAsText()
        assertEquals(expected = expected, actual = content)
    }

    private fun ApplicationTestBuilder.installPlugin() {
        install(plugin = Kopapi) {}
    }
}
