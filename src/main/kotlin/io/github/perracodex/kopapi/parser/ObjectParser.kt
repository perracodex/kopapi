/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */
package io.github.perracodex.kopapi.parser

import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import kotlin.reflect.*
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import com.fasterxml.jackson.annotation.JsonIgnore as JacksonJsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty as JacksonJsonProperty
import io.ktor.http.Url as KtorUrl
import kotlinx.datetime.Instant as KotlinInstant
import kotlinx.datetime.LocalDate as KotlinLocalDate
import kotlinx.datetime.LocalDateTime as KotlinLocalDateTime
import kotlinx.datetime.LocalTime as KotlinLocalTime
import kotlinx.serialization.SerialName as KotlinxSerialName
import kotlinx.serialization.Transient as KotlinxTransient
import java.net.URI as JavaURI
import java.net.URL as JavaURL
import java.nio.charset.Charset as JavaCharset
import java.time.Instant as JavaInstant
import java.time.LocalDate as JavaLocalDate
import java.time.LocalDateTime as JavaLocalDateTime
import java.time.LocalTime as JavaLocalTime
import java.time.OffsetDateTime as JavaOffsetDateTime
import java.time.ZoneId as JavaZoneId
import java.time.ZoneOffset as JavaZoneOffset
import java.time.ZonedDateTime as JavaZonedDateTime
import java.util.Currency as JavaCurrency
import java.util.Date as JavaDate
import java.util.Locale as JavaLocale
import java.util.TimeZone as JavaTimeZone
import java.util.regex.Pattern as JavaPattern

internal class ObjectParser {
    /** Track processed KTypes to handle generics uniquely. */
    private val processedTypes: MutableSet<KType> = mutableSetOf()

    /** Store component objects by name. */
    private val objectDefinitions: MutableMap<String, MutableMap<String, Any>> = mutableMapOf()

    /**
     * Main function to map an object type to OpenAPI-compliant JSON schema.
     */
    fun mapObject(kType: KType): Map<String, Any> {
        return mapObjectInternal(kType, typeParameterMap = emptyMap())
    }

    /**
     * Retrieves the object definitions (components) that have been processed.
     * These can be used in the OpenAPI components section.
     */
    fun getObjectDefinitions(): Map<String, Map<String, Any>> = objectDefinitions

    /**
     * Recursively maps a KType to its OpenAPI schema, handling recursion and complex types.
     * @param typeParameterMap A map of type parameter classifiers to actual KTypes for replacement.
     */
    private fun mapObjectInternal(
        kType: KType,
        typeParameterMap: Map<KClassifier, KType>
    ): Map<String, Any> {
        // Handle nullability at the property level.
        val baseSchema: MutableMap<String, Any> = if (kType.isMarkedNullable) {
            mapNonNullType(kType = kType, typeParameterMap = typeParameterMap)
                .toMutableMap()
                .apply { this["nullable"] = true }
        } else {
            mapNonNullType(kType = kType, typeParameterMap = typeParameterMap)
                .toMutableMap()
        }

        return baseSchema
    }

    /**
     * Maps a non-null KType to its OpenAPI schema.
     * @param typeParameterMap A map of type parameter classifiers to actual KTypes for replacement.
     */
    private fun mapNonNullType(
        kType: KType,
        typeParameterMap: Map<KClassifier, KType>
    ): Map<String, Any> {
        val classifier: KClassifier = kType.classifier
            ?: throw IllegalArgumentException("KType must have a classifier to map to OpenAPI schema.")

        return when {
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
                handleComplexOrBasicType(kClass = classifier, kType = kType, typeParameterMap = typeParameterMap)

            // Default to object for unknown or unsupported types.
            else -> unknownObject
        }
    }

    /**
     * Handles collections (List, Set) in OpenAPI schema.
     */
    private fun handleCollectionType(
        kType: KType,
        classifier: KClassifier,
        typeParameterMap: Map<KClassifier, KType>
    ): Map<String, Any> {
        // Check if the classifier is a primitive array first, such us IntArray, ByteArray, etc.
        if (isPrimitiveArrayType(classifier = classifier)) {
            return mapPrimitiveType(kClass = classifier as KClass<*>) ?: unknownObject
        } else {
            // For non-primitive arrays and collections, handle based on type arguments.
            val itemType: KType = kType.arguments.firstOrNull()?.type?.let {
                replaceTypeIfNeeded(type = it, typeParameterMap = typeParameterMap)
            } ?: return unknownObject  // Fallback if type resolution fails.

            // Map the item type to its respective schema, considering it's a regular object array or collection.
            val itemSchema: Map<String, Any> = mapObjectInternal(
                kType = itemType,
                typeParameterMap = typeParameterMap
            )

            return mapOf(
                "type" to "array",
                "items" to itemSchema
            )
        }
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
                classifier == CharArray::class || classifier == BooleanArray::class
    }

    /**
     * Handles maps (e.g., Map<String, Int>) in OpenAPI schema.
     * Ensures that the key type is String, as required by OpenAPI specifications.
     */
    private fun handleMapType(
        kType: KType,
        typeParameterMap: Map<KClassifier, KType>
    ): Map<String, Any> {
        val keyType: KType? = kType.arguments.getOrNull(index = 0)?.type?.let {
            replaceTypeIfNeeded(type = it, typeParameterMap = typeParameterMap)
        }
        val valueType: KType? = kType.arguments.getOrNull(index = 1)?.type?.let {
            replaceTypeIfNeeded(type = it, typeParameterMap = typeParameterMap)
        }

        // OpenAPI requires keys to be strings
        if (keyType == null || keyType.classifier != String::class) {
            throw IllegalArgumentException("OpenAPI only supports string keys for maps. Found key type: $keyType")
        }

        // Process the value type
        val valueSchema: Map<String, Any> = valueType?.let {
            mapObjectInternal(kType = it, typeParameterMap = typeParameterMap)
        } ?: unknownObject

        return mapOf(
            "type" to "object",
            "additionalProperties" to valueSchema
        )
    }

    /**
     * Handles enums in OpenAPI schema.
     */
    private fun handleEnumType(enumClass: KClass<*>): Map<String, Any> {
        val enumValues: List<String> = enumClass.java.enumConstants?.map {
            (it as Enum<*>).name
        } ?: emptyList()

        return mapOf(
            "type" to "string",
            "enum" to enumValues
        )
    }

    /**
     * Handles complex or basic types, such as data classes or primitive types.
     * For complex types, it adds them as a separate object in the objectDefinitions map and creates a $ref.
     */
    private fun handleComplexOrBasicType(
        kClass: KClass<*>,
        kType: KType,
        typeParameterMap: Map<KClassifier, KType>
    ): Map<String, Any> {
        // Handle primitive types
        val primitiveType: Map<String, Any>? = mapPrimitiveType(kClass = kClass)
        if (primitiveType != null) {
            return primitiveType
        }

        // Handle generics
        if (kType.arguments.isNotEmpty()) {
            val genericTypeName: String = generateGenericTypeName(kClass = kClass, kType = kType)
            if (!objectDefinitions.containsKey(genericTypeName)) {
                handleGenericType(
                    kClass = kClass,
                    kType = kType,
                    genericTypeName = genericTypeName,
                    parentTypeParameterMap = typeParameterMap
                )
            }
            return mapOf("\$ref" to "#/components/schemas/$genericTypeName")
        }

        // Prevent infinite recursion for self-referencing objects
        if (processedTypes.contains(kType)) {
            return mapOf("\$ref" to "#/components/schemas/${kClass.safeName()}")
        }

        // Process complex types like data classes
        processedTypes.add(kType)

        // Create an empty schema before processing properties to handle circular dependencies
        val propertiesMap: MutableMap<String, Any> = mutableMapOf()
        val schema: MutableMap<String, Any> = mutableMapOf("type" to "object", "properties" to propertiesMap)
        objectDefinitions[kClass.safeName()] = schema

        // Create a mutable map for properties
        val properties: MutableMap<String, Map<String, Any>> = mutableMapOf()

        // Step 1: Get the sorted properties based on the primary constructor's parameter order
        val sortedProperties: List<KProperty1<out Any, *>> = getSortedProperties(kClass = kClass)

        // Step 2: Process each property using the helper function
        sortedProperties.forEach { property ->
            val (serializedName, extendedSchema) = processProperty(
                property = property,
                typeParameterMap = typeParameterMap
            )
            properties[serializedName] = extendedSchema
        }

        // Add properties to the schema
        propertiesMap.putAll(properties)

        processedTypes.remove(kType) // Remove once done to handle different branches.

        return mapOf("\$ref" to "#/components/schemas/${kClass.safeName()}")
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

        // Sort properties based on constructor parameter order
        val sortedProperties: List<KProperty1<out Any, *>> = constructorParameters.mapNotNull { propertyMap[it] }

        // Append any additional properties not defined in the constructor
        val additionalProperties: List<KProperty1<out Any, *>> = propertyMap.keys
            .subtract(constructorParameters.toSet())
            .mapNotNull { propertyMap[it] }

        return sortedProperties + additionalProperties
    }

    /**
     * Processes a property by mapping its type, handling annotations, and preparing the schema entry.
     * @param property The property to process.
     * @param typeParameterMap A map of type parameter classifiers to actual KTypes for replacement.
     * @return A Pair containing the serialized name and the extended schema map.
     */
    private fun processProperty(
        property: KProperty1<*, *>,
        typeParameterMap: Map<KClassifier, KType>
    ): Pair<String, Map<String, Any>> {
        val serializedName: String = getSerializedPropertyName(property)
        val propertyType: KType = replaceTypeIfNeeded(type = property.returnType, typeParameterMap = typeParameterMap)
        val propertySchema: Map<String, Any> = mapObjectInternal(kType = propertyType, typeParameterMap = typeParameterMap)
        val propertyMetadata: Map<String, Any>? = handlePropertyAnnotations(property, serializedName)
        val extendedSchema: MutableMap<String, Any> = propertySchema.toMutableMap().apply {
            propertyMetadata?.let { putAll(it) }
        }
        return serializedName to extendedSchema
    }

    /**
     * Generates a unique and consistent name for a generic type, such as Page<Employee> becomes PageOfEmployee.
     * Handles multiple type parameters by joining them with 'And'.
     * Falls back to using qualified names if simpleName is not available.
     */
    private fun generateGenericTypeName(kClass: KClass<*>, kType: KType): String {
        val genericArgs: List<KClass<*>> = kType.arguments.mapNotNull { it.type?.classifier as? KClass<*> }

        // Function to safely get a class name, using qualifiedName as a fallback
        fun KClass<*>.safeName(): String {
            return this.simpleName
                ?: this.qualifiedName?.replace(oldChar = '.', newChar = '_')
                ?: "UnknownClass"
        }

        val baseName: String = kClass.safeName()
        val genericArgsNames: List<String> = genericArgs.map { it.safeName() }

        return if (genericArgsNames.size == 1) {
            "${baseName}Of${genericArgsNames.first()}"
        } else {
            "${baseName}Of${genericArgsNames.joinToString("And")}"
        }
    }

    /**
     * Handles generic types like Page<Employee> and creates a schema for them.
     */
    /**
     * Handles generic types like Page<Employee> and creates a schema for them.
     */
    private fun handleGenericType(
        kClass: KClass<*>,
        kType: KType,
        genericTypeName: String,
        parentTypeParameterMap: Map<KClassifier, KType>
    ) {
        // Add a placeholder schema early to avoid circular references
        objectDefinitions[genericTypeName] = mutableMapOf("type" to "object", "properties" to mutableMapOf<String, Any>())

        // Retrieve the type parameters from the generic class
        val typeParameters: List<KTypeParameter> = kClass.typeParameters

        // Retrieve the actual generic arguments provided
        val genericArgs: List<KType> = kType.arguments.mapNotNull { it.type }

        // Ensure the number of type parameters matches the number of generic arguments
        if (typeParameters.size != genericArgs.size) {
            throw IllegalArgumentException(
                "Type parameter count mismatch for $kClass. Expected ${typeParameters.size}, but got ${genericArgs.size}."
            )
        }

        // Create a map of type parameters to their actual types
        val currentTypeParameterMap: Map<KClassifier, KType> = typeParameters.mapIndexed { index, typeParam ->
            typeParam as KClassifier to genericArgs[index]
        }.toMap()

        // Merge parent type parameters with current type parameters
        val combinedTypeParameterMap: Map<KClassifier, KType> = parentTypeParameterMap + currentTypeParameterMap

        // Prepare a map to hold the properties for the generic instance
        val properties: MutableMap<String, Any> = mutableMapOf()

        // Retrieve sorted properties based on the primary constructor's parameter order
        val sortedProperties: List<KProperty1<out Any, *>> = getSortedProperties(kClass)

        // Iterate over each sorted property in the generic class
        sortedProperties.forEach { property ->
            val (serializedName, extendedSchema) = processProperty(property, combinedTypeParameterMap)
            properties[serializedName] = extendedSchema
        }

        // Update the placeholder schema with actual properties
        objectDefinitions[genericTypeName]?.putAll(
            mapOf(
                "type" to "object",
                "properties" to properties
            )
        )
    }

    /**
     * Replaces type parameters in a generic property type with actual types from the type parameter map.
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
     * Maps primitive Kotlin types (e.g., Int, String) to their OpenAPI schema counterparts.
     */
    @OptIn(ExperimentalUuidApi::class)
    private fun mapPrimitiveType(kClass: KClass<*>): Map<String, Any>? {
        return when (kClass) {
            // Basic Kotlin Types.
            String::class -> mapOf("type" to "string")
            Int::class -> mapOf("type" to "integer", "format" to "int32")
            Long::class -> mapOf("type" to "integer", "format" to "int64")
            Boolean::class -> mapOf("type" to "boolean")
            Double::class -> mapOf("type" to "number", "format" to "double")
            Float::class -> mapOf("type" to "number", "format" to "float")
            Short::class -> mapOf("type" to "integer", "format" to "int32")
            Byte::class -> mapOf("type" to "integer", "format" to "int32")
            Char::class -> mapOf("type" to "string", "maxLength" to 1)

            // Primitive Arrays.
            IntArray::class -> mapOf("type" to "array", "items" to mapOf("type" to "integer", "format" to "int32"))
            ByteArray::class -> mapOf("type" to "array", "items" to mapOf("type" to "string", "format" to "byte"))
            ShortArray::class -> mapOf("type" to "array", "items" to mapOf("type" to "integer", "format" to "int32"))
            FloatArray::class -> mapOf("type" to "array", "items" to mapOf("type" to "number", "format" to "float"))
            DoubleArray::class -> mapOf("type" to "array", "items" to mapOf("type" to "number", "format" to "double"))
            LongArray::class -> mapOf("type" to "array", "items" to mapOf("type" to "integer", "format" to "int64"))
            CharArray::class -> mapOf("type" to "array", "items" to mapOf("type" to "string", "maxLength" to 1))
            BooleanArray::class -> mapOf("type" to "array", "items" to mapOf("type" to "boolean"))

            // UUID Types.
            Uuid::class, UUID::class -> mapOf("type" to "string", "format" to "uuid")

            // Date and Time Types.
            KotlinLocalDate::class, JavaLocalDate::class -> mapOf("type" to "string", "format" to "date")
            KotlinLocalDateTime::class, JavaLocalDateTime::class -> mapOf("type" to "string", "format" to "date-time")
            KotlinInstant::class, JavaInstant::class -> mapOf("type" to "string", "format" to "date-time")
            KotlinLocalTime::class, JavaLocalTime::class -> mapOf("type" to "string", "format" to "time")
            JavaOffsetDateTime::class -> mapOf("type" to "string", "format" to "date-time")
            JavaZonedDateTime::class -> mapOf("type" to "string", "format" to "date-time")
            JavaDate::class -> mapOf("type" to "string", "format" to "date-time")

            // Big Numbers.
            BigDecimal::class -> mapOf("type" to "number", "format" to "double")
            BigInteger::class -> mapOf("type" to "integer", "format" to "int64")

            // URL and URI.
            JavaURL::class -> mapOf("type" to "string", "format" to "uri")
            JavaURI::class -> mapOf("type" to "string", "format" to "uri")
            KtorUrl::class -> mapOf("type" to "string", "format" to "uri")

            // Time and Zone Related Types.
            JavaZoneId::class -> mapOf(
                "type" to "string",
                "format" to "zone-id",
                "description" to "IANA time zone ID, e.g., 'Europe/Paris'"
            )

            JavaZoneOffset::class -> mapOf(
                "type" to "string",
                "format" to "zone-offset",
                "description" to "Time zone offset from UTC, e.g., '+02:00'"
            )

            JavaTimeZone::class -> mapOf(
                "type" to "string",
                "format" to "timezone",
                "description" to "IANA time zone name, e.g., 'America/New_York'"
            )

            // Locale and Internationalization.
            JavaLocale::class -> mapOf(
                "type" to "string",
                "format" to "locale",
                "description" to "Locale identifier, e.g., 'en-US'"
            )

            // Currency.
            JavaCurrency::class -> mapOf(
                "type" to "string",
                "format" to "currency",
                "description" to "Currency code, e.g., 'USD'"
            )

            // Pattern.
            JavaPattern::class -> mapOf(
                "type" to "string",
                "format" to "regex",
                "description" to "Regular expression pattern"
            )

            // Charset.
            JavaCharset::class -> mapOf(
                "type" to "string",
                "format" to "charset",
                "description" to "Character set name, e.g., 'UTF-8'"
            )

            else -> null // Return null if it's not a primitive type
        }
    }

    /**
     * Gets the serialized property name by handling `@SerialName` and Jackson's `@JsonProperty`,
     * or returns the property name if neither is annotated.
     */
    private fun getSerializedPropertyName(property: KProperty1<*, *>): String {
        // Check for Kotlinx's SerialName annotation first
        val serialNameAnnotation: KotlinxSerialName? = property.findAnnotation<KotlinxSerialName>()
        if (serialNameAnnotation != null && serialNameAnnotation.value.isNotBlank()) {
            return serialNameAnnotation.value
        }

        // Then check for Jackson's JsonProperty annotation
        val jsonPropertyAnnotation: JacksonJsonProperty? = property.findAnnotation<JacksonJsonProperty>()
        if (jsonPropertyAnnotation != null && jsonPropertyAnnotation.value.isNotBlank()) {
            return jsonPropertyAnnotation.value
        }

        // Fallback to the property's actual name.
        return property.name
    }

    /**
     * Handles property annotations like `@SerialName`, `@Transient`, and adds relevant metadata.
     */
    private fun handlePropertyAnnotations(property: KProperty1<*, *>, serializedName: String): Map<String, Any>? {
        val metadata: MutableMap<String, Any> = mutableMapOf()

        // Handle Transient annotations
        if (property.findAnnotation<KotlinxTransient>() != null || property.findAnnotation<JacksonJsonIgnore>() != null) {
            metadata["transient"] = true
        }

        // Handle serialized name changes
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

    companion object {
        /** Placeholder for unknown object types. */
       // private val unknownObject: Map<SpecKey, SpecType> = mapOf(SpecKey.TYPE to SpecType.OBJECT)
        private val unknownObject: Map<String, String> = mapOf("type" to "object")
    }
}
