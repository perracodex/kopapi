/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.introspection

import io.github.perracodex.kopapi.introspection.annotation.TypeIntrospectorApi
import io.github.perracodex.kopapi.introspection.descriptor.ElementName
import io.github.perracodex.kopapi.introspection.descriptor.TypeDescriptor
import io.github.perracodex.kopapi.introspection.resolver.*
import io.github.perracodex.kopapi.introspection.schema.TypeSchema
import io.github.perracodex.kopapi.introspection.schema.factory.PrimitiveFactory
import io.github.perracodex.kopapi.introspection.schema.factory.SchemaFactory
import io.github.perracodex.kopapi.schema.facet.ElementSchema
import io.github.perracodex.kopapi.schema.facet.SchemaProperty
import io.github.perracodex.kopapi.system.Tracer
import io.github.perracodex.kopapi.util.nativeName
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf

/**
 * Class for introspecting various Kotlin types, capable of traversing and mapping [KType] objects
 * into [TypeSchema] objects containing the necessary information to construct OpenAPI schemas.
 *
 * #### Key Features
 * - Recursive Introspection: Capable of recursively traversing complex types, including nested objects,
 *   self-referencing and cross-referencing types.
 * - Comprehensive Type Support: Handles all primitive types, enums, and common Kotlin and Java types.
 * - Collections and Maps: Supports collections such as `List`, `Set`, including primitive and typed arrays,
 *   and maps with both primitive and complex object values.
 * - Generics Handling: Resolves generics, including nested and complex generic types.
 * - Annotation Support: Recognizes `Kotlinx` annotations, with partial support for `Jackson`.
 * - Caching Mechanism: Caches resolved schemas to prevent duplication and improve performance.
 * - Ensures unique processing of types, even if they appear multiple times across different contexts.
 *
 * #### Supported Annotations
 * - Kotlinx:
 *      - `@SerialName`, `@Transient`, `@Required`
 * - Jackson (partial support):
 *     - `JsonTypeName`, `@JsonProperty`, `@JsonIgnore`
 */
@TypeIntrospectorApi
internal class TypeIntrospector {
    private val tracer: Tracer = Tracer<TypeIntrospector>()

    /** Cache of [TypeSchema] objects that have been processed. */
    private val typeSchemaCache: MutableSet<TypeSchema> = mutableSetOf()

    /**
     * Keeps track of the number of times a type is referenced.
     *
     * Note that this is not an accurate usage value, as the introspector cannot
     * track if a new introspection trigger is due to just a repeated call.
     *
     * The value represents the first time it is introspected, and the subsequent
     * matches within the same introspection due to circular references,
     * or when a new type is introspected which references the same type.
     */
    private val typeSchemaUsages: MutableMap<String, Int> = mutableMapOf()

    private val arrayResolver = ArrayResolver(introspector = this)
    private val collectionResolver = CollectionResolver(introspector = this)
    private val enumResolver = EnumResolver(introspector = this)
    private val genericsResolver = GenericsResolver(introspector = this)
    private val mapResolver = MapResolver(introspector = this)
    private val objectResolver = ObjectResolver(introspector = this)
    private val propertyResolver = PropertyResolver(introspector = this)

    /**
     * Retrieves the cached [TypeSchema] objects.
     */
    fun getTypeSchemas(): Set<TypeSchema> = typeSchemaCache

    /**
     * Retrieves the cached [TypeSchema] reference for the given [kType],
     * if such has been previously introspected and cached.
     *
     * @param kType The [KType] to retrieve the reference for.
     * @return The [TypeSchema] reference for the given [kType], if available; otherwise, `null`.
     */
    fun getTypeSchemaReference(kType: KType): TypeSchema? {
        val cachedSchema: TypeSchema = typeSchemaCache.find { typeSchema ->
            typeSchema.type == kType.nativeName()
        } ?: return null

        val className = ElementName(name = cachedSchema.name, renamedFrom = cachedSchema.renamedFrom)
        return TypeSchema.of(
            name = className,
            kType = kType,
            schema = SchemaFactory.ofReference(schemaName = className.name, referencedType = kType)
        )
    }

    /**
     * Traverses and resolves the given [kType], handling both simple and complex types,
     * including collections, maps, enums, and `Generics`. Manages recursion and self-referencing types.
     *
     * Returns a [TypeSchema] representing the structure of the [kType], considering `Generics`,
     * optional properties, nullable properties, and some concrete annotations (see [PrimitiveFactory]).
     *
     * #### Type Argument Bindings
     *
     * The [typeArgumentBindings] is a crucial component for resolving `Generics` types during traversal
     * and type introspections. This map is propagated through recursive introspections to maintain
     * consistency in type resolution across the entire type hierarchy.
     * See [GenericsResolver] for more detailed information on how this map is used.
     *
     * #### Decision Tree
     * ```
     * traverseType:
     *     |
     *     +-> Is the type an Array?
     *     |    |
     *     |    +-> Yes:
     *     |        - Is it a Primitive Array?
     *     |        |   |
     *     |        |   +-> Yes:
     *     |        |   |       - Map the primitive array type to schema.
     *     |        |   |       - Return schema.
     *     |        |   |
     *     |        |   +-> No:
     *     |        |       - Is it a typed array `Array<T>`?
     *     |        |           |
     *     |        |           +-> Yes:
     *     |        |           |       - Delegate to `CollectionResolver`.
     *     |        |           |
     *     |        |           +-> No:
     *     |        |                   - Log error.
     *     |        |                   - Return unknown type schema.
     *     |        |
     *     |        +-> No: Skip to next decision check.
     *     |
     *     +-> Is the type a Collection?
     *     |    |
     *     |    +-> Yes:
     *     |    |       - `CollectionResolver` processes the type.
     *     |    |       - Resolve the element type.
     *     |    |       - Traverse the element type using `TypeIntrospector`.
     *     |    |       - Build collection schema.
     *     |    |       - Return schema.
     *     |    |
     *     |    +-> No: Skip to next decision check.
     *     |
     *     +-> Is the type a Map?
     *     |     |
     *     |     +-> Yes:
     *     |     |      - `MapResolver` processes the type.
     *     |     |      - Validate that the key type is String.
     *     |     |      - Resolve the value type.
     *     |     |      - Traverse the value type using `TypeIntrospector`.
     *     |     |      - Build map schema with `additionalProperties`.
     *     |     |      - Return schema.
     *     |     |
     *     |     +-> No: Skip to next decision check.
     *     |
     *     +-> Is the type an Enum?
     *     |     |
     *     |     +-> Yes:
     *     |     |      - `EnumResolver` processes the type.
     *     |     |      - If not cached:
     *     |     |          - Extract enum values.
     *     |     |          - Build enum schema.
     *     |     |          - Add schema to cache.
     *     |     |      - Return schema or reference to schema.
     *     |     |
     *     |     +-> No: Skip to next decision check.
     *     |
     *     +-> Does the type have Type Arguments? (Generics)
     *     |    |
     *     |    +-> Yes:
     *     |    |       - `GenericsResolver` processes the type.
     *     |    |       - Generate unique name for the generic type.
     *     |    |       - If not cached:
     *     |    |           - Map type arguments to actual types.
     *     |    |           - Merge type argument bindings from outer context.
     *     |    |           - Traverse properties:
     *     |    |              - For each property:
     *     |    |                - Traverse property type using `TypeIntrospector`.
     *     |    |           - Build schema.
     *     |    |           - Add schema to cache.
     *     |    |       - Return schema or reference to schema.
     *     |    |
     *     |    +-> No: Skip to next decision check.
     *     |
     *     +-> Is the type a KClass? (Object)
     *          |
     *          +-> Yes:
     *          |       - `ObjectResolver` processes the type.
     *          |       - Check if the type is cached.
     *          |       - If not cached:
     *          |           - Add placeholder to cache.
     *          |           - Retrieve properties.
     *          |           - Traverse properties:
     *          |              - For each property:
     *          |                - Traverse property type using `TypeIntrospector`.
     *          |       - Build schema.
     *          |          - Update cache.
     *          |       - Return schema or reference to schema.
     *          |
     *          +-> No:
     *                  - Log error.
     *                  - Return unknown type schema.
     * ```
     *
     * @param kType The [KType] to resolve into a [TypeSchema].
     * @param typeArgumentBindings A map of type arguments' [KClassifier] to their actual [KType] replacements.
     * @return The resolved [TypeSchema] for the [kType].
     */
    fun traverseType(
        kType: KType,
        typeArgumentBindings: Map<KClassifier, KType>
    ): TypeSchema {
        tracer.debug("Traversing type: $kType. typeArgumentBindings=$typeArgumentBindings.")

        // Resolve the type's classifier, if unavailable, log an error and return an unknown type.
        val classifier: KClassifier = kType.classifier
            ?: run {
                tracer.error("Missing classifier. kType=$kType. typeArgumentBindings=$typeArgumentBindings.")
                return TypeSchema.ofUnknown(kType = kType)
            }

        val typeSchema: TypeSchema = when {
            // Handle primitive arrays (e.g.: IntArray), and typed arrays "Array<T>".
            TypeDescriptor.isArray(kType = kType) ->
                arrayResolver.traverse(
                    kType = kType,
                    classifier = classifier,
                    typeArgumentBindings = typeArgumentBindings
                )

            // Handle collections (e.g., List<String>, Set<Int>, etc.).
            TypeDescriptor.isCollection(classifier = classifier) ->
                collectionResolver.traverse(
                    kType = kType,
                    classifier = classifier,
                    typeArgumentBindings = typeArgumentBindings
                )

            // Handle maps (e.g., Map<String, Int>).
            classifier == Map::class ->
                mapResolver.traverse(kType = kType, typeArgumentBindings = typeArgumentBindings)

            // Handle enums.
            classifier is KClass<*> && classifier.isSubclassOf(Enum::class) ->
                enumResolver.process(enumClass = classifier)

            // Handle generics. Must be checked after arrays, collections, and maps
            // ase they also have type arguments.
            kType.arguments.isNotEmpty() && kType.arguments.none { it.type == null } ->
                genericsResolver.traverse(
                    kType = kType,
                    kClass = classifier as KClass<*>,
                    typeArgumentBindings = typeArgumentBindings
                )

            // Handle basic types and complex objects.
            // This condition must be placed last because all types are also instances of KClass.
            // If this check is placed earlier, the above branches will never be reached.
            classifier is KClass<*> ->
                objectResolver.traverse(
                    kType = kType,
                    kClass = classifier,
                    typeArgumentBindings = typeArgumentBindings
                )

            // Fallback for unknown types. This should never be reached.
            else -> {
                tracer.error("Unexpected type: $kType. typeArgumentBindings=$typeArgumentBindings.")
                TypeSchema.ofUnknown(kType = kType)
            }
        }

        return typeSchema
    }

    /**
     * Process [Collection] types (e.g.: [List], [Set], etc.),
     * by introspecting the contained element type and traversing it if needed.
     *
     * @param kType The [KType] representing the collection type.
     * @param classifier The [KClassifier] representing the [Collection]  type.
     * @param typeArgumentBindings A map of type arguments' [KClassifier] to their actual [KType] replacements.
     * @return The resolved [TypeSchema] for the collection type.
     */
    fun traverseCollection(
        kType: KType,
        classifier: KClassifier,
        typeArgumentBindings: Map<KClassifier, KType>
    ): TypeSchema {
        return collectionResolver.traverse(
            kType = kType,
            classifier = classifier,
            typeArgumentBindings = typeArgumentBindings
        )
    }

    /**
     * Processes the given [property] by traversing it and resolving its schema.
     *
     * @param classKType The [KType] of the class declaring the property.
     * @param property The [KProperty1] to process.
     * @param typeArgumentBindings A map of type arguments' [KClassifier] to their actual [KType] replacements.
     * @return A [SchemaProperty] containing information about the property.
     */
    fun traverseProperty(
        classKType: KType,
        property: KProperty1<out Any, *>,
        typeArgumentBindings: Map<KClassifier, KType>
    ): Pair<String, SchemaProperty> {
        // Traverse the property and resolve its schema.
        val property: Pair<String, SchemaProperty> = propertyResolver.traverse(
            classKType = classKType,
            property = property,
            typeArgumentBindings = typeArgumentBindings
        )

        // Decrease the reference count for transient properties.
        val schemaProperty: SchemaProperty = property.second
        if (schemaProperty.isTransient) {
            if (schemaProperty.schema is ElementSchema.Reference) {
                val referenceTypeName: String = schemaProperty.schema.referencedType.nativeName()
                decreaseReferenceCount(nativeName = referenceTypeName)
            }
        }

        return property
    }

    /**
     * Retrieves the properties from the given [kClass] preserving their declaration order.
     * This includes those defined in the primary constructor and in the class body.
     * Non-public properties are excluded from the result.
     *
     * Inherited properties are also included, being appended after the subclass's properties.
     *
     * @param kClass The [KClass] to retrieve properties from.
     * @return A list of [KProperty1] items sorted according to their declaration order.
     */
    fun getClassProperties(kClass: KClass<*>): List<KProperty1<out Any, *>> {
        return propertyResolver.getProperties(kClass = kClass)
    }

    /**
     * Determines whether the given [KType] is already present
     * in the [TypeSchema] cache.
     */
    fun isCached(kType: KType): Boolean {
        return typeSchemaCache.any { typeSchema ->
            typeSchema.type == kType.nativeName()
        }
    }

    /**
     * Caches the given [TypeSchema] object.
     */
    fun addToCache(schema: TypeSchema) {
        typeSchemaCache.add(schema)
    }

    /**
     * Increases the reference count for a [KType] given its [nativeName].
     *
     * @param nativeName The native name of the type.
     */
    fun increaseReferenceCount(nativeName: String) {
        typeSchemaUsages[nativeName] = typeSchemaUsages.getOrDefault(nativeName, 0) + 1
    }

    /**
     * Decreases the reference count for a [KType] given its [nativeName].
     *
     * @param nativeName The native name of the type.
     */
    private fun decreaseReferenceCount(nativeName: String) {
        val count: Int = typeSchemaUsages.getOrDefault(nativeName, 0)
        if (count > 0) {
            typeSchemaUsages[nativeName] = count - 1
        }
    }

    /**
     * Clears the [TypeSchema] cache.
     */
    fun clear() {
        typeSchemaCache.clear()
        typeSchemaUsages.clear()
    }
}
