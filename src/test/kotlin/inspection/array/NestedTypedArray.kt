/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package inspection.array

import io.github.perracodex.kopapi.inspector.TypeInspector
import io.github.perracodex.kopapi.inspector.schema.Schema
import io.github.perracodex.kopapi.inspector.schema.SchemaProperty
import io.github.perracodex.kopapi.inspector.schema.TypeSchema
import io.github.perracodex.kopapi.keys.ApiFormat
import io.github.perracodex.kopapi.keys.ApiType
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class NestedTypedArrayTest {

    private data class Box(
        val label: String,
        val weight: Double
    )

    private data class NestedBoxArray(
        @Suppress("ArrayInDataClass")
        val data: Array<Array<Box>>? = null
    )

    @Test
    fun `test nested array inspection with Array of nested Array of Box`() {
        // Define the type to inspect.
        val nestedBoxArrayType: KType = NestedBoxArray::class.createType()

        // Initialize the TypeInspector.
        val inspector = TypeInspector()
        val typeSchema: TypeSchema = inspector.inspect(kType = nestedBoxArrayType)

        // Verify that the typeSchema is a reference to the NestedBoxArray schema.
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
        val schemasSet: Set<TypeSchema> = inspector.getTypeSchemas()
        assertEquals(
            expected = 2,
            actual = schemasSet.size,
            message = "Expected two schemas (NestedBoxArray and Box)"
        )

        // Find the NestedBoxArray schema.
        val nestedBoxArraySchema: TypeSchema = schemasSet.find { it.name == typeSchema.name }
            ?: fail("NestedBoxArray schema not found")
        assertTrue(
            actual = nestedBoxArraySchema.schema is Schema.Object,
            message = "NestedBoxArray schema should be a Schema.Object"
        )

        // Validate the 'data' property.
        val dataProperty: SchemaProperty = nestedBoxArraySchema.schema.properties["data"]
            ?: fail("Property 'data' not found in NestedBoxArray schema")

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

        // Validate the outer array items.
        val outerArrayItemsSchema: Schema = dataProperty.schema.items
        assertTrue(
            actual = outerArrayItemsSchema is Schema.Array,
            message = "Outer array items schema should be a Schema.Array"
        )

        // Validate the inner array items.
        val innerArrayItemsSchema: Schema = outerArrayItemsSchema.items
        assertTrue(
            actual = innerArrayItemsSchema is Schema.Reference,
            message = "Inner array items schema should be a Schema.Reference"
        )
        assertEquals(
            expected = "${Schema.Reference.PATH}Box",
            actual = innerArrayItemsSchema.ref,
            message = "Inner array items schema reference should be to Box"
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

        // Check the schema type.
        assertEquals(
            expected = expectedType,
            actual = property.schema.apiType,
            message = "Property '$propertyName' should have type '$expectedType'"
        )

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
                    message = "Property '$propertyName' should have format '${expectedFormat.value}'"
                )
            } else {
                fail("Property '$propertyName' is expected to be a Schema.Primitive with a format")
            }
        }

        // Check if the property is a reference.
        if (isReference) {
            if (property.schema is Schema.Reference) {
                referenceRef?.let {
                    assertEquals(
                        expected = property.schema.ref,
                        actual = property.schema.ref,
                        message = "Property '$propertyName' should reference '${property.schema.ref}'"
                    )
                }
            } else {
                fail("Property '$propertyName' should be a Schema.Reference")
            }
        }
    }
}
