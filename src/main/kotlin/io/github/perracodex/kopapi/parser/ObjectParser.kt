/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */
package io.github.perracodex.kopapi.parser

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
 * - All primitive types, including enums.
 * - Major common types like UUID, Instant, LocalDate, etc. from both Kotlin and Java.
 * - Complex types like data classes, including nested complex Object properties.
 * - Collections like Lists, Sets, and Arrays, including primitive arrays (eg: IntArray).
 * - Maps with both complex objects or primitives.
 * - Generics support, including complex nested types (eg: List<Page<Array<ObjectB>>>)
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
    fun resolveTypeDefinition(kType: KType): TypeDefinition {
        val result: TypeDefinition = processObject(kType = kType, typeParameterMap = emptyMap())
        TypeDefinitionWarningManager.analyze(newTypeDefinition = result)
        return result
    }

    /**
     * Recursively maps the given [kType] KType, handling recursion and complex types.
     *
     * @param kType The [KType] to recursively map.
     * @param typeParameterMap A map of type parameter classifiers to actual KTypes for replacement.
     * @return The [TypeDefinition]for the given [kType].
     */
    private fun processObject(
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

            // Handle basic types and complex objects.
            // This condition must be placed last because all types are also instances of KClass.
            // If this check is placed earlier, the above checks will never be reached.
            classifier is KClass<*> ->
                handleComplexOrBasicType(kType = kType, kClass = classifier, typeParameterMap = typeParameterMap)

            // Fallback for unknown or unsupported types.
            else ->
                TypeDefinition.create(name = "Unknown_$kType", kType = kType, definition = mutableMapOf(typeObject))
        }

        // Mark the TypeDefinition as nullable if the KType is nullable.
        if (kType.isMarkedNullable) {
            typeDefinition.definition[SpecKey.REQUIRED()] = false
        }

        return typeDefinition
    }

    /**
     * Handles collections (List, Set).
     *
     * @param kType The [KType] representing the collection type.
     * @param classifier The [KClassifier] representing the collection class (e.g., List, Set).
     * @param typeParameterMap A map of type parameter classifiers to actual [KType] items for replacement.
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
            val serializedName: String = getSerializedClassName(kClass = classifier)
            return TypeDefinition.create(
                name = serializedName,
                kType = kType,
                definition = definition ?: mutableMapOf(typeObject)
            )
        }

        // Handle non-primitive arrays and collections based on their type arguments.
        val serializedName: String = getSerializedClassName(kClass = (classifier as KClass<*>))
        val itemType: KType = kType.arguments.firstOrNull()?.type?.let {
            replaceTypeIfNeeded(type = it, typeParameterMap = typeParameterMap)
        } ?: return TypeDefinition.create(
            name = serializedName,
            kType = kType,
            definition = mutableMapOf(typeObject)
        )

        // Map the item type to its respective TypeDefinition,
        // considering it's a regular object array or collection.
        val typeDefinition: TypeDefinition = processObject(
            kType = itemType,
            typeParameterMap = typeParameterMap
        )

        return TypeDefinition.create(
            name = "ArrayOf${typeDefinition.name}",
            kType = kType,
            definition = mutableMapOf(typeArray, SpecKey.ITEMS() to typeDefinition.definition)
        )
    }

    /**
     * Checks if a KClassifier represents any array type,
     * which includes both primitive arrays and arrays of objects (e.g., Array<String>).
     *
     * Unlike List<T>, where the type parameter information is retained at runtime
     * due to reified type parameters in inline functions, generic array types (like Array<T>)
     * do not retain their specific type information because of type erasure. This limitation
     * necessitates the use of Java's reflection capabilities to identify array types accurately.
     *
     * @param classifier The [KClassifier] of the KType to check.
     * @return True if the classifier represents an array type, false otherwise.
     */
    private fun isArrayType(classifier: KClassifier): Boolean {
        return isPrimitiveArrayType(classifier = classifier)
                || (classifier as? KClass<*>)?.javaObjectType?.isArray ?: false
    }

    /**
     * Checks if a KClassifier represents a primitive array type.
     *
     * @param classifier The [KClassifier] of the KType to check.
     * @return True if the classifier is a primitive array type, false otherwise.
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
     * Handles maps (e.g., Map<String, Int>).
     *
     * @param kType The [KType] representing the map type.
     * @param typeParameterMap A map of type parameter classifiers to actual [KType] items for replacement.
     * @return The resolved [TypeDefinition] for the map type.
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
            processObject(kType = it, typeParameterMap = typeParameterMap)
        } ?: TypeDefinition.create(name = "MapOf${kType}", kType = kType, definition = mutableMapOf(typeObject))

        return TypeDefinition.create(
            name = "MapOf${typeDefinition.name}",
            kType = kType,
            definition = mutableMapOf(typeObject, "additionalProperties" to typeDefinition.definition)
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
        val serializedEnumName: String = getSerializedClassName(kClass = enumClass)
        val definition: TypeDefinition = TypeDefinition.create(
            name = serializedEnumName,
            kType = enumClass.createType(),
            definition = mutableMapOf(typeString, SpecKey.ENUM() to enumValues)
        )

        // Add the enum definition to the object definitions if it's not already present.
        typeDefinitionsCache.add(definition)

        // Return a reference to the enum definition
        val enumName: String = getSerializedClassName(kClass = enumClass)
        return TypeDefinition.create(
            name = enumName,
            kType = enumClass.createType(),
            definition = buildDefinitionReference(name = enumName)
        )
    }

    /**
     * Handles complex or basic types, such as data classes or primitive types.
     * For complex types, it adds them as a separate object in the objectDefinitions map and creates a $ref.
     *
     * @param kType The KType representing the object type.
     * @param kClass The KClass representing the complex or basic type.
     * @param typeParameterMap A map of type parameter classifiers to actual KTypes for replacement.
     * @return The [TypeDefinition] for the complex or basic type.
     */
    private fun handleComplexOrBasicType(
        kType: KType,
        kClass: KClass<*>,
        typeParameterMap: Map<KClassifier, KType>
    ): TypeDefinition {
        val classSerializedName: String = getSerializedClassName(kClass = kClass)

        // Handle primitive types.
        mapPrimitiveType(kClass = kClass)?.let { definition ->
            return TypeDefinition.create(
                name = classSerializedName,
                kType = kType,
                definition = definition
            )
        }

        // Handle generics.
        if (kType.arguments.isNotEmpty()) {
            val genericTypeName: String = generateGenericTypeName(kType = kType, kClass = kClass)
            if (typeDefinitionsCache.none { it.type == kType.nativeName() }) {
                handleGenericType(
                    kType = kType,
                    kClass = kClass,
                    genericTypeName = genericTypeName,
                    parentTypeParameterMap = typeParameterMap
                )
            }
            return TypeDefinition.create(
                name = genericTypeName,
                kType = kType,
                definition = buildDefinitionReference(name = genericTypeName)
            )
        }

        // Prevent infinite recursion for self-referencing objects.
        if (inProcessTypeDefinitions.contains(kType.nativeName())) {
            return TypeDefinition.create(
                name = classSerializedName,
                kType = kType,
                definition = buildDefinitionReference(name = classSerializedName)
            )
        }

        // Process complex types like data classes.
        inProcessTypeDefinitions.add(kType.nativeName())

        // Create an empty definition before processing properties to handle circular dependencies.
        val propertiesMap: MutableMap<String, Any> = mutableMapOf()
        val definition: MutableMap<String, Any> = mutableMapOf(typeObject, "properties" to propertiesMap)
        val placeholder: TypeDefinition = TypeDefinition.create(
            name = classSerializedName,
            kType = kType,
            definition = definition
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

        return TypeDefinition.create(
            name = classSerializedName,
            kType = kType,
            definition = buildDefinitionReference(name = classSerializedName)
        )
    }

    /**
     * Retrieves and sorts properties based on the primary constructor's parameter order.
     * For classes without a primary constructor, properties are sorted based on their declaration order.
     * @param kClass The Kotlin class.
     * @return A list of KProperty1 sorted according to the constructor's parameter order.
     */
    private fun getSortedProperties(kClass: KClass<*>): List<KProperty1<out Any, *>> {
        val primaryConstructor: KFunction<Any>? = kClass.primaryConstructor
        val constructorParameters: List<String> = primaryConstructor?.parameters?.mapNotNull { it.name } ?: emptyList()

        // Map property names to KProperty1
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

        val serializedName: String = getSerializedPropertyName(property = property)

        val propertyType: KType = replaceTypeIfNeeded(
            type = property.returnType,
            typeParameterMap = typeParameterMap
        )

        val typeDefinition: TypeDefinition = processObject(
            kType = propertyType,
            typeParameterMap = typeParameterMap
        )

        val propertyMetadata: Map<String, Any>? = handlePropertyAnnotations(
            property = property,
            serializedName = serializedName
        )

        val extendedDefinition: MutableMap<String, Any> = typeDefinition.definition.apply {
            propertyMetadata?.let { putAll(it) }
        }

        return serializedName to extendedDefinition
    }

    /**
     * Generates a unique and consistent name for a generic type, such as Page<Employee> becomes PageOfEmployee.
     * Handles multiple type parameters by joining them with 'Of'.
     * Falls back to using qualified names if simpleName is not available.
     */
    private fun generateGenericTypeName(kType: KType, kClass: KClass<*>): String {
        val genericArgs: List<KClass<*>> = kType.arguments.mapNotNull { it.type?.classifier as? KClass<*> }
        val baseName: String = getSerializedClassName(kClass = kClass)
        val genericArgsNames: List<String> = genericArgs.map { it.safeName() }

        return if (genericArgsNames.size == 1) {
            "${baseName}Of${genericArgsNames.first()}"
        } else {
            "${baseName}Of${genericArgsNames.joinToString(separator = "Of")}"
        }
    }

    /**
     * Handles generic types like Page<Employee> and creates a definition for them.
     *
     * @param kClass The Kotlin class representing the generic type.
     * @param kType The KType containing the actual types for the generics.
     * @param genericTypeName The generated name for the generic type.
     * @param parentTypeParameterMap A map of type parameter classifiers to actual KTypes for replacement.
     */
    private fun handleGenericType(
        kType: KType,
        kClass: KClass<*>,
        genericTypeName: String,
        parentTypeParameterMap: Map<KClassifier, KType>
    ) {
        // Add a placeholder definition early to avoid circular references.
        val placeholder: TypeDefinition = TypeDefinition.create(
            name = genericTypeName,
            kType = kType,
            definition = mutableMapOf(typeObject, SpecKey.PROPERTIES() to mutableMapOf<String, Any>())
        )
        typeDefinitionsCache.add(placeholder)

        // Retrieve the type parameters from the generic class.
        val typeParameters: List<KTypeParameter> = kClass.typeParameters

        // Retrieve the actual generic arguments provided.
        val genericArgs: List<KType> = kType.arguments.mapNotNull { it.type }

        // Ensure the number of type parameters matches the number of generic arguments.
        if (typeParameters.size != genericArgs.size) {
            throw IllegalArgumentException(
                "Type parameter count mismatch for $kClass. " +
                        "Expected ${typeParameters.size}, but got ${genericArgs.size}."
            )
        }

        // Create a map of type parameters to their actual types.
        val currentTypeParameterMap: Map<KClassifier, KType> = typeParameters
            .mapIndexed { index, typeParam ->
                typeParam as KClassifier to genericArgs[index]
            }.toMap()

        // Merge parent type parameters with current type parameters.
        val combinedTypeParameterMap: Map<KClassifier, KType> = parentTypeParameterMap + currentTypeParameterMap

        // Prepare a map to hold the properties for the generic instance.
        val properties: MutableMap<String, Any> = mutableMapOf()

        // Retrieve sorted properties based on the primary constructor's parameter order
        val sortedProperties: List<KProperty1<out Any, *>> = getSortedProperties(kClass)

        // Iterate over each sorted property in the generic class.
        sortedProperties.forEach { sortedProperty ->
            val (serializedName, extendedDefinition) = processProperty(
                property = sortedProperty,
                typeParameterMap = combinedTypeParameterMap
            )
            properties[serializedName] = extendedDefinition
        }

        // Update the placeholder definition  with actual properties.
        placeholder.definition.putAll(
            mapOf(typeObject, "properties" to properties)
        )
    }

    /**
     * Replaces type parameters in a generic property type with actual types from the type parameter map.
     *
     * @param type The original KType to be processed.
     * @param typeParameterMap A map of type parameter classifiers to actual KTypes for replacement.
     * @return The processed KType with type parameters replaced as needed.
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
            String::class, CharSequence::class -> mutableMapOf(typeString)
            Char::class -> mutableMapOf(typeString, formatChar)
            Boolean::class -> mutableMapOf(typeString, typeBoolean)
            Int::class -> mutableMapOf(typeInteger, formatInt32)
            Long::class -> mutableMapOf(typeInteger, formatInt64)
            Double::class -> mutableMapOf(typeNumber, formatDouble)
            Float::class -> mutableMapOf(typeNumber, formatFloat)
            Short::class -> mutableMapOf(typeInteger, formatInt32)
            Byte::class -> mutableMapOf(typeInteger, formatInt32)
            UInt::class -> mutableMapOf(typeInteger, formatInt32)
            ULong::class -> mutableMapOf(typeInteger, formatInt64)
            UShort::class -> mutableMapOf(typeInteger, formatInt32)
            UByte::class -> mutableMapOf(typeInteger, formatInt32)

            // Primitive Arrays.
            IntArray::class, UIntArray::class -> mutableMapOf(typeArray, SpecKey.ITEMS() to mutableMapOf(typeInteger, formatInt32))
            ByteArray::class, UByteArray::class -> mutableMapOf(typeArray, SpecKey.ITEMS() to mutableMapOf(typeString, formatByte))
            ShortArray::class, UShortArray::class -> mutableMapOf(typeArray, SpecKey.ITEMS() to mutableMapOf(typeInteger, formatInt32))
            FloatArray::class -> mutableMapOf(typeArray, SpecKey.ITEMS() to mutableMapOf(typeNumber, formatFloat))
            DoubleArray::class -> mutableMapOf(typeArray, SpecKey.ITEMS() to mutableMapOf(typeNumber, formatDouble))
            LongArray::class, ULongArray::class -> mutableMapOf(typeArray, SpecKey.ITEMS() to mutableMapOf(typeInteger, formatInt64))
            CharArray::class -> mutableMapOf(typeArray, SpecKey.ITEMS() to mutableMapOf(typeString, formatChar))
            BooleanArray::class -> mutableMapOf(typeArray, SpecKey.ITEMS() to mutableMapOf(typeBoolean))

            // UUID Types.
            Uuid::class, UUID::class -> mutableMapOf(typeString, formatUuid)

            // Kotlin Date/Time Types.
            kotlinx.datetime.LocalDate::class -> mutableMapOf(typeString, formatDate)
            kotlinx.datetime.LocalDateTime::class -> mutableMapOf(typeString, formatDateTime)
            kotlinx.datetime.Instant::class -> mutableMapOf(typeString, formatDateTime)
            kotlinx.datetime.LocalTime::class -> mutableMapOf(typeString, formatTime)

            // Java Date/Time Types.
            java.time.OffsetDateTime::class -> mutableMapOf(typeString, formatDateTime)
            java.time.ZonedDateTime::class -> mutableMapOf(typeString, formatDateTime)
            java.time.LocalTime::class -> mutableMapOf(typeString, formatTime)
            java.time.LocalDate::class -> mutableMapOf(typeString, formatDate)
            java.time.LocalDateTime::class -> mutableMapOf(typeString, formatDateTime)
            java.time.Instant::class -> mutableMapOf(typeString, formatDateTime)
            java.util.Date::class -> mutableMapOf(typeString, formatDateTime)
            java.sql.Date::class -> mutableMapOf(typeString, formatDate)

            // Big Numbers.
            BigDecimal::class -> mutableMapOf(typeNumber, formatDouble)
            BigInteger::class -> mutableMapOf(typeInteger, formatInt64)

            // URL and URI.
            io.ktor.http.Url::class -> mapOf("type" to "string", "format" to "uri")
            java.net.URL::class -> mapOf("type" to "string", "format" to "uri")
            java.net.URI::class -> mapOf("type" to "string", "format" to "uri")

            // Time Zone and Locale Types
            java.time.ZoneId::class -> mapOf("type" to "string", "format" to "zone-id")
            java.time.ZoneOffset::class -> mapOf("type" to "string", "format" to "zone-offset")
            java.util.TimeZone::class -> mapOf("type" to "string", "format" to "timezone")
            java.util.Locale::class -> mapOf("type" to "string", "format" to "locale")
            java.util.Currency::class -> mapOf("type" to "string", "format" to "currency")
            java.nio.charset.Charset::class -> mapOf("type" to "string", "format" to "charset")

            // Regex and Patterns.
            // Currently treated as strings as we don't have the regex expressions when parsing.
            // A possible enhancement could be to check for annotations like @Pattern.
            Regex::class -> mapOf("type" to "string")
            java.util.regex.Pattern::class -> mapOf("type" to "string")
            java.net.URL::class -> mutableMapOf(typeString, formatUri)

            else -> null // Return null if it's not a primitive type.
        }
    }

    /**
     * Gets the serialized class name by handling serializer annotations,
     * or returns the class simple name if not annotated.
     *
     * @param kClass The Kotlin class to check for annotations.
     * @return The serialized class name or simple name if not annotated.
     */
    private fun getSerializedClassName(kClass: KClass<*>): String {
        return getSerializedName(annotatedElement = kClass, defaultName = kClass.safeName())
    }

    /**
     * Gets the serialized property name by handling serializer annotations,
     * or returns the property name as is if not annotated.
     */
    private fun getSerializedPropertyName(property: KProperty1<*, *>): String {
        return getSerializedName(annotatedElement = property, defaultName = property.name)
    }

    /**
     * Gets the serialized name based on either Kotlinx or Jackson annotations.
     * Falls back to the provided default name if neither annotation is present.
     *
     * @param annotatedElement The element (class or property) to check for annotations.
     * @param defaultName The name to return if no relevant annotation is found.
     * @return The serialized name based on annotations or the default name.
     */
    private fun getSerializedName(annotatedElement: Any, defaultName: String): String {
        // List of pairs containing annotation lookup functions and the way to extract the relevant value.
        val annotationCheckers: Set<(Any) -> String?> = setOf(
            { element -> (element as? KClass<*>)?.findAnnotation<KotlinxSerialName>()?.value },
            { element -> (element as? KProperty1<*, *>)?.findAnnotation<KotlinxSerialName>()?.value },
            { element -> (element as? KClass<*>)?.findAnnotation<JacksonJsonTypeName>()?.value },
            { element -> (element as? KProperty1<*, *>)?.findAnnotation<JacksonJsonProperty>()?.value }
        )

        // Iterate over the annotation checkers to find the first non-blank name.
        annotationCheckers.forEach { checker ->
            checker(annotatedElement)?.let { serialName ->
                if (serialName.isNotBlank()) {
                    return serialName
                }
            }
        }

        // Fallback to the provided default name.
        return defaultName
    }

    /**
     * Handles property annotations like `@SerialName`, `@Transient`, and adds relevant metadata.
     */
    private fun handlePropertyAnnotations(property: KProperty1<*, *>, serializedName: String): Map<String, Any>? {
        val metadata: MutableMap<String, Any> = mutableMapOf()

        // Handle Transient annotations.
        if (property.findAnnotation<KotlinxTransient>() != null || property.findAnnotation<JacksonJsonIgnore>() != null) {
            metadata["transient"] = true
        }

        // Handle serialized name changes.
        if (serializedName != property.name) {
            metadata["originalName"] = property.name
        }

        return if (metadata.isEmpty()) null else metadata
    }

    /**
     * Extension function to safely get a class name, using qualifiedName as a fallback.
     */
    private fun KClass<*>.safeName(): String {
        return this.simpleName ?: this.qualifiedName?.replace(oldChar = '.', newChar = '_') ?: "UnknownClass"
    }

    /**
     * Converts an object to a schema reference.
     */
    private fun buildDefinitionReference(name: String): MutableMap<String, Any> {
        return mutableMapOf("\$ref" to "#/components/schemas/$name")
    }

    private val typeObject: Pair<String, String> = Pair(SpecKey.TYPE(), SpecType.OBJECT())
    private val typeArray: Pair<String, String> = Pair(SpecKey.TYPE(), SpecType.ARRAY())
    private val typeInteger: Pair<String, String> = Pair(SpecKey.TYPE(), SpecType.INTEGER())
    private val typeNumber: Pair<String, String> = Pair(SpecKey.TYPE(), SpecType.NUMBER())
    private val typeString: Pair<String, String> = Pair(SpecKey.TYPE(), SpecType.STRING())
    private val typeBoolean: Pair<String, String> = Pair(SpecKey.TYPE(), SpecType.BOOLEAN())

    private val formatInt32: Pair<String, String> = Pair(SpecKey.FORMAT(), SpecFormat.INT32())
    private val formatInt64: Pair<String, String> = Pair(SpecKey.FORMAT(), SpecFormat.INT64())
    private val formatFloat: Pair<String, String> = Pair(SpecKey.FORMAT(), SpecFormat.FLOAT())
    private val formatDouble: Pair<String, String> = Pair(SpecKey.FORMAT(), SpecFormat.DOUBLE())

    private val formatUuid: Pair<String, String> = Pair(SpecKey.FORMAT(), SpecFormat.UUID())

    private val formatDate: Pair<String, String> = Pair(SpecKey.FORMAT(), SpecFormat.DATE())
    private val formatDateTime: Pair<String, String> = Pair(SpecKey.FORMAT(), SpecFormat.DATETIME())
    private val formatTime: Pair<String, String> = Pair(SpecKey.FORMAT(), SpecFormat.TIME())

    private val formatUri: Pair<String, String> = Pair(SpecKey.FORMAT(), SpecFormat.URI())

    private val formatByte: Pair<String, String> = Pair(SpecKey.FORMAT(), SpecFormat.BYTE())
    private val formatChar: Pair<String, Pair<String, Short>> = Pair(SpecKey.FORMAT(), "maxLength" to 1)
}
