/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package swagger

import io.github.perracodex.kopapi.dsl.plugin.builder.ApiDocsBuilder
import io.github.perracodex.kopapi.dsl.plugin.builder.SwaggerBuilder
import io.github.perracodex.kopapi.dsl.plugin.element.ApiDocs
import io.github.perracodex.kopapi.plugin.Kopapi
import io.github.perracodex.kopapi.provider.SwaggerProvider
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
        SchemaRegistry.release()
    }

    @Test
    fun `verify initializer`() = testApplication {
        installPlugin()

        val response: HttpResponse = client.get(urlString = "${SwaggerBuilder.DEFAULT_SWAGGER_URL}/swagger-initializer.js")
        assertEquals(actual = HttpStatusCode.OK, expected = response.status)

        val apiDocs: ApiDocs = ApiDocsBuilder().build()
        val expected: String = SwaggerProvider.getSwaggerInitializer(apiDocs = apiDocs)
        val content: String = response.bodyAsText()
        assertEquals(expected = expected, actual = content)
    }

    private fun ApplicationTestBuilder.installPlugin() {
        install(plugin = Kopapi) {}
    }
}
