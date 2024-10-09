/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package inspection.array

import io.github.perracodex.kopapi.inspector.TypeInspector
import io.github.perracodex.kopapi.inspector.schema.Schema
import io.github.perracodex.kopapi.inspector.schema.TypeSchema
import io.github.perracodex.kopapi.keys.DataFormat
import io.github.perracodex.kopapi.keys.DataType
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimitiveArrayTest {

    private data class PrimitiveArrayInfo(
        val kClass: KClass<*>,
        val type: DataType,
        val format: DataFormat?
    )

    @Test
    fun `test primitive arrays`() {
        // Define a list of primitive array types and their expected DataType and DataFormat.
        val primitiveTypes: List<PrimitiveArrayInfo> = listOf(
            PrimitiveArrayInfo(kClass = IntArray::class, type = DataType.INTEGER, DataFormat.INT32),
            PrimitiveArrayInfo(kClass = LongArray::class, type = DataType.INTEGER, DataFormat.INT64),
            PrimitiveArrayInfo(kClass = FloatArray::class, type = DataType.NUMBER, DataFormat.FLOAT),
            PrimitiveArrayInfo(kClass = DoubleArray::class, type = DataType.NUMBER, DataFormat.DOUBLE),
            PrimitiveArrayInfo(kClass = ShortArray::class, type = DataType.INTEGER, DataFormat.INT32),
            PrimitiveArrayInfo(kClass = ByteArray::class, type = DataType.STRING, DataFormat.BYTE),
            PrimitiveArrayInfo(kClass = CharArray::class, type = DataType.STRING, format = null),
            PrimitiveArrayInfo(kClass = BooleanArray::class, type = DataType.BOOLEAN, format = null),
            PrimitiveArrayInfo(kClass = UIntArray::class, type = DataType.INTEGER, DataFormat.INT32),
            PrimitiveArrayInfo(kClass = ULongArray::class, type = DataType.INTEGER, DataFormat.INT64),
            PrimitiveArrayInfo(kClass = UShortArray::class, type = DataType.INTEGER, DataFormat.INT32),
            PrimitiveArrayInfo(kClass = UByteArray::class, type = DataType.STRING, DataFormat.BYTE)
        )

        for (arrayInfo in primitiveTypes) {
            val arrayType: KType = arrayInfo.kClass.createType()

            // Initialize the TypeInspector.
            val inspector = TypeInspector()
            val typeSchema: TypeSchema = inspector.inspect(kType = arrayType)

            // Verify that the TypeSchema is of type array.
            assertTrue(
                actual = typeSchema.schema is Schema.Array,
                message = "Expected schema to be a Schema.Array for ${arrayInfo.kClass.simpleName}"
            )

            // Retrieve the registered schemas.
            val schemasSet: Set<TypeSchema> = inspector.getTypeSchemas()
            assertTrue(
                actual = schemasSet.isEmpty(),
                message = "Expected no registered schemas for ${arrayInfo.kClass.simpleName}"
            )

            // Verify that the items schema is a Schema.Primitive.
            assertTrue(
                actual = typeSchema.schema.items is Schema.Primitive,
                message = "Items schema should be a Schema.Primitive for ${arrayInfo.kClass.simpleName}"
            )

            // Check the expected DataType.
            assertEquals(
                expected = arrayInfo.type,
                actual = typeSchema.schema.items.type,
                message = "Items type mismatch for ${arrayInfo.kClass.simpleName}"
            )

            // Check the expected DataFormat.
            assertEquals(
                expected = arrayInfo.format?.value,
                actual = typeSchema.schema.items.format,
                message = "Items format mismatch for ${arrayInfo.kClass.simpleName}"
            )
        }
    }
}
