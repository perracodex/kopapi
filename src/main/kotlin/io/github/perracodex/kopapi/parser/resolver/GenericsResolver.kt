/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.parser.resolver

import io.github.perracodex.kopapi.parser.ObjectTypeParser
import io.github.perracodex.kopapi.parser.annotation.ObjectTypeParserAPI
import io.github.perracodex.kopapi.parser.definition.ElementMetadata
import io.github.perracodex.kopapi.parser.definition.TypeDefinition
import io.github.perracodex.kopapi.parser.spec.Spec
import kotlin.reflect.*

/**
 * Resolves generics types, considering nested and complex generics.
 *
 * Responsibilities:
 * - Verify if the generics type has already been processed, in which case return a reference to it.
 * - Traversing the generics type to resolve its properties and cache their definitions.
 * - Generating a unique and consistent name for the generics type.
 * - Caching the created [TypeDefinition] to avoid redundant processing.
 */
@ObjectTypeParserAPI
internal object GenericsResolver {
    /**
     * Handles generics types, considering nested and complex generics.
     *
     * @param kType The KType representing the generics type.
     * @param kClass The KClass representing the generics type.
     * @param typeParameterMap A map of type parameters' [KClassifier] to actual [KType] for replacement.
     * @return The [TypeDefinition] for the generics type.
     */
    fun process(
        kType: KType,
        kClass: KClass<*>,
        typeParameterMap: Map<KClassifier, KType>
    ): TypeDefinition {
        val genericsTypeName: String = generateTypeName(kType = kType, kClass = kClass)

        // Check if the generics type has already been processed,
        // if not, traverse the generics type to resolve its properties and cache it.
        if (!ObjectTypeParser.isCached(kType = kType)) {
            traverse(
                kType = kType,
                kClass = kClass,
                genericsTypeName = genericsTypeName,
                parentTypeParameterMap = typeParameterMap
            )
        }

        // Return a definition reference to the generics type.
        return TypeDefinition.of(
            name = genericsTypeName,
            kType = kType,
            definition = Spec.reference(schema = genericsTypeName)
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
     * Traverses a generics type to resolve its properties and cache the definition.
     *
     * @param kClass The [kClass] representing the generic type.
     * @param kType The [KType] containing the actual types for the generics.
     * @param genericsTypeName The generated name for the generics type.
     * @param parentTypeParameterMap A map of type parameter classifiers to actual [KType] objects for replacement.
     */
    private fun traverse(
        kType: KType,
        kClass: KClass<*>,
        genericsTypeName: String,
        parentTypeParameterMap: Map<KClassifier, KType>
    ) {
        // Add a placeholder definition early to avoid circular references.
        val placeholder: TypeDefinition = TypeDefinition.of(
            name = genericsTypeName,
            kType = kType,
            definition = Spec.properties(value = mutableMapOf())
        )
        ObjectTypeParser.addToCache(definition = placeholder)

        // Retrieve the type parameters from the generic class.
        val classTypeParameters: List<KTypeParameter> = kClass.typeParameters
        // Retrieve the actual generics arguments provided.
        val genericsArguments: List<KType> = kType.arguments.mapNotNull { it.type }

        // Ensure the number of type parameters matches the number of the generics arguments.
        if (classTypeParameters.size != genericsArguments.size) {
            throw IllegalArgumentException(
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
        val properties: MutableMap<String, Any> = mutableMapOf()

        // Retrieve sorted properties based on the primary constructor's parameter order
        val sortedProperties: List<KProperty1<out Any, *>> = PropertyResolver.getProperties(kClass = kClass)

        // Iterate over each sorted property in the generic class.
        sortedProperties.forEach { sortedProperty ->
            val (propertyName, extendedDefinition) = PropertyResolver.traverse(
                property = sortedProperty,
                typeParameterMap = combinedTypeParameterMap
            )
            properties[propertyName] = extendedDefinition
        }

        // Update the placeholder definition  with actual properties.
        placeholder.definition.putAll(Spec.properties(value = properties))
    }
}