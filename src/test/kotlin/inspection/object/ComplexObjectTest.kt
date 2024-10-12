/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package inspection.`object`/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

import io.github.perracodex.kopapi.inspector.TypeSchemaProvider
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

class ComplexObjectTest {

    // Define an enum to be used in the data class.
    private enum class Status {
        ACTIVE,
        INACTIVE,
        PENDING
    }

    // Define a nested data class.
    private data class Address(
        val street: String,
        val city: String,
        val zipCode: String?
    )

    // Define the main data class with collections and nested objects.
    private data class User(
        val id: Int,
        val name: String,
        val email: String?,
        val status: Status,
        val addresses: List<Address>,
        val attributes: Map<String, Any>
    )

    @Test
    fun `test complex schema inspection with nested objects and collections`() {
        // Inspect the type.
        val schemaProvider = TypeSchemaProvider()
        val userType: KType = User::class.createType()
        val typeSchema: TypeSchema = schemaProvider.inspect(kType = userType)

        // Verify that the TypeSchema is a reference to the User schema.
        assertTrue(actual = typeSchema.schema is Schema.Reference, message = "Expected schema to be a Schema.Reference")
        assertEquals(
            expected = "${Schema.Reference.PATH}${typeSchema.name}",
            actual = typeSchema.schema.ref,
            message = "Reference value is incorrect"
        )

        // Retrieve the registered schemas.
        val schemasSet: Set<TypeSchema> = schemaProvider.getTypeSchemas()
        assertTrue(actual = schemasSet.size >= 3, message = "Expected at least three schemas (User, Address, Status)")

        // Find the User schema.
        val userSchema: TypeSchema = schemasSet.find { it.name == "User" }
            ?: fail("User schema not found")
        assertTrue(actual = userSchema.schema is Schema.Object, message = "User schema should be a Schema.Object")

        // Validate User properties.
        assertEquals(expected = 6, actual = userSchema.schema.properties.size, message = "User should have six properties")

        // Validate individual properties.
        validateProperty(
            properties = userSchema.schema.properties,
            propertyName = "id",
            expectedType = ApiType.INTEGER,
            expectedFormat = ApiFormat.INT32
        )

        validateProperty(
            properties = userSchema.schema.properties,
            propertyName = "name",
            expectedType = ApiType.STRING,
            isRequired = true
        )

        validateProperty(
            properties = userSchema.schema.properties,
            propertyName = "email",
            expectedType = ApiType.STRING,
            isNullable = true,
            isRequired = true
        )

        validateProperty(
            properties = userSchema.schema.properties,
            propertyName = "status",
            expectedType = ApiType.OBJECT,
            isEnum = true,
            itemsRef = "${Schema.Reference.PATH}Status"
        )

        validateProperty(
            properties = userSchema.schema.properties,
            propertyName = "addresses",
            expectedType = ApiType.ARRAY,
            itemsType = ApiType.OBJECT,
            itemsRef = "${Schema.Reference.PATH}Address"
        )

        validateProperty(
            properties = userSchema.schema.properties,
            propertyName = "attributes",
            expectedType = ApiType.OBJECT,
            hasAdditionalProperties = true
        )

        // Validate the Address schema.
        val addressSchema: TypeSchema = schemasSet.find { it.name == "Address" }
            ?: fail("Address schema not found")
        assertTrue(actual = addressSchema.schema is Schema.Object, message = "Address schema should be a Schema.Object")

        val addressProperties: MutableMap<String, SchemaProperty> = addressSchema.schema.properties
        assertEquals(expected = 3, actual = addressProperties.size, message = "Address should have three properties")

        // Validate Address properties.
        validateProperty(
            properties = addressProperties,
            propertyName = "street",
            expectedType = ApiType.STRING,
            isRequired = true
        )

        validateProperty(
            properties = addressProperties,
            propertyName = "city",
            expectedType = ApiType.STRING,
            isRequired = true
        )

        validateProperty(
            properties = addressProperties,
            propertyName = "zipCode",
            expectedType = ApiType.STRING,
            isNullable = true,
            isRequired = true
        )

        // Validate the Status enum schema.
        val statusSchema: TypeSchema = schemasSet.find { it.name == "Status" }
            ?: fail("Status schema not found")
        assertTrue(actual = statusSchema.schema is Schema.Enum, message = "Status schema should be a Schema.Enum")

        assertEquals(
            expected = listOf(Status.ACTIVE.name, Status.INACTIVE.name, Status.PENDING.name),
            actual = statusSchema.schema.values,
            message = "Status enum values do not match"
        )
    }

    /**
     * Validates a single property within the properties map.
     */
    private fun validateProperty(
        properties: Map<String, SchemaProperty>,
        propertyName: String,
        expectedType: ApiType,
        expectedFormat: ApiFormat? = null,
        isNullable: Boolean = false,
        isRequired: Boolean = true,
        isEnum: Boolean = false,
        enumValues: List<String>? = null,
        itemsType: ApiType? = null,
        itemsRef: String? = null,
        hasAdditionalProperties: Boolean = false
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
                    message = "Property '$propertyName' should have format '${it.value}'"
                )
            } else {
                fail("Property '$propertyName' is expected to be a Schema.Primitive with a format")
            }
        }

        // Check if the property is an enum.
        if (isEnum) {
            assertTrue(
                actual = property.schema is Schema.Reference,
                message = "Property '$propertyName' should be a Schema.Reference to an enum"
            )
        }

        // Check enum values if provided.
        enumValues?.let {
            if (property.schema is Schema.Reference) {
                // Assuming the enum schema is already validated elsewhere.
            } else {
                fail("Property '$propertyName' should be a Schema.Reference to an enum")
            }
        }

        // Check items type and reference if the property is an array.
        itemsType?.let {
            if (property.schema is Schema.Array) {
                assertEquals(
                    expected = itemsType,
                    actual = property.schema.items.apiType,
                    message = "Items of '$propertyName' should have type '$itemsType'"
                )

                itemsRef?.let {
                    if (property.schema.items is Schema.Reference) {
                        assertEquals(
                            expected = itemsRef,
                            actual = property.schema.items.ref,
                            message = "Items of '$propertyName' should reference '$itemsRef'"
                        )
                    } else {
                        fail("Items of '$propertyName' should be a Schema.Reference")
                    }
                }
            } else {
                fail("Property '$propertyName' should be a Schema.Array")
            }
        }

        // Check for additionalProperties if it's a map.
        if (hasAdditionalProperties) {
            if (property.schema is Schema.AdditionalProperties) {
                // Additional properties are allowed.
            } else {
                fail("Property '$propertyName' should be a Schema.AdditionalProperties")
            }
        }
    }
}
