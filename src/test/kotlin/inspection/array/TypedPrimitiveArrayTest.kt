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
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TypedPrimitiveArrayTest {

    private data class PrimitiveTypeInfo(
        val kClass: KClass<*>,
        val type: DataType,
        val format: DataFormat?
    )

    @Test
    fun `test typed primitive arrays`() {
        // Define a list of primitive types and their expected DataType and DataFormat.
        val primitiveTypes: List<PrimitiveTypeInfo> = listOf(
            PrimitiveTypeInfo(kClass = Int::class, type = DataType.INTEGER, format = DataFormat.INT32),
            PrimitiveTypeInfo(kClass = Long::class, type = DataType.INTEGER, format = DataFormat.INT64),
            PrimitiveTypeInfo(kClass = Float::class, type = DataType.NUMBER, format = DataFormat.FLOAT),
            PrimitiveTypeInfo(kClass = Double::class, type = DataType.NUMBER, format = DataFormat.DOUBLE),
            PrimitiveTypeInfo(kClass = Short::class, type = DataType.INTEGER, format = DataFormat.INT32),
            PrimitiveTypeInfo(kClass = Byte::class, type = DataType.STRING, format = DataFormat.BYTE),
            PrimitiveTypeInfo(kClass = UByte::class, type = DataType.INTEGER, format = DataFormat.INT32),
            PrimitiveTypeInfo(kClass = Char::class, type = DataType.STRING, format = null),
            PrimitiveTypeInfo(kClass = Boolean::class, type = DataType.BOOLEAN, format = null),
            PrimitiveTypeInfo(kClass = UByte::class, type = DataType.INTEGER, format = DataFormat.INT32),
            PrimitiveTypeInfo(kClass = UInt::class, type = DataType.INTEGER, format = DataFormat.INT32),
            PrimitiveTypeInfo(kClass = ULong::class, type = DataType.INTEGER, format = DataFormat.INT64),
            PrimitiveTypeInfo(kClass = UShort::class, type = DataType.INTEGER, format = DataFormat.INT32)
        )

        for (typeInfo in primitiveTypes) {
            val arrayType: KType = Array::class.createType(
                arguments = listOf(KTypeProjection.invariant(typeInfo.kClass.createType()))
            )

            // Initialize the TypeInspector.
            val inspector = TypeInspector()
            val typeSchema: TypeSchema = inspector.inspect(kType = arrayType)

            // Verify that the TypeSchema is an Array schema.
            assertTrue(
                actual = typeSchema.schema is Schema.Array,
                message = "Expected schema to be a Schema.Array for Array<${typeInfo.kClass.simpleName}>"
            )

            // Retrieve the registered schemas.
            val schemasSet: Set<TypeSchema> = inspector.getTypeSchemas()
            assertTrue(
                actual = schemasSet.isEmpty(),
                message = "Expected no schemas for Array<${typeInfo.kClass.simpleName}>"
            )

            // Verify that the items schema is a Schema.Primitive.
            assertTrue(
                actual = typeSchema.schema.items is Schema.Primitive,
                message = "Items schema should be a Schema.Primitive for Array<${typeInfo.kClass.simpleName}>"
            )

            // Check the expected DataType.
            assertEquals(
                expected = typeInfo.type,
                actual = typeSchema.schema.items.type,
                message = "Items type mismatch for Array<${typeInfo.kClass.simpleName}>"
            )

            // Check the expected DataFormat.
            assertEquals(
                expected = typeInfo.format?.value,
                actual = typeSchema.schema.items.format,
                message = "Items format mismatch for Array<${typeInfo.kClass.simpleName}>"
            )
        }
    }
}