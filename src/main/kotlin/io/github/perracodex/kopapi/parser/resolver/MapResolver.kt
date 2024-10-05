/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.parser.resolver

import io.github.perracodex.kopapi.parser.ObjectTypeParser
import io.github.perracodex.kopapi.parser.annotation.ObjectTypeParserAPI
import io.github.perracodex.kopapi.parser.definition.TypeDefinition
import io.github.perracodex.kopapi.parser.spec.Spec
import kotlin.reflect.KClassifier
import kotlin.reflect.KType

/**
 * Resolves [Map] objects into their corresponding [TypeDefinition].
 *
 * Maps do not generate their own schema definition references,
 * but a definition reference will be created for their value type if such is a complex object.
 *
 * Responsibilities:
 * - Handling [Map] objects by resolving their key and value types.
 * - Traverse the value type if it is a complex object.
 * - Ensuring that the map's key type is [String].
 * - Creating a [TypeDefinition] which includes an `additionalProperties` spec for the value type.
 * - Caching the created [TypeDefinition] to avoid redundant processing.
 */
@ObjectTypeParserAPI
internal object MapResolver {
    /**
     * Resolves a [Map] type to a [TypeDefinition].
     *
     * @param kType The [KType] representing the map type.
     * @param typeParameterMap A map of type parameters' [KClassifier] to actual [KType] items for replacement.
     * @return The resolved [TypeDefinition] for the map, with additionalProperties for the value type.
     * @throws IllegalArgumentException if the map's key type is not [String].
     */
    fun process(
        kType: KType,
        typeParameterMap: Map<KClassifier, KType>
    ): TypeDefinition {
        val keyType: KType? = kType.arguments.getOrNull(index = 0)?.type?.let {
            ObjectTypeParser.replaceTypeIfNeeded(type = it, typeParameterMap = typeParameterMap)
        }
        val valueType: KType? = kType.arguments.getOrNull(index = 1)?.type?.let {
            ObjectTypeParser.replaceTypeIfNeeded(type = it, typeParameterMap = typeParameterMap)
        }

        // OpenAPI requires keys to be strings.
        if (keyType == null || keyType.classifier != String::class) {
            throw IllegalArgumentException("Maps must have strings as keys. Found key type: $keyType")
        }

        // Process the value type.
        val typeDefinition: TypeDefinition = valueType?.let {
            ObjectTypeParser.traverseType(kType = it, typeParameterMap = typeParameterMap)
        } ?: TypeDefinition.of(name = "MapOf${kType}", kType = kType, definition = Spec.objectType())

        return TypeDefinition.of(
            name = "MapOf${typeDefinition.name}",
            kType = kType,
            definition = Spec.additionalProperties(value = typeDefinition.definition)
        )
    }
}