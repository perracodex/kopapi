/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.resolver

import io.github.perracodex.kopapi.core.KopapiException
import io.github.perracodex.kopapi.inspector.TypeInspector
import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.spec.Spec
import io.github.perracodex.kopapi.inspector.type.ElementMetadata
import io.github.perracodex.kopapi.inspector.type.TypeSchema
import kotlin.reflect.*

/**
 * Resolves `Generics` types, considering nested and complex `Generics`.
 *
 * #### Type Parameter Map Details
 *
 * `Generics` processing relies on the `typeParameterMap` arguments being passed across traversal.
 * This mapping is crucial for resolving `Generics` types during type inspection.
 * It is a mapping between the `Generics` type parameter (such as `T`, `K`, etc.), and its actual
 * real type argument.
 * This mapping ensures that when the inspector encounters a type argument, it can substitute
 * it with the appropriate actual type.
 * This map is propagated through recursive inspections to maintain consistency in type resolution
 * across the entire type hierarchy.
 *
 * #### Flow
 *
 * **Example Scenario: Inspecting a `Generics` Data Class `Page<Employee>`**
 * ```
 * // Generic data class definition.
 * data class Page<T>(
 *     val content: T,
 *     val pageNumber: Int,
 *     val pageSize: Int
 * )
 *
 * // Concrete type to inspect.
 * data class Employee(
 *     val id: Int,
 *     val name: String
 * )
 *
 * // Data class using the generic `Page` with `Employee` as a type argument.
 * data class Result(
 *     val page: Page<Employee>,
 *     val someOtherProperty: Int,
 *     ...
 * )
 * ```
 *
 * **Flow Explanation:**
 * 1. **Inspecting `Page<Employee>`:**
 *    - **`Generics` Detection:** Identifies `Page` as a `Generics` class with type parameter `T`.
 *    - **Type Argument Mapping:** Creates `typeParameterMap = { T -> Employee }`.
 *    - **Property Traversal:**
 *      - **`content: T`:**
 *        - Substitutes `T` with `Employee` using `typeParameterMap`.
 *        - Delegates to `ObjectResolver` to process `Employee`, generating its schema.
 *      - **`pageNumber: Int` & `pageSize: Int`:**
 *        - Identified as primitive types and mapped directly to OpenAPI `integer` type.
 *    - **Schema Assembly:** Combines the schemas to form `PageOfEmployee`, referencing the `Employee` schema.
 *
 * **Resulting Schema:**
 * ```
 * {
 *   "PageOfEmployee": {
 *     "type": "object",
 *     "properties": {
 *       "content": {
 *         "$ref": "#/components/schemas/Employee"
 *       },
 *       "pageNumber": {
 *         "type": "integer"
 *       },
 *       "pageSize": {
 *         "type": "integer"
 *       }
 *     }
 *   },
 *   "Employee": {
 *     "type": "object",
 *     "properties": {
 *       "id": {
 *         "type": "integer"
 *       },
 *       "name": {
 *         "type": "string"
 *       }
 *     }
 *   }
 * }
 * ```
 *
 * **Example Key Points:**
 *
 * - **`Generics` Class Handling:** `Page<T>` is a `Generics` class with type parameter `T`.
 *   When inspecting `Page<Employee>`, `typeParameterMap` is utilized to substitute `T` with `Employee`.
 * - **Type Replacement:** The `typeParameterMap` enables substituting `T` in the `content` property with `Employee`.
 * - **Delegation to Specific Resolvers:**
 *   - `content: T` (now `content: Employee`) is delegated to `ObjectResolver` to process the concrete type `Employee`.
 *   - Primitive properties like `pageNumber` and `pageSize` are handled directly without needing type substitution.
 * - **Accurate Schema Generation:** Each property's schema is correctly generated based on its type,
 *   ensuring the final `PageOfEmployee` schema accurately reflects its structure.
 */
@TypeInspectorAPI
internal object GenericsResolver {
    /**
     * Handles generics types, considering nested and complex generics.
     *
     * @param kType The KType representing the generics type.
     * @param kClass The KClass representing the generics type.
     * @param typeParameterMap A map of type parameters' [KClassifier] to actual [KType] for replacement.
     * @return The [TypeSchema] for the generics type.
     */
    fun process(
        kType: KType,
        kClass: KClass<*>,
        typeParameterMap: Map<KClassifier, KType>
    ): TypeSchema {
        val genericsTypeName: String = generateTypeName(kType = kType, kClass = kClass)

        // Check if the generics type has already been processed,
        // if not, traverse the generics type to resolve its properties and cache it.
        if (!TypeInspector.isCached(kType = kType)) {
            traverse(
                kType = kType,
                kClass = kClass,
                genericsTypeName = genericsTypeName,
                parentTypeParameterMap = typeParameterMap
            )
        }

        // Return a reference to the generics type schema.
        return TypeSchema.of(
            name = genericsTypeName,
            kType = kType,
            schema = Spec.reference(schema = genericsTypeName)
        )
    }

    /**
     * Generates a unique and consistent name for a generics type,
     * such as Page<Employee> becomes PageOfEmployee.
     *
     * Handles multiple type parameters by joining them with 'Of',
     * for example, Page<Employee, Department> becomes PageOfEmployeeOfDepartment.
     *
     * @param kType The [KType] representing the generics type, used to extract type arguments.
     * @param kClass The [KClass] representing the generics class.
     * @return The generated name for the generics type.
     */
    private fun generateTypeName(kType: KType, kClass: KClass<*>): String {
        val arguments: List<KClass<*>> = kType.arguments.mapNotNull { it.type?.classifier as? KClass<*> }
        val className: String = ElementMetadata.getClassName(kClass = kClass)
        val argumentsNames: List<String> = arguments.map {
            ElementMetadata.getClassName(kClass = it)
        }

        return if (argumentsNames.size == 1) {
            "${className}Of${argumentsNames.first()}"
        } else {
            "${className}Of${argumentsNames.joinToString(separator = "Of")}"
        }
    }

    /**
     * Traverses a generics type to resolve its properties and cache their schema.
     *
     * @param kClass The [kClass] representing the generic type.
     * @param kType The [KType] containing the actual types for the generics.
     * @param genericsTypeName The generated name for the generics type.
     * @param parentTypeParameterMap A map of type parameter classifiers to actual [KType] objects for replacement.
     */
    @Suppress("DuplicatedCode")
    private fun traverse(
        kType: KType,
        kClass: KClass<*>,
        genericsTypeName: String,
        parentTypeParameterMap: Map<KClassifier, KType>
    ) {
        // Add a schema placeholder early to avoid circular references.
        val schemaPlaceholder: TypeSchema = TypeSchema.of(
            name = genericsTypeName,
            kType = kType,
            schema = Spec.properties(value = mutableMapOf())
        )
        TypeInspector.addToCache(schema = schemaPlaceholder)

        // Retrieve the type parameters from the generic class.
        val classTypeParameters: List<KTypeParameter> = kClass.typeParameters
        // Retrieve the actual generics arguments provided.
        val genericsArguments: List<KType> = kType.arguments.mapNotNull { it.type }

        // Ensure the number of type parameters matches the number of the generics arguments.
        if (classTypeParameters.size != genericsArguments.size) {
            throw KopapiException(
                "Generics type parameter count mismatch for $kClass. " +
                        "Expected ${classTypeParameters.size}, but got ${genericsArguments.size}."
            )
        }

        // Create a map of type parameters to their actual types.
        val currentTypeParameterMap: Map<KClassifier, KType> = classTypeParameters
            .mapIndexed { index, typeParameterItem ->
                typeParameterItem as KClassifier to genericsArguments[index]
            }.toMap()

        // Merge parent type parameters with current type parameters.
        val combinedTypeParameterMap: Map<KClassifier, KType> = parentTypeParameterMap + currentTypeParameterMap

        // Prepare a map to hold the properties for the generic instance.
        val propertiesSchemas: MutableMap<String, Any> = mutableMapOf()

        // Traverse each class property and resolve its schema.
        val classProperties: List<KProperty1<out Any, *>> = PropertyResolver.getProperties(kClass = kClass)
        classProperties.forEach { property ->
            val propertySchema: PropertySchema = PropertyResolver.traverse(
                classKType = kType,
                property = property,
                typeParameterMap = combinedTypeParameterMap
            )
            propertiesSchemas[propertySchema.name] = propertySchema.schema
        }

        // Update the placeholder with actual processed schemas.
        schemaPlaceholder.schema.putAll(
            Spec.properties(value = propertiesSchemas)
        )
    }
}