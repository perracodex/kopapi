/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package inspection.array

import io.github.perracodex.kopapi.inspector.TypeSchemaProvider
import io.github.perracodex.kopapi.inspector.schema.Schema
import io.github.perracodex.kopapi.inspector.schema.SchemaProperty
import io.github.perracodex.kopapi.inspector.schema.TypeSchema
import io.github.perracodex.kopapi.types.ApiFormat
import io.github.perracodex.kopapi.types.ApiType
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class TypedArrayTest {

    private data class Box(
        val label: String,
        val weight: Double
    )

    private data class BoxArray(
        @Suppress("ArrayInDataClass")
        val data: Array<Box>? = null
    )

    @Test
    fun `test object array inspection with Array of Box`() {
        // Define the type to inspect.
        val boxArrayType: KType = BoxArray::class.createType()

        // Inspect the type.
        val schemaProvider = TypeSchemaProvider()
        val typeSchema: TypeSchema = schemaProvider.inspect(kType = boxArrayType)

        // Verify that the typeSchema is a reference to the BoxArray schema.
        assertTrue(
            actual = typeSchema.schema is Schema.Reference,
            message = "Expected schema to be a Schema.Reference"
        )
        assertEquals(
            expected = "${Schema.Reference.PATH}${typeSchema.name}",
            actual = typeSchema.schema.ref,
            message = "Reference value is incorrect"
        )

        // Retrieve the registered schemas.
        val schemasSet: Set<TypeSchema> = schemaProvider.getTypeSchemas()
        assertEquals(
            expected = 2,
            actual = schemasSet.size,
            message = "Expected two schemas (BoxArray and Box)"
        )

        // Find the BoxArray schema.
        val boxArraySchema: TypeSchema = schemasSet.find { it.name == typeSchema.name }
            ?: fail("BoxArray schema not found")
        assertTrue(
            actual = boxArraySchema.schema is Schema.Object,
            message = "BoxArray schema should be a Schema.Object"
        )

        // Validate the 'data' property.
        val dataProperty: SchemaProperty = boxArraySchema.schema.properties["data"]
            ?: fail("Property 'data' not found in BoxArray schema")

        // Check if 'data' property is nullable.
        assertEquals(
            expected = true,
            actual = dataProperty.isNullable,
            message = "Property 'data' should be nullable"
        )

        // Get the schema of the 'data' property.
        assertTrue(
            actual = dataProperty.schema is Schema.Array,
            message = "Data property schema should be a Schema.Array"
        )

        // Validate the array items.
        // The items should be a reference to Box schema.
        assertTrue(
            actual = dataProperty.schema.items is Schema.Reference,
            message = "Items schema should be a Schema.Reference"
        )
        assertEquals(
            expected = "${Schema.Reference.PATH}Box",
            actual = dataProperty.schema.items.ref,
            message = "Items schema reference should be to Box"
        )

        // Validate the Box schema.
        val boxSchema: TypeSchema = schemasSet.find { it.name == "Box" }
            ?: fail("Box schema not found")
        assertTrue(
            actual = boxSchema.schema is Schema.Object,
            message = "Box schema should be a Schema.Object"
        )

        // Validate Box properties.
        assertEquals(
            expected = 2,
            actual = boxSchema.schema.properties.size,
            message = "Box should have two properties"
        )

        // Validate individual properties.
        validateProperty(
            properties = boxSchema.schema.properties,
            propertyName = "label",
            expectedType = ApiType.STRING,
            isRequired = true
        )

        validateProperty(
            properties = boxSchema.schema.properties,
            propertyName = "weight",
            expectedType = ApiType.NUMBER,
            expectedFormat = ApiFormat.DOUBLE,
            isRequired = true
        )
    }

    private fun validateProperty(
        properties: Map<String, SchemaProperty>,
        propertyName: String,
        expectedType: ApiType,
        expectedFormat: ApiFormat? = null,
        isNullable: Boolean = false,
        isRequired: Boolean = true,
        isReference: Boolean = false,
        referenceRef: String? = null
    ) {
        val property: SchemaProperty = properties[propertyName]
            ?: fail("Property '$propertyName' is missing")

        // Check if the property is nullable.
        assertEquals(
            expected = isNullable,
            actual = property.isNullable,
            message = "Property '$propertyName' nullable mismatch"
        )

        // Check if the property is required.
        assertEquals(
            expected = isRequired,
            actual = property.isRequired,
            message = "Property '$propertyName' required mismatch"
        )

        // Check the format if provided.
        expectedFormat?.let {
            if (property.schema is Schema.Primitive) {
                assertEquals(
                    expected = expectedFormat.value,
                    actual = property.schema.format,
                    message = "Property '$propertyName' should have format '${it.value}'"
                )

                // Check the schema type.
                assertEquals(
                    expected = expectedType,
                    actual = property.schema.schemaType,
                    message = "Property '$propertyName' should have type '$expectedType'"
                )
            } else {
                fail("Property '$propertyName' is expected to be a Schema.Primitive with a format")
            }
        }

        // Check if the property is a reference.
        if (isReference) {
            assertTrue(
                actual = property.schema is Schema.Reference,
                message = "Property '$propertyName' should be a Schema.Reference"
            )

            referenceRef?.let {
                assertEquals(
                    expected = property.schema.ref,
                    actual = property.schema.ref,
                    message = "Property '$propertyName' should reference '${property.schema.ref}'"
                )
            }
        }
    }
}
