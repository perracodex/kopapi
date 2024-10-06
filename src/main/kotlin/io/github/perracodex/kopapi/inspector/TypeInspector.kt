/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector

import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.resolver.*
import io.github.perracodex.kopapi.inspector.spec.Spec
import io.github.perracodex.kopapi.inspector.type.TypeDescriptor
import io.github.perracodex.kopapi.inspector.type.TypeSchema
import io.github.perracodex.kopapi.inspector.type.TypeSchemaConflicts
import io.github.perracodex.kopapi.inspector.type.nativeName
import io.github.perracodex.kopapi.utils.Tracer
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf

/**
 * Class for introspecting various Kotlin types capable of traversing and mapping [KType] objects
 * into [TypeSchema] objects containing the necessary information to construct OpenAPI schemas.
 *
 * #### Key Features
 * - Inspection recursion for complex types.
 * - All primitive types, including enum classes.
 * - Major common types like `UUID`, `Instant`, `LocalDate`, etc. from both Kotlin and Java.
 * - Complex types like data classes, including nested complex Object properties.
 * - `Collections` like `Lists`, `Sets`, and `Arrays`, including primitive arrays (eg: `IntArray`).
 * - `Maps` with both complex objects or primitives.
 * - Generics support, including complex nested types.
 *
 * #### Supported Annotations
 * - Kotlinx:
 *      - `@SerialName`, `@Transient`, `@Required`
 * - Jackson:
 *     - `JsonTypeName`, `@JsonProperty`, `@JsonIgnore`
 *
 * #### Caching
 *  The inspector caches resolved schemas to avoid duplication, so types are uniquely processed
 *  regardless of how many times are found in the current processing context, or subsequent calls.
 *  If different types are found with the same name, but different package they will still be
 *  processed and cached separately, but a warning will be added to the [TypeSchemaConflicts]
 *  class allowing to inform about potential conflicts.
 */
internal object TypeInspector {
    private val tracer = Tracer<TypeInspector>()

    /** Cache of [TypeSchema] objects that have been processed. */
    private val typeSchemaCache: MutableSet<TypeSchema> = mutableSetOf()

    /**
     * Retrieves the currently cached [TypeSchema] objects.
     *
     * @return A set of [TypeSchema] objects.
     */
    fun getTypeSchemas(): Set<TypeSchema> = typeSchemaCache

    /**
     * Inspect the given [kType] to its corresponding [TypeSchema] representation.
     *
     * @param kType The target [KType] to inspect.
     * @return The resolved [TypeSchema] for the given [kType].
     */
    @OptIn(TypeInspectorAPI::class)
    fun inspect(kType: KType): TypeSchema {
        val result: TypeSchema = traverse(kType = kType, typeParameterMap = emptyMap())
        TypeSchemaConflicts.analyze(newSchema = result)
        return result
    }

    /**
     * Resets the inspector by clearing all processed type schemas, including conflicts.
     */
    @OptIn(TypeInspectorAPI::class)
    fun reset() {
        typeSchemaCache.clear()
        TypeSchemaConflicts.clear()
    }

    /**
     * Traverses and resolves the given [kType], handling both simple and complex types,
     * including collections, maps, enums, and `Generics`. Manages recursion and self-referencing types.
     *
     * Returns a [TypeSchema] representing the structure of the [kType], considering `Generics`,
     * optional properties, nullable properties, and some concrete annotations (see [TypeDescriptor]).
     *
     * #### Type Parameter Map Details
     *
     * The [typeParameterMap] is a crucial component for resolving `Generics` types during traversal
     * and type inspections. This map is propagated through recursive inspections to maintain
     * consistency in type resolution across the entire type hierarchy.
     * See [GenericsResolver] for more detailed information on how this map is used.
     *
     * @param kType The [KType] to resolve into a [TypeSchema].
     * @param typeParameterMap A map of type parameters' [KClassifier] to their actual [KType] replacements.
     * @return The resolved [TypeSchema] for the [kType].
     */
    @TypeInspectorAPI
    fun traverse(
        kType: KType,
        typeParameterMap: Map<KClassifier, KType>
    ): TypeSchema {
        // Resolve the type's classifier, if unavailable, log an error and return an unknown type.
        val classifier: KClassifier = kType.classifier
            ?: run {
                tracer.error("Missing classifier. kType=$kType. typeParameterMap=$typeParameterMap.")
                return buildUnknownTypeSchema(kType = kType)
            }

        val typeSchema: TypeSchema = when {
            // Handle primitive arrays (e.g.: IntArray), and generics arrays (e.g., Array<String>).
            TypeDescriptor.isArray(kType = kType) ->
                ArrayResolver.process(
                    kType = kType,
                    classifier = classifier,
                    typeParameterMap = typeParameterMap
                )

            // Handle collections (e.g., List<String>, Set<Int>, etc.).
            TypeDescriptor.isCollection(classifier = classifier) ->
                CollectionResolver.process(
                    kType = kType,
                    classifier = classifier,
                    typeParameterMap = typeParameterMap
                )

            // Handle maps (e.g., Map<String, Int>).
            classifier == Map::class ->
                MapResolver.process(kType = kType, typeParameterMap = typeParameterMap)

            // Handle enums.
            classifier is KClass<*> && classifier.isSubclassOf(Enum::class) ->
                EnumResolver.process(enumClass = classifier)

            // Handle generics. Must be checked after arrays, collections, and maps
            // because they also have type arguments.
            kType.arguments.isNotEmpty() ->
                GenericsResolver.process(
                    kType = kType,
                    kClass = classifier as KClass<*>,
                    typeParameterMap = typeParameterMap
                )

            // Handle basic types and complex objects.
            // This condition must be placed last because all types are also instances of KClass.
            // If this check is placed earlier, the above branches will never be reached.
            classifier is KClass<*> ->
                ObjectResolver.process(
                    kType = kType,
                    kClass = classifier,
                    typeParameterMap = typeParameterMap
                )

            // Fallback for unknown types. This should never be reached.
            else -> {
                tracer.error("Unexpected type: $kType. typeParameterMap=$typeParameterMap.")
                buildUnknownTypeSchema(kType = kType)
            }
        }

        return typeSchema
    }

    /**
     * Constructs a [TypeSchema] for the given [kType] when the type is unknown.
     *
     * @param kType The unknown [KType] to build a schema for.
     *
     */
    @OptIn(TypeInspectorAPI::class)
    private fun buildUnknownTypeSchema(
        kType: KType,
    ): TypeSchema {
        return TypeSchema.of(
            name = "Unknown_$kType",
            kType = kType,
            schema = Spec.objectType()
        )
    }

    /**
     * Determines whether the given [KType] is already present
     * in the [TypeSchema] cache.
     */
    @TypeInspectorAPI
    fun isCached(kType: KType): Boolean {
        return typeSchemaCache.any {
            it.type == kType.nativeName()
        }
    }

    /**
     * Caches the given [TypeSchema] object.
     */
    @TypeInspectorAPI
    fun addToCache(schema: TypeSchema) {
        typeSchemaCache.add(schema)
    }

    /**
     * Returns the corresponding type from [typeParameterMap] if the [kType]'s classifier
     * is found in the given [typeParameterMap].
     * Otherwise, returns the provided [kType].
     *
     * #### Type Substitution
     * This function checks if the classifier (typically a type argument like `T`, `K`, etc.)
     * of the given [kType] exists in the [typeParameterMap].
     * If it does, it replaces the type with its mapped concrete type.
     *
     * #### Ensuring Accurate Type Resolution
     * By substituting type parameters with actual types, it ensures that the introspecting
     * traversal can generate precise [TypeSchema] representations, especially for `Generics` types.
     *
     * #### Example Scenario
     * Consider a `Generics` class `Data<T>`. When inspecting `Data<Car>`,
     * `T` should be replaced with `Car` in the type resolution process.
     *
     * @param kType The [kType] to check for replacement.
     * @param typeParameterMap A map where type parameters are mapped to their actual [kType] values.
     * @return The [kType] from the map if the classifier is found, otherwise the provided [kType].
     */
    @TypeInspectorAPI
    fun replaceTypeIfNeeded(
        kType: KType,
        typeParameterMap: Map<KClassifier, KType>
    ): KType {
        val classifier: KClassifier? = kType.classifier
        return if (classifier in typeParameterMap) {
            typeParameterMap[classifier]!!
        } else {
            kType
        }
    }
}
