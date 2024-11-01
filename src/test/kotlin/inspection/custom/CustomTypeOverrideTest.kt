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
import io.github.perracodex.kopapi.types.ApiFormat
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

class CustomTypeOverrideTest {

    @BeforeEach
    fun reset() {
        SchemaRegistry.clear()
    }

    @Test
    fun `custom type overriding native primitive`() = testApplication {
        application {
            // Install Kopapi plugin.
            install(Kopapi) {
                addType<Long>(type = ApiType.NUMBER, format = ApiFormat.INT64) {
                    minimum = 1
                    maximum = 100
                    multipleOf = 10
                    exclusiveMinimum = 5
                    exclusiveMaximum = 10
                }
            }

            routing {
                get("/path") {
                    // Do nothing.
                } api {
                    pathParameter<Long>(name = "param")
                }
            }

            val typeName = "CustomTypeOfLong"

            // Inspect the type.
            val schemaProvider = TypeSchemaProvider()
            val longType: KType = Long::class.createType()
            val typeSchema: TypeSchema = schemaProvider.inspect(kType = longType)

            // Verify it created a schema reference.
            assertTrue(
                actual = typeSchema.schema is ElementSchema.Reference,
                message = "Expected schema to be a ElementSchema.Object for ${longType.safeName()}"
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
                    actual = customTypeSchema.schema.minimum == 1,
                    message = "Expected minimum to be 1 for $typeName"
                )
                assertTrue(
                    actual = customTypeSchema.schema.maximum == 100,
                    message = "Expected maximum to be 100 for $typeName"
                )
                assertTrue(
                    actual = customTypeSchema.schema.multipleOf == 10,
                    message = "Expected multipleOf to be 10 for $typeName"
                )
                assertTrue(
                    actual = customTypeSchema.schema.exclusiveMinimum == 5,
                    message = "Expected exclusiveMinimum to be 5 for $typeName"
                )
                assertTrue(
                    actual = customTypeSchema.schema.exclusiveMaximum == 10,
                    message = "Expected exclusiveMaximum to be 10 for $typeName"
                )
            }
        }
    }
}
