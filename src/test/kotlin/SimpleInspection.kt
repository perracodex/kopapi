/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

import io.github.perracodex.kopapi.inspector.TypeInspector
import io.github.perracodex.kopapi.inspector.spec.SpecFormat
import io.github.perracodex.kopapi.inspector.spec.SpecKey
import io.github.perracodex.kopapi.inspector.spec.SpecType
import io.github.perracodex.kopapi.inspector.type.TypeSchema
import kotlin.reflect.full.createType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SimpleInspection {

    @Test
    fun `test a simple schema inspection by verifying the schema structure`() {
        // Define the test  Data Class
        data class Box(
            val color: String,
            val weight: Int,
            val isFragile: Boolean
        )

        // Inspect the Type Schema
        val schemeRef: TypeSchema = TypeInspector.inspect(kType = Box::class.createType())

        // Basic definition validation,
        assertEquals(Box::class.java.simpleName, schemeRef.name, "Schema name should match the class name")
        assertEquals(Box::class.java.name, schemeRef.type, "Schema type should match the fully qualified class name")
        assertEquals(1, schemeRef.schema.size, "Schema should contain exactly one entry")

        // Validate the object reference entry,
        val (key, value) = schemeRef.schema.entries.first()
        assertEquals(SpecKey.REFERENCE(), key, "The first key should be SpecKey.REFERENCE()")
        assertEquals("${SpecKey.REFERENCE_PREFIX}${schemeRef.name}", value, "The reference value is incorrect")

        // Retrieve and assert registered schemas,
        val schemesSet: Set<TypeSchema> = TypeInspector.getTypeSchemas()
        assertEquals(1, schemesSet.size, "There should be exactly one registered schema")

        // Verify the definition of the retrieved schema,
        val scheme: TypeSchema = schemesSet.first()
        assertEquals(Box::class.java.simpleName, scheme.name, "Retrieved schema name should match")
        assertEquals(Box::class.java.name, scheme.type, "Retrieved schema type should match")
        assertEquals(2, scheme.schema.size, "Retrieved schema should have exactly two entries")
        assertEquals(SpecType.OBJECT(), scheme.schema[SpecKey.TYPE()], "Schema type should be OBJECT")

        // Validate schema properties,
        val properties = extractProperties(scheme)
        assertEquals(3, properties.size, "Properties should contain exactly three entries")

        // Assert each of the properties,
        validateProperty(properties, "color", SpecType.STRING())
        validateProperty(properties, "weight", SpecType.INTEGER(), SpecFormat.INT32())
        validateProperty(properties, "isFragile", SpecType.BOOLEAN())
    }

    /**
     * Extracts and casts the properties map from the schema.
     * Throws an AssertionError if the structure is not as expected.
     */
    private fun extractProperties(scheme: TypeSchema): MutableMap<String, MutableMap<String, String>> {
        @Suppress("UNCHECKED_CAST")
        val properties = scheme.schema[
            SpecKey.PROPERTIES()
        ] as? MutableMap<String, MutableMap<String, String>>
            ?: fail("Properties map is missing or has an incorrect type")
        return properties
    }

    /**
     * Validates a single property within the properties map.
     *
     * @param properties The complete properties map.
     * @param propertyName The name of the property to validate.
     * @param expectedType The expected type of the property.
     * @param expectedFormat (Optional) The expected format of the property.
     */
    private fun validateProperty(
        properties: MutableMap<String, MutableMap<String, String>>,
        propertyName: String,
        expectedType: String,
        expectedFormat: String? = null
    ) {
        val property: MutableMap<String, String> = properties[propertyName]
            ?: fail("Property '$propertyName' is missing")

        assertEquals(
            expected = expectedType,
            actual = property[SpecKey.TYPE()],
            message = "Property '$propertyName' should have type '$expectedType'"
        )

        expectedFormat?.let {
            assertEquals(
                expected = expectedFormat,
                actual = property[SpecKey.FORMAT()],
                message = "Property '$propertyName' should have format '$expectedFormat'"
            )
        }
    }
}