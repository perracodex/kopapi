/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package introspection.array

import io.github.perracodex.kopapi.introspection.TypeSchemaProvider
import io.github.perracodex.kopapi.introspection.schema.TypeSchema
import io.github.perracodex.kopapi.schema.facet.ElementSchema
import io.github.perracodex.kopapi.schema.facet.SchemaProperty
import io.github.perracodex.kopapi.type.ApiFormat
import io.github.perracodex.kopapi.type.ApiType
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
    fun `test object array introspection with Array of Box`() {
        // Define the type to introspect.
        val boxArrayType: KType = BoxArray::class.createType()

        // Introspect the type.
        val schemaProvider = TypeSchemaProvider()
        val typeSchema: TypeSchema = schemaProvider.introspect(kType = boxArrayType)

        // Verify that the typeSchema is a reference to the BoxArray schema.
        assertTrue(
            actual = typeSchema.schema is ElementSchema.Reference,
            message = "Expected schema to be a Schema.Reference"
        )
        assertEquals(
            expected = "${ElementSchema.Reference.PATH}${typeSchema.name}",
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
            actual = boxArraySchema.schema is ElementSchema.ObjectDescriptor,
            message = "BoxArray schema should be an ObjectDescriptor"
        )

        // Validate the 'data' property.
        val dataProperty: SchemaProperty = boxArraySchema.schema.objectProperties["data"]
            ?: fail("Property 'data' not found in BoxArray schema")

        // Check if 'data' property is nullable.
        assertEquals(
            expected = true,
            actual = dataProperty.isNullable,
            message = "Property 'data' should be nullable"
        )

        // Get the schema of the 'data' property.
        assertTrue(
            actual = dataProperty.schema is ElementSchema.Array,
            message = "Data property schema should be a Schema.Array"
        )

        // Validate the array items.
        // The items should be a reference to Box schema.
        assertTrue(
            actual = dataProperty.schema.items is ElementSchema.Reference,
            message = "Items schema should be a Schema.Reference"
        )
        assertEquals(
            expected = "${ElementSchema.Reference.PATH}Box",
            actual = dataProperty.schema.items.ref,
            message = "Items schema reference should be to Box"
        )

        // Validate the Box schema.
        val boxSchema: TypeSchema = schemasSet.find { it.name == "Box" }
            ?: fail("Box schema not found")
        assertTrue(
            actual = boxSchema.schema is ElementSchema.ObjectDescriptor,
            message = "Box schema should be an"
        )

        // Validate Box properties.
        assertEquals(
            expected = 2,
            actual = boxSchema.schema.objectProperties.size,
            message = "Box should have two properties"
        )

        // Validate individual properties.
        validateProperty(
            properties = boxSchema.schema.objectProperties,
            propertyName = "label",
            expectedType = ApiType.STRING,
            isRequired = true
        )

        validateProperty(
            properties = boxSchema.schema.objectProperties,
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
            if (property.schema is ElementSchema.Primitive) {
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
                fail("Property '$propertyName' is expected to be a ElementSchema.Primitive with a format")
            }
        }

        // Check if the property is a reference.
        if (isReference) {
            assertTrue(
                actual = property.schema is ElementSchema.Reference,
                message = "Property '$propertyName' should be a ElementSchema.Reference"
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
