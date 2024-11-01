/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package inspection.custom

import io.github.perracodex.kopapi.dsl.operation.api
import io.github.perracodex.kopapi.inspector.TypeSchemaProvider
import io.github.perracodex.kopapi.inspector.schema.TypeSchema
import io.github.perracodex.kopapi.plugin.Kopapi
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.github.perracodex.kopapi.schema.facets.ElementSchema
import io.github.perracodex.kopapi.types.ApiType
import io.github.perracodex.kopapi.utils.safeName
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.BeforeEach
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.test.Test
import kotlin.test.assertTrue

class CustomTypeTest {

    @BeforeEach
    fun reset() {
        SchemaRegistry.clear()
    }

    @Test
    fun `custom type with custom data class`() = testApplication {
        application {
            // Install Kopapi plugin.
            install(Kopapi) {
                addType<Data>(type = ApiType.STRING) {
                    minLength = 1
                    maxLength = 100
                }
            }

            routing {
                get("/path") {
                    // Do nothing.
                } api {
                    pathParameter<Data>(name = "param")
                }
            }

            val typeName = "CustomTypeOfData"

            // Inspect the type.
            val schemaProvider = TypeSchemaProvider()
            val type: KType = Data::class.createType()
            val typeSchema: TypeSchema = schemaProvider.inspect(kType = type)

            // Verify it created a schema reference.
            assertTrue(
                actual = typeSchema.schema is ElementSchema.Reference,
                message = "Expected schema to be a ElementSchema.Object for ${type.safeName()}"
            )
            assertTrue(
                actual = typeSchema.schema.ref == "${ElementSchema.Reference.PATH}$typeName",
            )

            // Verify that the custom type schema.
            schemaProvider.getTypeSchemas().find {
                it.name == typeName
            }?.let { customTypeSchema ->
                assertTrue(
                    actual = customTypeSchema.schema is ElementSchema.Primitive,
                    message = "Expected schema to be a ElementSchema.Primitive for $typeName"
                )
                assertTrue(
                    actual = customTypeSchema.schema.minLength == 1,
                    message = "Expected minimum to be 1 for $typeName"
                )
                assertTrue(
                    actual = customTypeSchema.schema.maxLength == 100,
                    message = "Expected maximum to be 100 for $typeName"
                )
            }
        }
    }

    private data class Data(val value: String)
}
