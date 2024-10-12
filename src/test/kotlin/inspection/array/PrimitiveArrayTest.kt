/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package inspection.array

import io.github.perracodex.kopapi.inspector.TypeSchemaProvider
import io.github.perracodex.kopapi.inspector.schema.Schema
import io.github.perracodex.kopapi.inspector.schema.TypeSchema
import io.github.perracodex.kopapi.keys.ApiFormat
import io.github.perracodex.kopapi.keys.ApiType
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimitiveArrayTest {

    private data class PrimitiveArrayInfo(
        val kClass: KClass<*>,
        val type: ApiType,
        val format: ApiFormat?
    )

    @Test
    fun `test primitive arrays`() {
        // Define a list of primitive array types and their expected ApiType and ApiFormat.
        val primitiveTypes: List<PrimitiveArrayInfo> = listOf(
            PrimitiveArrayInfo(kClass = IntArray::class, type = ApiType.INTEGER, ApiFormat.INT32),
            PrimitiveArrayInfo(kClass = LongArray::class, type = ApiType.INTEGER, ApiFormat.INT64),
            PrimitiveArrayInfo(kClass = FloatArray::class, type = ApiType.NUMBER, ApiFormat.FLOAT),
            PrimitiveArrayInfo(kClass = DoubleArray::class, type = ApiType.NUMBER, ApiFormat.DOUBLE),
            PrimitiveArrayInfo(kClass = ShortArray::class, type = ApiType.INTEGER, ApiFormat.INT32),
            PrimitiveArrayInfo(kClass = ByteArray::class, type = ApiType.STRING, ApiFormat.BYTE),
            PrimitiveArrayInfo(kClass = CharArray::class, type = ApiType.STRING, format = null),
            PrimitiveArrayInfo(kClass = BooleanArray::class, type = ApiType.BOOLEAN, format = null),
            PrimitiveArrayInfo(kClass = UIntArray::class, type = ApiType.INTEGER, ApiFormat.INT32),
            PrimitiveArrayInfo(kClass = ULongArray::class, type = ApiType.INTEGER, ApiFormat.INT64),
            PrimitiveArrayInfo(kClass = UShortArray::class, type = ApiType.INTEGER, ApiFormat.INT32),
            PrimitiveArrayInfo(kClass = UByteArray::class, type = ApiType.STRING, ApiFormat.BYTE)
        )

        for (arrayInfo in primitiveTypes) {
            val arrayType: KType = arrayInfo.kClass.createType()

            // Inspect the type.
            val schemaProvider = TypeSchemaProvider()
            val typeSchema: TypeSchema = schemaProvider.inspect(kType = arrayType)

            // Verify that the TypeSchema is of type array.
            assertTrue(
                actual = typeSchema.schema is Schema.Array,
                message = "Expected schema to be a Schema.Array for ${arrayInfo.kClass.simpleName}"
            )

            // Retrieve the registered schemas.
            val schemasSet: Set<TypeSchema> = schemaProvider.getTypeSchemas()
            assertTrue(
                actual = schemasSet.isEmpty(),
                message = "Expected no registered schemas for ${arrayInfo.kClass.simpleName}"
            )

            // Verify that the items schema is a Schema.Primitive.
            assertTrue(
                actual = typeSchema.schema.items is Schema.Primitive,
                message = "Items schema should be a Schema.Primitive for ${arrayInfo.kClass.simpleName}"
            )

            // Check the expected ApiType.
            assertEquals(
                expected = arrayInfo.type,
                actual = typeSchema.schema.items.apiType,
                message = "Items type mismatch for ${arrayInfo.kClass.simpleName}"
            )

            // Check the expected ApiFormat.
            assertEquals(
                expected = arrayInfo.format?.value,
                actual = typeSchema.schema.items.format,
                message = "Items format mismatch for ${arrayInfo.kClass.simpleName}"
            )
        }
    }
}
