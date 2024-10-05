/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */
package io.github.perracodex.kopapi.parser

import io.github.perracodex.kopapi.parser.spec.Spec
import io.github.perracodex.kopapi.parser.spec.SpecKey
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.uuid.Uuid
import com.fasterxml.jackson.annotation.JsonIgnore as JacksonJsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty as JacksonJsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName as JacksonJsonTypeName
import kotlinx.serialization.SerialName as KotlinxSerialName
import kotlinx.serialization.Transient as KotlinxTransient

/**
 * Object parser for various Kotlin types to prepare data structures
 * that can be used to construct schemas for the OpenAPI documentation.
 *
 * #### Key Features
 * - Parsing recursion for complex types.
 * - All primitive types, including enum classes.
 * - Major common types like UUID, Instant, LocalDate, etc. from both Kotlin and Java.
 * - Complex types like data classes, including nested complex Object properties.
 * - Collections like Lists, Sets, and Arrays, including primitive arrays (eg: IntArray).
 * - Maps with both complex objects or primitives.
 * - Generics support, including complex nested types.
 *
 * #### Supported Annotations
 * - Kotlinx:
 *      - `@SerialName`, `@Transient`
 * - Jackson:
 *     - `@JsonProperty`, `@JsonIgnore`
 *
 * #### Caching
 *  The parser caches processed types and object definitions to avoid duplication,
 *  so objects are uniquely processed regardless of how many times are found in the
 *  current processing context, or subsequent calls.
 */
internal object ObjectParser {
    /** Cache of [TypeDefinition] objects that have been processed. */
    private val typeDefinitionsCache: MutableSet<TypeDefinition> = mutableSetOf()

    /** Temporarily tracks processed [KType] objects while parsing, to handle generics uniquely. */
    private val inProcessTypeDefinitions: MutableSet<String> = mutableSetOf()

    /**
     * Retrieves the currently cached [TypeDefinition] objects.
     *
     * @return A set of [TypeDefinition] objects.
     */
    fun getTypeDefinitions(): Set<TypeDefinition> = typeDefinitionsCache

    /**
     * Resets the parser by clearing all processed types and object definitions.
     */
    fun reset() {
        inProcessTypeDefinitions.clear()
        typeDefinitionsCache.clear()
        TypeDefinitionWarningManager.clear()
    }

    /**
     * Parses a KType to its [TypeDefinition] representation.
     *
     * @param kType The KType to parse.
     * @return The [TypeDefinition] for the given [kType].
     */
    fun process(kType: KType): TypeDefinition {
        val result: TypeDefinition = traverseTypeDefinition(kType = kType, typeParameterMap = emptyMap())
        TypeDefinitionWarningManager.analyze(newTypeDefinition = result)
        return result
    }

    /**
     * Traverses and resolves the given [KType], handling both simple and complex types,
     * including collections, maps, enums, and generics. Manages recursion and self-referencing types.
     *
     * Returns a [TypeDefinition] representing the structure of the [kType], considering generic parameters
     * nullable properties, and any annotations present.
     *
     * @param kType The [KType] to resolve into a [TypeDefinition].
     * @param typeParameterMap A map of type parameters' [KClassifier] to their corresponding [KType].
     * @return The resolved [TypeDefinition] for the [kType].
     */
    private fun traverseTypeDefinition(
        kType: KType,
        typeParameterMap: Map<KClassifier, KType>
    ): TypeDefinition {
        val classifier: KClassifier = kType.classifier
            ?: throw IllegalArgumentException("KType must have a classifier.")

        val typeDefinition: TypeDefinition = when {
            // Handle collections (e.g., List<String>, Set<Int>).
            classifier == List::class || classifier == Set::class || isArrayType(classifier = classifier) ->
                handleCollectionType(kType = kType, classifier = classifier, typeParameterMap = typeParameterMap)

            // Handle maps (e.g., Map<String, Int>).
            classifier == Map::class ->
                handleMapType(kType = kType, typeParameterMap = typeParameterMap)

            // Handle enums.
            classifier is KClass<*> && classifier.isSubclassOf(Enum::class) ->
                handleEnumType(enumClass = classifier)

            // Handle generics.
            kType.arguments.isNotEmpty() ->
                handleGenericsType(kType = kType, kClass = classifier as KClass<*>, typeParameterMap = typeParameterMap)

            // Handle basic types and complex objects.
            // This condition must be placed last because all types are also instances of KClass.
            // If this check is placed earlier, the above branches will never be reached.
            classifier is KClass<*> ->
                handleComplexOrBasicType(kType = kType, kClass = classifier, typeParameterMap = typeParameterMap)

            // Fallback for unknown types. This should never be reached.
            else ->
                TypeDefinition.of(
                    name = "Unknown_$kType",
                    kType = kType,
                    definition = Spec.objectType
                )
        }

        // Mark the TypeDefinition as nullable if the KType is nullable.
        if (kType.isMarkedNullable) {
            typeDefinition.definition[SpecKey.REQUIRED()] = false
        }

        return typeDefinition
    }

    /**
     * Handles collections (eg: List, Set), including primitive and non-primitive arrays.
     *
     * @param kType The [KType] representing the collection type.
     * @param classifier The [KClassifier] representing the collection class (e.g., List, Set).
     * @param typeParameterMap A map of type parameters' [KClassifier] to actual [KType] items for replacement.
     * @return The resolved [TypeDefinition] for the collection type.
     */
    private fun handleCollectionType(
        kType: KType,
        classifier: KClassifier,
        typeParameterMap: Map<KClassifier, KType>
    ): TypeDefinition {
        // Check if the classifier is a primitive array first, such as IntArray, ByteArray, etc.
        if (isPrimitiveArrayType(classifier = classifier)) {
            val definition: MutableMap<String, Any>? = mapPrimitiveType(kClass = classifier as KClass<*>)
            val className: String = getClassName(kClass = classifier)
            return TypeDefinition.of(
                name = className,
                kType = kType,
                definition = definition ?: Spec.objectType
            )
        }

        // Handle non-primitive arrays and collections based on their type arguments.
        val className: String = getClassName(kClass = (classifier as KClass<*>))
        val itemType: KType = kType.arguments.firstOrNull()?.type?.let {
            replaceTypeIfNeeded(type = it, typeParameterMap = typeParameterMap)
        } ?: return TypeDefinition.of(
            name = className,
            kType = kType,
            definition = Spec.objectType
        )

        // Map the item type to its respective TypeDefinition,
        // considering it's a regular object array or collection.
        val typeDefinition: TypeDefinition = traverseTypeDefinition(
            kType = itemType,
            typeParameterMap = typeParameterMap
        )

        return TypeDefinition.of(
            name = "ArrayOf${typeDefinition.name}",
            kType = kType,
            definition = Spec.collection(value = typeDefinition.definition)
        )
    }

    /**
     * Determines whether the given [KClassifier] represents any array type in Kotlin,
     * including both primitive arrays (e.g., [IntArray], [DoubleArray])
     * and generic object arrays (e.g., [Array]<String>).
     *
     * Unlike standard generic classes like [List], array types in Kotlin are represented by distinct classes
     * for each primitive type and a generic [Array] class for reference types. This distinction means that
     * identifying an array type requires checking against all possible array classifiers, both primitive
     * and generic.
     *
     * @param classifier The [KClassifier] of the [KType] to evaluate.
     * @return True if the [classifier] corresponds to any Kotlin array type, otherwise False.
     */
    private fun isArrayType(classifier: KClassifier): Boolean {
        return isPrimitiveArrayType(classifier = classifier)
                || (classifier as? KClass<*>)?.javaObjectType?.isArray ?: false
    }

    /**
     * Determines whether the given [KClassifier] represents a specialized primitive array type.
     *
     * Kotlin provides specialized array classes for each primitive type (e.g., [IntArray], [ByteArray], [FloatArray]),
     * which are distinct from the generic [Array] class used for reference types.
     *
     * @param classifier The [KClassifier] of the [KType] to evaluate.
     * @return True if the [classifier] is one of Kotlin's primitive array types, otherwise False.
     */
    private fun isPrimitiveArrayType(classifier: KClassifier): Boolean {
        return classifier == IntArray::class || classifier == ByteArray::class ||
                classifier == ShortArray::class || classifier == FloatArray::class ||
                classifier == DoubleArray::class || classifier == LongArray::class ||
                classifier == CharArray::class || classifier == BooleanArray::class ||
                classifier == UIntArray::class || classifier == ULongArray::class ||
                classifier == UByteArray::class || classifier == UShortArray::class
    }

    /**
     * Resolves a map type (e.g., Map<String, Int>) to a [TypeDefinition].
     *
     * Maps do not generate their own schema definition references,
     * but a definition reference will be created for the value type if such is a complex object.
     *
     * @param kType The [KType] representing the map type.
     * @param typeParameterMap A map of type parameters' [KClassifier] to actual [KType] items for replacement.
     * @return The resolved [TypeDefinition] for the map, with additionalProperties for the value type.
     * @throws IllegalArgumentException if the map's key type is not [String].
     */
    private fun handleMapType(
        kType: KType,
        typeParameterMap: Map<KClassifier, KType>
    ): TypeDefinition {
        val keyType: KType? = kType.arguments.getOrNull(index = 0)?.type?.let {
            replaceTypeIfNeeded(type = it, typeParameterMap = typeParameterMap)
        }
        val valueType: KType? = kType.arguments.getOrNull(index = 1)?.type?.let {
            replaceTypeIfNeeded(type = it, typeParameterMap = typeParameterMap)
        }

        // OpenAPI requires keys to be strings.
        if (keyType == null || keyType.classifier != String::class) {
            throw IllegalArgumentException("Maps must have strings as keys. Found key type: $keyType")
        }

        // Process the value type.
        val typeDefinition: TypeDefinition = valueType?.let {
            traverseTypeDefinition(kType = it, typeParameterMap = typeParameterMap)
        } ?: TypeDefinition.of(name = "MapOf${kType}", kType = kType, definition = Spec.objectType)

        return TypeDefinition.of(
            name = "MapOf${typeDefinition.name}",
            kType = kType,
            definition = Spec.additionalProperties(value = typeDefinition.definition)
        )
    }

    /**
     * Handles enums classes.
     *
     * @param enumClass The [KClass] representing the enum type.
     * @return The [TypeDefinition] for the enum type.
     */
    private fun handleEnumType(enumClass: KClass<*>): TypeDefinition {
        val enumValues: List<String> = enumClass.java.enumConstants?.map {
            (it as Enum<*>).name
        } ?: emptyList()

        // Create the TypeDefinition for the enum as a separate object.
        val enumClassName: String = getClassName(kClass = enumClass)
        val definition: TypeDefinition = TypeDefinition.of(
            name = enumClassName,
            kType = enumClass.createType(),
            definition = Spec.enum(values = enumValues)
        )

        // Add the enum definition to the object definitions if it's not already present.
        typeDefinitionsCache.add(definition)

        // Return a reference to the enum definition
        return TypeDefinition.of(
            name = enumClassName,
            kType = enumClass.createType(),
            definition = buildDefinitionReference(name = enumClassName)
        )
    }

    /**
     * Resolves a complex or basic type (such as data classes or primitives) into a [TypeDefinition].
     *
     * Resolves both Kotlin primitives and complex types like data classes. It also
     * manages recursive structures, circular dependencies, and caches type definitions for reuse.
     *
     * Primitive types are immediately mapped, while complex types are recursively processed,
     * including their properties.
     *
     * @param kType The [KType] representing the type to resolve.
     * @param kClass The [KClass] corresponding to the type to resolve.
     * @param typeParameterMap A map of type parameters' [KClassifier] to their corresponding [KType].
     * @return The resolved [TypeDefinition] for the complex or basic type.
     */
    private fun handleComplexOrBasicType(
        kType: KType,
        kClass: KClass<*>,
        typeParameterMap: Map<KClassifier, KType>
    ): TypeDefinition {
        val className: String = getClassName(kClass = kClass)

        // Handle primitive types.
        mapPrimitiveType(kClass = kClass)?.let { definition ->
            return TypeDefinition.of(
                name = className,
                kType = kType,
                definition = definition
            )
        }

        // Prevent infinite recursion for self-referencing objects.
        if (inProcessTypeDefinitions.contains(kType.nativeName())) {
            return TypeDefinition.of(
                name = className,
                kType = kType,
                definition = buildDefinitionReference(name = className)
            )
        }

        // Process complex types like data classes.
        inProcessTypeDefinitions.add(kType.nativeName())

        // Create an empty definition before processing properties to handle circular dependencies.
        val propertiesMap: MutableMap<String, Any> = mutableMapOf()
        val placeholder: TypeDefinition = TypeDefinition.of(
            name = className,
            kType = kType,
            definition = Spec.properties(value = propertiesMap)
        )
        typeDefinitionsCache.add(placeholder)

        // Create a mutable map for properties.
        val properties: MutableMap<String, Map<String, Any>> = mutableMapOf()

        // Step 1: Get the sorted properties based on the primary constructor's parameter order.
        val sortedProperties: List<KProperty1<out Any, *>> = getSortedProperties(kClass = kClass)

        // Step 2: Process each property using the helper function.
        sortedProperties.forEach { property ->
            val (propertySerializedName, extendedDefinition) = processProperty(
                property = property,
                typeParameterMap = typeParameterMap
            )
            properties[propertySerializedName] = extendedDefinition
        }

        // Add properties to the definition.
        propertiesMap.putAll(properties)
        // Remove once done to handle different branches.
        inProcessTypeDefinitions.remove(kType.nativeName())

        return TypeDefinition.of(
            name = className,
            kType = kType,
            definition = buildDefinitionReference(name = className)
        )
    }

    /**
     * Handles generics types like SomeType<SomeObject>, considering nested and complex generics.
     *
     * @param kType The KType representing the generics type.
     * @param kClass The KClass representing the generics type.
     * @param typeParameterMap A map of type parameters' [KClassifier] to actual [KType] for replacement.
     * @return The [TypeDefinition] for the generics type.
     */
    private fun handleGenericsType(
        kType: KType,
        kClass: KClass<*>,
        typeParameterMap: Map<KClassifier, KType>
    ): TypeDefinition {
        val genericsTypeName: String = generateGenericsTypeName(kType = kType, kClass = kClass)

        // Check if the generics type has already been processed.
        if (typeDefinitionsCache.none { it.type == kType.nativeName() }) {
            resolveGenericsType(
                kType = kType,
                kClass = kClass,
                genericsTypeName = genericsTypeName,
                parentTypeParameterMap = typeParameterMap
            )
        }

        return TypeDefinition.of(
            name = genericsTypeName,
            kType = kType,
            definition = buildDefinitionReference(name = genericsTypeName)
        )
    }

    /**
     * Retrieves and sorts properties based on the primary constructor's parameter order.
     * For classes without a primary constructor, properties are sorted based on their declaration order.
     *
     * @param kClass The Kotlin class.
     * @return A list of KProperty1 sorted according to the constructor's parameter order.
     */
    private fun getSortedProperties(kClass: KClass<*>): List<KProperty1<out Any, *>> {
        val primaryConstructor: KFunction<Any>? = kClass.primaryConstructor
        val constructorParameters: List<String> = primaryConstructor?.parameters?.mapNotNull { it.name } ?: emptyList()

        // Map property names to KProperty1 objects.
        val propertyMap: Map<String, KProperty1<out Any, *>> = kClass.declaredMemberProperties.associateBy { it.name }

        // Sort properties based on constructor parameter order.
        val sortedProperties: List<KProperty1<out Any, *>> = constructorParameters.mapNotNull { propertyMap[it] }

        // Append any additional properties not defined in the constructor.
        val additionalProperties: List<KProperty1<out Any, *>> = propertyMap.keys
            .subtract(constructorParameters.toSet())
            .mapNotNull { propertyMap[it] }

        return sortedProperties + additionalProperties
    }

    /**
     * Processes a property by mapping its type, handling annotations,
     * and preparing the definition entry.
     *
     * @param property The property to process.
     * @param typeParameterMap A map of type parameter classifiers to actual KTypes for replacement.
     * @return A Pair containing the serialized name and the extended definition map.
     */
    private fun processProperty(
        property: KProperty1<*, *>,
        typeParameterMap: Map<KClassifier, KType>
    ): Pair<String, Map<String, Any>> {
        val propertyName: String = getPropertyName(property = property)

        val propertyType: KType = replaceTypeIfNeeded(
            type = property.returnType,
            typeParameterMap = typeParameterMap
        )

        val typeDefinition: TypeDefinition = traverseTypeDefinition(
            kType = propertyType,
            typeParameterMap = typeParameterMap
        )

        val propertyMetadata: Map<String, Any>? = handlePropertyAnnotations(
            property = property,
            propertyName = propertyName
        )

        val extendedDefinition: MutableMap<String, Any> = typeDefinition.definition.apply {
            propertyMetadata?.let { putAll(it) }
        }

        return propertyName to extendedDefinition
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
    private fun generateGenericsTypeName(kType: KType, kClass: KClass<*>): String {
        val arguments: List<KClass<*>> = kType.arguments.mapNotNull { it.type?.classifier as? KClass<*> }
        val className: String = getClassName(kClass = kClass)
        val argumentsNames: List<String> = arguments.map {
            getClassName(kClass = it)
        }

        return if (argumentsNames.size == 1) {
            "${className}Of${argumentsNames.first()}"
        } else {
            "${className}Of${argumentsNames.joinToString(separator = "Of")}"
        }
    }

    /**
     * Resolves generics types, handling nested complex generics.
     *
     * @param kClass The [kClass] representing the generic type.
     * @param kType The [KType] containing the actual types for the generics.
     * @param genericsTypeName The generated name for the generics type.
     * @param parentTypeParameterMap A map of type parameter classifiers to actual [KType] objects for replacement.
     */
    private fun resolveGenericsType(
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
        typeDefinitionsCache.add(placeholder)

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
        val sortedProperties: List<KProperty1<out Any, *>> = getSortedProperties(kClass = kClass)

        // Iterate over each sorted property in the generic class.
        sortedProperties.forEach { sortedProperty ->
            val (serializedName, extendedDefinition) = processProperty(
                property = sortedProperty,
                typeParameterMap = combinedTypeParameterMap
            )
            properties[serializedName] = extendedDefinition
        }

        // Update the placeholder definition  with actual properties.
        placeholder.definition.putAll(Spec.properties(value = properties))
    }

    /**
     * Returns the corresponding type from [typeParameterMap] if the [type]'s classifier
     * is found in the given [typeParameterMap].
     * Otherwise, returns the provided [type].
     *
     * @param type The [KType] to check.
     * @param typeParameterMap A map where type parameters are mapped to their actual [KType] values.
     * @return The [KType] from the map if the classifier is found, otherwise the provided [type].
     */
    private fun replaceTypeIfNeeded(
        type: KType,
        typeParameterMap: Map<KClassifier, KType>
    ): KType {
        val classifier: KClassifier? = type.classifier
        return if (classifier in typeParameterMap) {
            typeParameterMap[classifier]!!
        } else {
            type
        }
    }

    /**
     * Maps primitive Kotlin types (e.g., Int, String).
     */
    private fun mapPrimitiveType(kClass: KClass<*>): MutableMap<String, Any>? {
        return when (kClass) {
            // Basic Kotlin Types.
            String::class, CharSequence::class -> Spec.string
            Char::class -> Spec.char
            Boolean::class -> Spec.boolean
            Int::class -> Spec.int32
            Long::class -> Spec.int64
            Double::class -> Spec.double
            Float::class -> Spec.float
            Short::class -> Spec.int32
            Byte::class -> Spec.int32
            UInt::class -> Spec.int32
            ULong::class -> Spec.int64
            UShort::class -> Spec.int32
            UByte::class -> Spec.int32

            // Primitive Arrays.
            IntArray::class, ShortArray::class, UIntArray::class, UShortArray::class -> Spec.array(spec = Spec.int32)
            LongArray::class, ULongArray::class -> Spec.array(spec = Spec.int64)
            FloatArray::class -> Spec.array(spec = Spec.float)
            DoubleArray::class -> Spec.array(spec = Spec.double)
            BooleanArray::class -> Spec.array(spec = Spec.boolean)
            CharArray::class -> Spec.array(spec = Spec.char)
            ByteArray::class, UByteArray::class -> Spec.array(spec = Spec.byte)

            // UUID Types.
            Uuid::class, UUID::class -> Spec.uuid

            // Kotlin Date/Time Types.
            kotlinx.datetime.LocalDate::class -> Spec.date
            kotlinx.datetime.LocalDateTime::class -> Spec.dateTime
            kotlinx.datetime.Instant::class -> Spec.dateTime
            kotlinx.datetime.LocalTime::class -> Spec.time

            // Java Date/Time Types.
            java.time.OffsetDateTime::class -> Spec.dateTime
            java.time.ZonedDateTime::class -> Spec.dateTime
            java.time.LocalTime::class -> Spec.time
            java.time.LocalDate::class -> Spec.date
            java.time.LocalDateTime::class -> Spec.dateTime
            java.time.Instant::class -> Spec.dateTime
            java.util.Date::class -> Spec.dateTime
            java.sql.Date::class -> Spec.date

            // Big Numbers.
            BigDecimal::class -> Spec.double
            BigInteger::class -> Spec.int64

            // URL and URI.
            io.ktor.http.Url::class -> Spec.uri
            java.net.URL::class -> Spec.uri
            java.net.URI::class -> Spec.uri

            else -> null // Return null if it's not a primitive type.
        }
    }

    /**
     * Resolves the name for the give [kClass], considering serializer annotations if present.
     *
     * @param kClass The [KClass] to process for name resolution.
     * @return The resolved class name.
     */
    private fun getClassName(kClass: KClass<*>): String {
        return geElementName(target = kClass)
    }

    /**
     * Resolves the name for the give [property], considering serializer annotations if present.
     *
     * @param property The [KProperty1] to process for name resolution.
     * @return The resolved property name.
     */
    private fun getPropertyName(property: KProperty1<*, *>): String {
        return geElementName(target = property)
    }

    /**
     * Resolves the name of the given [target], either from specific annotations if present,
     * or from the target's own name.
     *
     * @param target The target ([KClass] or [KProperty1]) to process for name resolution.
     * @return The resolved name from annotations or the target's own name if no annotation is found.
     * @throws IllegalArgumentException if the target is not a supported type.
     */
    private fun geElementName(target: Any): String {
        // List of pairs containing annotation lookup functions and the way to extract the relevant value.
        val annotationCheckers: Set<(Any) -> String?> = setOf(
            { element -> (element as? KClass<*>)?.findAnnotation<KotlinxSerialName>()?.value },
            { element -> (element as? KProperty1<*, *>)?.findAnnotation<KotlinxSerialName>()?.value },
            { element -> (element as? KClass<*>)?.findAnnotation<JacksonJsonTypeName>()?.value },
            { element -> (element as? KProperty1<*, *>)?.findAnnotation<JacksonJsonProperty>()?.value }
        )

        // Iterate over the annotation checkers to find the first non-blank name.
        annotationCheckers.forEach { checker ->
            checker(target)?.let { serialName ->
                if (serialName.isNotBlank()) {
                    return serialName
                }
            }
        }

        // Fallback to the object name if no annotations are found.
        return when (target) {
            is KClass<*> -> target.safeName()
            is KProperty1<*, *> -> target.name
            else -> throw IllegalArgumentException("Unsupported target type: ${target::class.simpleName}")
        }
    }

    /**
     * Handles property annotations like `@SerialName`, `@Transient`, and adds relevant metadata.
     *
     * @param property The [KProperty1] to process.
     * @param propertyName The currently resolved element name, which could be the serialized name.
     * @return A map of metadata for the property, or null if no annotations are found.
     */
    private fun handlePropertyAnnotations(
        property: KProperty1<*, *>,
        propertyName: String
    ): Map<String, Any>? {
        val metadata: MutableMap<String, Any> = mutableMapOf()

        // Handle Transient annotations.
        if (property.findAnnotation<KotlinxTransient>() != null || property.findAnnotation<JacksonJsonIgnore>() != null) {
            metadata[SpecKey.TRANSIENT()] = true
        }

        // Handle serialized name changes.
        if (propertyName != property.name) {
            metadata[SpecKey.ORIGINAL_NAME()] = property.name
        }

        return if (metadata.isEmpty()) null else metadata
    }

    /**
     * Extension function to safely get a class name.
     * If The name cannot be determined, it creates a fallback name based on the class type.
     */
    fun KClass<*>.safeName(): String {
        return this.simpleName
            ?: this.qualifiedName?.substringAfterLast(delimiter = '.')
            ?: "UnknownClass_${this.toString().replace(Regex(pattern = "[^A-Za-z0-9_]"), replacement = "_")}"
    }

    /**
     * Converts an object to a schema reference.
     */
    private fun buildDefinitionReference(name: String): MutableMap<String, Any> {
        return mutableMapOf("\$ref" to "#/components/schemas/$name")
    }
}
