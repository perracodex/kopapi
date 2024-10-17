/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package inspection.array

import io.github.perracodex.kopapi.inspector.TypeSchemaProvider
import io.github.perracodex.kopapi.inspector.schema.Schema
import io.github.perracodex.kopapi.inspector.schema.TypeSchema
import io.github.perracodex.kopapi.types.ApiFormat
import io.github.perracodex.kopapi.types.ApiType
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TypedPrimitiveArrayTest {

    private data class PrimitiveTypeInfo(
        val kClass: KClass<*>,
        val type: ApiType,
        val format: ApiFormat?
    )

    @Test
    fun `test typed primitive arrays`() {
        // Define a list of primitive types and their expected ApiType and ApiFormat.
        val primitiveTypes: List<PrimitiveTypeInfo> = listOf(
            PrimitiveTypeInfo(kClass = Int::class, type = ApiType.INTEGER, format = ApiFormat.INT32),
            PrimitiveTypeInfo(kClass = Long::class, type = ApiType.INTEGER, format = ApiFormat.INT64),
            PrimitiveTypeInfo(kClass = Float::class, type = ApiType.NUMBER, format = ApiFormat.FLOAT),
            PrimitiveTypeInfo(kClass = Double::class, type = ApiType.NUMBER, format = ApiFormat.DOUBLE),
            PrimitiveTypeInfo(kClass = Short::class, type = ApiType.INTEGER, format = ApiFormat.INT32),
            PrimitiveTypeInfo(kClass = Byte::class, type = ApiType.STRING, format = ApiFormat.BYTE),
            PrimitiveTypeInfo(kClass = UByte::class, type = ApiType.INTEGER, format = ApiFormat.INT32),
            PrimitiveTypeInfo(kClass = Char::class, type = ApiType.STRING, format = null),
            PrimitiveTypeInfo(kClass = Boolean::class, type = ApiType.BOOLEAN, format = null),
            PrimitiveTypeInfo(kClass = UByte::class, type = ApiType.INTEGER, format = ApiFormat.INT32),
            PrimitiveTypeInfo(kClass = UInt::class, type = ApiType.INTEGER, format = ApiFormat.INT32),
            PrimitiveTypeInfo(kClass = ULong::class, type = ApiType.INTEGER, format = ApiFormat.INT64),
            PrimitiveTypeInfo(kClass = UShort::class, type = ApiType.INTEGER, format = ApiFormat.INT32)
        )

        for (typeInfo in primitiveTypes) {
            val arrayType: KType = Array::class.createType(
                arguments = listOf(KTypeProjection.invariant(typeInfo.kClass.createType()))
            )

            // Inspect the type.
            val schemaProvider = TypeSchemaProvider()
            val typeSchema: TypeSchema = schemaProvider.inspect(kType = arrayType)

            // Verify that the TypeSchema is an Array schema.
            assertTrue(
                actual = typeSchema.schema is Schema.Array,
                message = "Expected schema to be a Schema.Array for Array<${typeInfo.kClass.simpleName}>"
            )

            // Retrieve the registered schemas.
            val schemasSet: Set<TypeSchema> = schemaProvider.getTypeSchemas()
            assertTrue(
                actual = schemasSet.isEmpty(),
                message = "Expected no schemas for Array<${typeInfo.kClass.simpleName}>"
            )

            // Verify that the items schema is a Schema.Primitive.
            assertTrue(
                actual = typeSchema.schema.items is Schema.Primitive,
                message = "Items schema should be a Schema.Primitive for Array<${typeInfo.kClass.simpleName}>"
            )

            // Check the expected ApiType.
            assertEquals(
                expected = typeInfo.type,
                actual = typeSchema.schema.items.schemaType,
                message = "Items type mismatch for Array<${typeInfo.kClass.simpleName}>"
            )

            // Check the expected ApiFormat.
            assertEquals(
                expected = typeInfo.format?.value,
                actual = typeSchema.schema.items.format,
                message = "Items format mismatch for Array<${typeInfo.kClass.simpleName}>"
            )
        }
    }
}