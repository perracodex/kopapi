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
        assertTrue(actual = typeSchema.schema is ElementSchema.Reference, message = "Expected schema to be a Schema.Reference")
        assertEquals(
            expected = "${ElementSchema.Reference.PATH}${typeSchema.name}",
            actual = typeSchema.schema.ref,
            message = "Reference value is incorrect"
        )

        // Retrieve the registered schemas.
        val schemasSet: Set<TypeSchema> = schemaProvider.getTypeSchemas()
        assertTrue(actual = schemasSet.size >= 3, message = "Expected at least three schemas (User, Address, Status)")

        // Find the User schema.
        val userSchema: TypeSchema = schemasSet.find { it.name == "User" }
            ?: fail("User schema not found")
        assertTrue(actual = userSchema.schema is ElementSchema.ObjectDescriptor, message = "User schema should be an ObjectDescriptor")

        // Validate User properties.
        assertEquals(expected = 6, actual = userSchema.schema.objectProperties.size, message = "User should have six properties")

        // Validate individual properties.
        validateProperty(
            properties = userSchema.schema.objectProperties,
            propertyName = "id",
            expectedType = ApiType.INTEGER,
            expectedFormat = ApiFormat.INT32
        )

        validateProperty(
            properties = userSchema.schema.objectProperties,
            propertyName = "name",
            expectedType = ApiType.STRING,
            isRequired = true
        )

        validateProperty(
            properties = userSchema.schema.objectProperties,
            propertyName = "email",
            expectedType = ApiType.STRING,
            isNullable = true,
            isRequired = true
        )

        validateProperty(
            properties = userSchema.schema.objectProperties,
            propertyName = "status",
            expectedType = ApiType.OBJECT,
            isEnum = true,
            itemsRef = "${ElementSchema.Reference.PATH}Status"
        )

        validateProperty(
            properties = userSchema.schema.objectProperties,
            propertyName = "addresses",
            expectedType = ApiType.ARRAY,
            itemsType = ApiType.OBJECT,
            itemsRef = "${ElementSchema.Reference.PATH}Address"
        )

        validateProperty(
            properties = userSchema.schema.objectProperties,
            propertyName = "attributes",
            expectedType = ApiType.OBJECT,
            hasAdditionalProperties = true
        )

        // Validate the Address schema.
        val addressSchema: TypeSchema = schemasSet.find { it.name == "Address" }
            ?: fail("Address schema not found")
        assertTrue(
            actual = addressSchema.schema is ElementSchema.ObjectDescriptor,
            message = "Address schema should be an ObjectDescriptor"
        )

        val addressProperties: MutableMap<String, SchemaProperty> = addressSchema.schema.objectProperties
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
        assertTrue(actual = statusSchema.schema is ElementSchema.Enum, message = "Status schema should be a Schema.Enum")

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

        // Check if the property is an enum.
        if (isEnum) {
            assertTrue(
                actual = property.schema is ElementSchema.Reference,
                message = "Property '$propertyName' should be a ElementSchema.Reference to an enum"
            )
        }

        // Check enum values if provided.
        enumValues?.let {
            if (property.schema is ElementSchema.Reference) {
                // Assuming the enum schema is already validated elsewhere.
            } else {
                fail("Property '$propertyName' should be a ElementSchema.Reference to an enum")
            }
        }

        // Check items type and reference if the property is an array.
        itemsType?.let {
            if (property.schema is ElementSchema.Array) {
                itemsRef?.let {
                    if (property.schema.items is ElementSchema.Reference) {
                        assertEquals(
                            expected = itemsRef,
                            actual = property.schema.items.ref,
                            message = "Items of '$propertyName' should reference '$itemsRef'"
                        )

                        assertEquals(
                            expected = itemsType,
                            actual = property.schema.items.schemaType,
                            message = "Items of '$propertyName' should have type '$itemsType'"
                        )
                    } else {
                        fail("Items of '$propertyName' should be a ElementSchema.Reference")
                    }
                }
            } else {
                fail("Property '$propertyName' should be a ElementSchema.Array")
            }
        }

        // Check for additionalProperties if it's a map.
        if (hasAdditionalProperties) {
            if (property.schema is ElementSchema.AdditionalProperties) {
                // Additional properties are allowed.
            } else {
                fail("Property '$propertyName' should be a ElementSchema.AdditionalProperties")
            }
        }
    }
}
