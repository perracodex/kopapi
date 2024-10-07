/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.resolver

import io.github.perracodex.kopapi.inspector.TypeResolver
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
 * This mapping ensures that when the resolver encounters a type argument, it can substitute
 * it with the appropriate actual type.
 * This map is propagated through recursive inspections to maintain consistency in type resolution
 * across the entire type hierarchy.
 *
 * #### Type Parameter Map Scope
 * The typeParameterMap` is scoped per traversal context, ensuring that type substitutions
 * are isolated and consistent within each generic inspection. This prevents type parameter
 * mappings from different generics from interfering with one another.
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
 *
 * @see [PropertyResolver]
 * @see [TypeResolver]
 */
@TypeInspectorAPI
internal class GenericsResolver(private val typeResolver: TypeResolver) {
    /**
     * Handles generics types, considering nested and complex generics.
     *
     * @param kType The KType representing the generics type.
     * @param kClass The KClass representing the generics type.
     * @param typeParameterMap A map of type parameters' [KClassifier] to actual [KType] for replacement.
     * @return The [TypeSchema] for the generics type.
     */
    fun traverse(
        kType: KType,
        kClass: KClass<*>,
        typeParameterMap: Map<KClassifier, KType>
    ): TypeSchema {
        val genericsTypeName: String = generateTypeName(kType = kType, kClass = kClass)

        // Check if the generics type has already been processed,
        // if not, traverse the generics type to resolve its properties and cache it.
        if (!typeResolver.isCached(kType = kType)) {
            traverse(
                kType = kType,
                kClass = kClass,
                genericsTypeName = genericsTypeName,
                inheritedTypeParameterMap = typeParameterMap,
                typeResolver = typeResolver
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
     * such as for example `Page<Employee>` becomes `PageOfEmployee`.
     *
     * Handles multiple type parameters by joining them with 'Of',
     * for example, `Page<Employee, Department>` becomes `PageOfEmployeeOfDepartment`.
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
     * Traverses a `Generics` type to resolve its properties and cache their schema.
     *
     * #### Type Parameter Map Details
     * - `inheritedTypeParameterMap` Carries type mappings from the outer traversal context.
     * - `localTypeParameterMap` Maps the current `Generics` type parameters to their concrete type arguments.
     * - `mergedTypeParameterMap` Combines inherited and local type parameters to maintain context-specific substitutions.
     *
     * #### Type Parameter Map Lifecycle
     * - `inheritedTypeParameterMap` is received from the caller and remains unchanged within this scope.
     * - `localTypeParameterMap` is created for each new `Generics` traversal.
     * - `mergedTypeParameterMap` is used within the current traversal and passed only downwards to nested traversals.
     *    Does not affect the inherited map in upper context scopes.
     *
     * This ensures type parameters from different `Generics` remain isolated, preventing mix-ups
     * and maintaining accurate type resolution throughout the traversal process.
     *
     * @param kType The [KType] containing the actual type arguments for the `Generics`.
     * @param kClass The [KClass] representing the `Generics` type.
     * @param genericsTypeName The generated name for the `Generics` type schema.
     * @param inheritedTypeParameterMap A map of type parameter classifiers to their concrete [KType]s from the outer context.
     * @param typeResolver The [TypeResolver] instance to cache resolved schemas.
     */
    @Suppress("DuplicatedCode")
    private fun traverse(
        kType: KType,
        kClass: KClass<*>,
        genericsTypeName: String,
        inheritedTypeParameterMap: Map<KClassifier, KType>,
        typeResolver: TypeResolver
    ) {
        // Create a schema placeholder and cache it to handle potential circular references.
        val schemaPlaceholder: TypeSchema = TypeSchema.of(
            name = genericsTypeName,
            kType = kType,
            schema = Spec.properties(value = mutableMapOf())
        )
        typeResolver.addToCache(schema = schemaPlaceholder)

        // Extract `Generic` type parameters and their corresponding type arguments.
        val typeParameters: List<KTypeParameter> = kClass.typeParameters
        val typeArguments: List<KType> = kType.arguments.mapNotNull { it.type }

        // Validate that each type parameter has a corresponding type argument.
        require(typeParameters.size == typeArguments.size) {
            "Generics type parameter count mismatch for $kClass. " +
                    "Expected ${typeParameters.size}, but got ${typeArguments.size}."
        }

        // Map each type parameter to its actual type argument.
        val localTypeParameterMap: Map<KClassifier, KType> = typeParameters
            .mapIndexed { index, typeParameter ->
                typeParameter as KClassifier to typeArguments[index]
            }.toMap()

        // Merge inherited type parameters with the local context type parameter mappings.
        val mergedTypeParameterMap: Map<KClassifier, KType> = inheritedTypeParameterMap + localTypeParameterMap

        // Initialize a map to hold resolved schemas for each property.
        val propertiesSchemas: MutableMap<String, Any> = mutableMapOf()

        // Retrieve all relevant properties of the generic class.
        val classProperties: List<KProperty1<out Any, *>> = typeResolver.getClassProperties(kClass = kClass)

        // Traverse each property to resolve its schema using the merged type parameters.
        classProperties.forEach { property ->
            val propertySchema: PropertySchema = typeResolver.traverseProperty(
                classKType = kType,
                property = property,
                typeParameterMap = mergedTypeParameterMap
            )
            propertiesSchemas[propertySchema.name] = propertySchema.schema
        }

        // Update the cached schema placeholder with the resolved property schemas.
        schemaPlaceholder.schema.putAll(
            Spec.properties(value = propertiesSchemas)
        )
    }
}
