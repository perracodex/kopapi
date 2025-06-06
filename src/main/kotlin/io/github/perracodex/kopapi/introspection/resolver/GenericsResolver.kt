/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.introspection.resolver

import io.github.perracodex.kopapi.introspection.TypeIntrospector
import io.github.perracodex.kopapi.introspection.annotation.TypeIntrospectorApi
import io.github.perracodex.kopapi.introspection.descriptor.ElementName
import io.github.perracodex.kopapi.introspection.descriptor.MetadataDescriptor
import io.github.perracodex.kopapi.introspection.schema.TypeSchema
import io.github.perracodex.kopapi.introspection.schema.factory.SchemaFactory
import io.github.perracodex.kopapi.schema.facet.ElementSchema
import io.github.perracodex.kopapi.schema.facet.SchemaProperty
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.system.Tracer
import io.github.perracodex.kopapi.util.nativeName
import kotlin.reflect.*

/**
 * - Purpose:
 *      - Handles generic types with type parameters.
 * - Action:
 *      - Generate Unique Name:
 *          Creates a unique name for the generic type (e.g., `PageOfEmployee`).
 *      - Type Argument Bindings:
 *          - Create Local Bindings Map: Maps the generic type parameters to their concrete types.
 *          - Merge Argument Bindings: Merges local and inherited bindings for new traversal a context scope.
 *      - Traverse Properties:
 *          - Retrieve Properties: Gets the properties of the generic type.
 *          - Traverse Each Property: Uses `TypeIntrospector` to traverse each property, substituting types as needed.
 *      - Construct Schema: Builds the schema for the generic type.
 *      - Caching: Adds the schema to the `TypeIntrospector` cache.
 *      - Result: Constructs and returns the generic type schema.
 *
 * #### Type Argument Bindings
 * - Purpose:
 *      - Keeps track of substitutions for generic type arguments during traversal.
 * - Behavior:
 *      - Isolation: Each traversal context maintains its own bindings to prevent interference between different contexts.
 *      - Propagation: Passed down during recursive traversal to ensure correct substitutions.
 * - Example:
 *      - If introspecting `Container<T>`, and `T` is for example mapped to `String`,
 *        the map `{ T -> String }` is used to substitute `T` with `String`.
 *
 * `Generics` processing relies on the `typeArgumentBindings` being passed across traversal.
 * The argument bindings are crucial for resolving `Generics` types during type introspection.
 * It's a mapping between a type argument (such as `T`, `K`, etc.), and its actual real type.
 * This mapping ensures that when the resolver encounters a type argument, it can substitute
 * it with the appropriate actual type.
 *
 * #### Type Argument Bindings Scope
 * The `typeArgumentBindings` is scoped per traversal context, ensuring that type substitutions
 * are isolated and consistent within each traversal introspection. This prevents type argument
 * bindings from different context traversals from interfering with one another.
 *
 * #### Flow
 *
 * **Example Scenario: Introspecting a `Generics` Data Class `Page<Employee>`**
 * ```
 * // Generic data class definition.
 * data class Page<T>(
 *     val content: T,
 *     val pageNumber: Int,
 *     val pageSize: Int
 * )
 *
 * // Concrete type to introspect.
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
 * 1. **Introspecting `Page<Employee>`:**
 *    - **`Generics` Detection:** Identifies `Page` as a `Generics` class with type parameter `T`.
 *    - **Type Argument Mapping:** Creates `typeArgumentBindings = { T -> Employee }`.
 *    - **Property Traversal:**
 *      - **`content: T`:**
 *        - Substitutes `T` with `Employee` using `typeArgumentBindings`.
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
 *   When introspecting `Page<Employee>`, `typeArgumentBindings` is utilized to substitute `T` with `Employee`.
 * - **Type Replacement:** The `typeArgumentBindings` enables substituting `T` in the `content` property with `Employee`.
 * - **Delegation to Specific Resolvers:**
 *   - `content: T` (now `content: Employee`) is delegated to `ObjectResolver` to process the concrete type `Employee`.
 *   - Primitive properties like `pageNumber` and `pageSize` are handled directly without needing type substitution.
 * - **Accurate Schema Generation:** Each property's schema is correctly generated based on its type,
 *   ensuring the final `PageOfEmployee` schema accurately reflects its structure.
 *
 * @see [PropertyResolver]
 * @see [TypeIntrospector]
 */
@TypeIntrospectorApi
internal class GenericsResolver(private val introspector: TypeIntrospector) {
    private val tracer: Tracer = Tracer<GenericsResolver>()

    /**
     * Handles generics types, considering nested and complex generics.
     *
     * @param kType The KType representing the generics type.
     * @param kClass The KClass representing the generics type.
     * @param typeArgumentBindings A map of type arguments' [KClassifier] to their actual [KType] replacements.
     * @return The [TypeSchema] for the generics type.
     */
    fun traverse(
        kType: KType,
        kClass: KClass<*>,
        typeArgumentBindings: Map<KClassifier, KType>
    ): TypeSchema {
        tracer.debug("Traversing generics type: $kType.")

        // Every time a traversal is initiated, increase the reference count for the type,
        // as this implies that the type is being used in the schema.
        introspector.increaseReferenceCount(nativeName = kType.nativeName())

        val genericsTypeName: String = generateTypeName(kType = kType, kClass = kClass)

        // Check if the generics type has already been processed,
        // if not, traverse the generics type to resolve its properties and cache it.
        if (!introspector.isCached(kType = kType)) {
            tracer.debug("Generics type not cached, processing: $kType.")
            traverse(
                kType = kType,
                kClass = kClass,
                genericsTypeName = genericsTypeName,
                inheritedTypeArgumentBindings = typeArgumentBindings,
                introspector = introspector
            )
        }

        // Return a reference to the generics type schema.
        return TypeSchema.of(
            name = ElementName(name = genericsTypeName),
            kType = kType,
            schema = SchemaFactory.ofReference(schemaName = genericsTypeName, referencedType = kType)
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
        val className: ElementName = MetadataDescriptor.getClassName(kClass = kClass)
        val argumentsNames: List<ElementName> = arguments.map {
            MetadataDescriptor.getClassName(kClass = it)
        }

        return if (argumentsNames.size == 1) {
            "${className.name}Of${argumentsNames.first().name}"
        } else {
            "${className.name}Of${argumentsNames.joinToString(separator = "Of") { it.name }}"
        }
    }

    /**
     * Traverses a `Generics` type to resolve its properties and cache their schema.
     *
     * #### Type Argument Bindings
     * - `inheritedTypeArgumentBindings` Carries type mappings from the outer traversal context.
     * - `localTypeArgumentBindings` Maps the current `Generics` type parameters to their concrete type arguments.
     * - `mergedTypeArgumentBindings` Combines inherited and local type parameters to maintain context-specific substitutions.
     *
     * #### Type Argument Bindings Lifecycle
     * - `inheritedTypeArgumentBindings` is received from the caller and remains unchanged within this scope.
     * - `localTypeArgumentBindings` is created for each new `Generics` traversal.
     * - `mergedTypeArgumentBindings` is used within the current traversal and passed only downwards to nested traversals.
     *    Does not affect the inherited map in upper context scopes.
     *
     * This ensures type parameters from different `Generics` remain isolated, preventing mix-ups
     * and maintaining accurate type resolution throughout the traversal process.
     *
     * @param kType The [KType] containing the actual type arguments for the `Generics`.
     * @param kClass The [KClass] representing the `Generics` type.
     * @param genericsTypeName The generated name for the `Generics` type schema.
     * @param inheritedTypeArgumentBindings A map of type arguments' [KClassifier] to their actual [KType] replacements.
     * @param introspector The [TypeIntrospector] instance to cache resolved schemas.
     */
    private fun traverse(
        kType: KType,
        kClass: KClass<*>,
        genericsTypeName: String,
        inheritedTypeArgumentBindings: Map<KClassifier, KType>,
        introspector: TypeIntrospector
    ) {
        // Create a schema placeholder and cache it to handle potential circular references.
        val schemaPlaceholder: TypeSchema = TypeSchema.of(
            name = ElementName(name = genericsTypeName),
            kType = kType,
            schema = SchemaFactory.ofObjectDescriptor()
        )
        introspector.addToCache(schema = schemaPlaceholder)

        // Extract `Generic` type parameters and their corresponding type arguments.
        val typeParameters: List<KTypeParameter> = kClass.typeParameters
        val typeArguments: List<KType> = kType.arguments.mapNotNull { it.type }

        // Validate that each type parameter has a corresponding type argument.
        if (typeParameters.size != typeArguments.size) {
            throw KopapiException(
                "Generics type argument count mismatch for $kClass. " +
                        "Expected ${typeParameters.size}, but got ${typeArguments.size}."
            )
        }

        // Map each type parameter to its actual type argument.
        val localTypeArgumentBindings: Map<KClassifier, KType> = typeParameters
            .mapIndexed { index, typeParameter ->
                typeParameter as KClassifier to typeArguments[index]
            }.toMap()

        // Merge inherited type parameters with the local context type argument bindings.
        val mergedTypeArgumentBindings: Map<KClassifier, KType> = inheritedTypeArgumentBindings + localTypeArgumentBindings

        // Get the placeholder schema to place each property.
        val propertiesSchemas: ElementSchema.ObjectDescriptor = schemaPlaceholder.schema as ElementSchema.ObjectDescriptor

        // Retrieve all relevant properties of the generic class.
        val classProperties: List<KProperty1<out Any, *>> = introspector.getClassProperties(kClass = kClass)

        // Traverse each property to resolve its schema using the merged type parameters.
        tracer.debug("Traversing properties of generics type: $kType.")
        classProperties.forEach { property ->
            val (name: String, schemaProperty: SchemaProperty) = introspector.traverseProperty(
                classKType = kType,
                property = property,
                typeArgumentBindings = mergedTypeArgumentBindings
            )

            propertiesSchemas.objectProperties[name] = schemaProperty
        }
    }
}
