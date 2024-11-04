/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package inspection.objects

import io.github.perracodex.kopapi.inspector.TypeSchemaProvider
import io.github.perracodex.kopapi.inspector.schema.TypeSchema
import io.github.perracodex.kopapi.schema.facets.ElementSchema
import io.github.perracodex.kopapi.schema.facets.SchemaProperty
import io.github.perracodex.kopapi.types.ApiFormat
import io.github.perracodex.kopapi.types.ApiType
import kotlin.reflect.full.createType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class SimpleObjectTest {

    // Define the test Data Class.
    private data class Box(
        val color: String,
        val weight: Int,
        val isFragile: Boolean
    )

    @Test
    fun `test a simple schema inspection by verifying the schema structure`() {

        // Inspect the type.
        val schemaProvider = TypeSchemaProvider()
        val schemeRef: TypeSchema = schemaProvider.inspect(kType = Box::class.createType())

        // Basic definition validation.
        assertEquals(expected = Box::class.simpleName, actual = schemeRef.name, message = "Schema name should match the class name")
        assertEquals(expected = Box::class.java.name, actual = schemeRef.type, message = "Schema type should match the type string")

        // Validate that schemeRef.schema is a ElementSchema.Reference
        assertTrue(actual = schemeRef.schema is ElementSchema.Reference, message = "Expected schema to be a Schema.Reference")
        assertEquals(
            expected = "${ElementSchema.Reference.PATH}${schemeRef.name}",
            actual = schemeRef.schema.ref,
            message = "Reference value is incorrect"
        )

        // Retrieve and assert registered schemas.
        val schemasSet: Set<TypeSchema> = schemaProvider.getTypeSchemas()
        assertEquals(expected = 1, actual = schemasSet.size, message = "There should be exactly one registered schema")

        // Get the actual schema for Box.
        val schema: TypeSchema = schemasSet.first()
        assertEquals(expected = Box::class.simpleName, actual = schema.name, message = "Retrieved schema name should match")
        assertEquals(expected = Box::class.java.name, actual = schema.type, message = "Retrieved schema type should match")

        // Assert that schema.schema is a ElementSchema.ObjectDescriptor
        assertTrue(actual = schema.schema is ElementSchema.ObjectDescriptor, message = "Expected schema to be an ObjectDescriptor")

        // Validate schema properties.
        val properties: MutableMap<String, SchemaProperty> = schema.schema.objectProperties
        assertEquals(expected = 3, actual = properties.size, message = "Properties should contain exactly three entries")

        // Assert each of the properties.
        validateProperty(properties = properties, propertyName = "color", expectedType = ApiType.STRING)
        validateProperty(properties = properties, propertyName = "weight", expectedType = ApiType.INTEGER, ApiFormat.INT32)
        validateProperty(properties = properties, propertyName = "isFragile", expectedType = ApiType.BOOLEAN)
    }

    /**
     * Validates a single property within the properties map.
     *
     * @param properties The complete properties map.
     * @param propertyName The name of the property to validate.
     * @param expectedType The expected [ApiType] of the property.
     * @param expectedFormat (Optional) The expected [ApiFormat] of the property.
     */
    private fun validateProperty(
        properties: Map<String, SchemaProperty>,
        propertyName: String,
        expectedType: ApiType,
        expectedFormat: ApiFormat? = null
    ) {
        val property: SchemaProperty = properties[propertyName]
            ?: fail("Property '$propertyName' is missing")

        val schema: ElementSchema = property.schema

        // If expectedFormat is provided, check it.
        expectedFormat?.let {
            if (schema is ElementSchema.Primitive) {
                assertEquals(
                    expected = expectedFormat.value,
                    actual = schema.format,
                    message = "Property '$propertyName' should have format '${expectedFormat.value}'"
                )

                // Check the schema type.
                assertEquals(
                    expected = expectedType,
                    actual = schema.schemaType,
                    message = "Property '$propertyName' should have type '$expectedType'"
                )
            } else {
                fail("Property '$propertyName' is expected to be a ElementSchema.Primitive with a format")
            }
        }
    }
}