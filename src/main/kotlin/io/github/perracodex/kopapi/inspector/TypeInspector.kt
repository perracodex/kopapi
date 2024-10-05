/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector

import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import io.github.perracodex.kopapi.inspector.resolver.*
import io.github.perracodex.kopapi.inspector.spec.Spec
import io.github.perracodex.kopapi.inspector.type.TypeDefinition
import io.github.perracodex.kopapi.inspector.type.TypeDefinitionWarningManager
import io.github.perracodex.kopapi.inspector.type.nativeName
import io.github.perracodex.kopapi.utils.Tracer
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.uuid.Uuid

/**
 * Inspector for various Kotlin types capable of traversing and parsing [KType] objects
 * into [TypeDefinition] objects containing the necessary information to construct OpenAPI schemas.
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
internal object TypeInspector {
    private val tracer = Tracer<TypeInspector>()

    /** Cache of [TypeDefinition] objects that have been processed. */
    private val typeDefinitionsCache: MutableSet<TypeDefinition> = mutableSetOf()

    /**
     * Retrieves the currently cached [TypeDefinition] objects.
     *
     * @return A set of [TypeDefinition] objects.
     */
    fun getTypeDefinitions(): Set<TypeDefinition> = typeDefinitionsCache

    /**
     * Parses a KType to its [TypeDefinition] representation.
     *
     * @param kType The KType to parse.
     * @return The [TypeDefinition] for the given [kType].
     */
    @OptIn(TypeInspectorAPI::class)
    fun inspect(kType: KType): TypeDefinition {
        val result: TypeDefinition = traverse(kType = kType, typeParameterMap = emptyMap())
        TypeDefinitionWarningManager.analyze(newTypeDefinition = result)
        return result
    }

    /**
     * Resets the parser by clearing all processed types and object definitions.
     */
    @OptIn(TypeInspectorAPI::class)
    fun reset() {
        typeDefinitionsCache.clear()
        TypeDefinitionWarningManager.clear()
    }

    /**
     * Traverses and resolves the given [kType], handling both simple and complex types,
     * including collections, maps, enums, and generics. Manages recursion and self-referencing types.
     *
     * Returns a [TypeDefinition] representing the structure of the [kType], considering generic parameters
     * nullable properties, and any annotations present.
     *
     * @param kType The [KType] to resolve into a [TypeDefinition].
     * @param typeParameterMap A map of type parameters' [KClassifier] to their corresponding [KType].
     * @return The resolved [TypeDefinition] for the [kType].
     */
    @TypeInspectorAPI
    fun traverse(
        kType: KType,
        typeParameterMap: Map<KClassifier, KType>
    ): TypeDefinition {
        // Resolve the type's classifier, if unavailable, log an error and return an unknown type.
        val classifier: KClassifier = kType.classifier
            ?: run {
                tracer.error("KType must have a classifier. kType=$kType. typeParameterMap=$typeParameterMap.")
                return TypeDefinition.of(
                    name = "Unknown_$kType",
                    kType = kType,
                    definition = Spec.objectType()
                )
            }

        val typeDefinition: TypeDefinition = when {
            // Handle collections (e.g., List<String>, Set<Int>).
            classifier == List::class || classifier == Set::class || isArrayType(classifier = classifier) ->
                CollectionResolver.process(kType = kType, classifier = classifier, typeParameterMap = typeParameterMap)

            // Handle maps (e.g., Map<String, Int>).
            classifier == Map::class ->
                MapResolver.process(kType = kType, typeParameterMap = typeParameterMap)

            // Handle enums.
            classifier is KClass<*> && classifier.isSubclassOf(Enum::class) ->
                EnumResolver.process(enumClass = classifier)

            // Handle generics.
            kType.arguments.isNotEmpty() ->
                GenericsResolver.process(kType = kType, kClass = classifier as KClass<*>, typeParameterMap = typeParameterMap)

            // Handle basic types and complex objects.
            // This condition must be placed last because all types are also instances of KClass.
            // If this check is placed earlier, the above branches will never be reached.
            classifier is KClass<*> ->
                ObjectResolver.process(kType = kType, kClass = classifier, typeParameterMap = typeParameterMap)

            // Fallback for unknown types. This should never be reached.
            else -> {
                tracer.error("Unexpected type. kType=$kType. classifier=$classifier. typeParameterMap=$typeParameterMap.")
                TypeDefinition.of(
                    name = "Unknown_$kType",
                    kType = kType,
                    definition = Spec.objectType()
                )
            }
        }

        return typeDefinition
    }

    /**
     * Determines whether the given [KType] is already present
     * in the [TypeDefinition] cache.
     */
    @TypeInspectorAPI
    fun isCached(kType: KType): Boolean {
        return typeDefinitionsCache.any {
            it.type == kType.nativeName()
        }
    }

    /**
     * Caches the given [TypeDefinition] object.
     */
    @TypeInspectorAPI
    fun addToCache(definition: TypeDefinition) {
        typeDefinitionsCache.add(definition)
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
    @TypeInspectorAPI
    fun replaceTypeIfNeeded(
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
    @TypeInspectorAPI
    private fun isArrayType(classifier: KClassifier): Boolean {
        return isPrimitiveArrayType(classifier = classifier)
                || (classifier as? KClass<*>)?.javaObjectType?.isArray ?: false
    }

    /**
     * Determines whether the given [KClassifier] represents a specialized primitive array type.
     *
     * Kotlin provides specialized array classes for each primitive type (e.g., [IntArray], [FloatArray]),
     * which are distinct from the generic [Array] class used for reference types.
     *
     * @param classifier The [KClassifier] of the [KType] to evaluate.
     * @return True if the [classifier] is one of Kotlin's primitive array types, otherwise False.
     */
    @TypeInspectorAPI
    fun isPrimitiveArrayType(classifier: KClassifier): Boolean {
        return classifier == IntArray::class || classifier == ByteArray::class ||
                classifier == ShortArray::class || classifier == FloatArray::class ||
                classifier == DoubleArray::class || classifier == LongArray::class ||
                classifier == CharArray::class || classifier == BooleanArray::class ||
                classifier == UIntArray::class || classifier == ULongArray::class ||
                classifier == UByteArray::class || classifier == UShortArray::class
    }

    /**
     * Constructs a mutable map representing the schema for the given primitive type.
     *
     * @param kClass The [KClass] representing the primitive type.
     * @return A mutable map representing the schema for the primitive type,
     * or null if the type is not a primitive.
     */
    @TypeInspectorAPI
    fun mapPrimitiveType(kClass: KClass<*>): MutableMap<String, Any>? {
        return when (kClass) {
            // Basic Kotlin Types.
            String::class, CharSequence::class -> Spec.string()
            Char::class -> Spec.char()
            Boolean::class -> Spec.boolean()
            Int::class -> Spec.int32()
            Long::class -> Spec.int64()
            Double::class -> Spec.double()
            Float::class -> Spec.float()
            Short::class -> Spec.int32()
            Byte::class -> Spec.int32()
            UInt::class -> Spec.int32()
            ULong::class -> Spec.int64()
            UShort::class -> Spec.int32()
            UByte::class -> Spec.int32()

            // Primitive Arrays.
            IntArray::class, ShortArray::class, UIntArray::class, UShortArray::class -> Spec.array(spec = Spec.int32())
            LongArray::class, ULongArray::class -> Spec.array(spec = Spec.int64())
            FloatArray::class -> Spec.array(spec = Spec.float())
            DoubleArray::class -> Spec.array(spec = Spec.double())
            BooleanArray::class -> Spec.array(spec = Spec.boolean())
            CharArray::class -> Spec.array(spec = Spec.char())
            ByteArray::class, UByteArray::class -> Spec.array(spec = Spec.byte())

            // UUID Types.
            Uuid::class, UUID::class -> Spec.uuid()

            // Kotlin Date/Time Types.
            kotlinx.datetime.LocalDate::class -> Spec.date()
            kotlinx.datetime.LocalDateTime::class -> Spec.dateTime()
            kotlinx.datetime.Instant::class -> Spec.dateTime()
            kotlinx.datetime.LocalTime::class -> Spec.time()

            // Java Date/Time Types.
            java.time.OffsetDateTime::class -> Spec.dateTime()
            java.time.ZonedDateTime::class -> Spec.dateTime()
            java.time.LocalTime::class -> Spec.time()
            java.time.LocalDate::class -> Spec.date()
            java.time.LocalDateTime::class -> Spec.dateTime()
            java.time.Instant::class -> Spec.dateTime()
            java.util.Date::class -> Spec.dateTime()
            java.sql.Date::class -> Spec.date()

            // Big Numbers.
            BigDecimal::class -> Spec.double()
            BigInteger::class -> Spec.int64()

            // URL and URI.
            io.ktor.http.Url::class -> Spec.uri()
            java.net.URL::class -> Spec.uri()
            java.net.URI::class -> Spec.uri()

            else -> null // Return null if it's not a primitive type.
        }
    }
}
