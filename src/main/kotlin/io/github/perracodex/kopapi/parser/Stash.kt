package io.github.perracodex.kopapi.parser

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// generateJsonSchema(T::class)

// Function to generate JSON Schema for a given Kotlin Class
public fun <T : Any> generateJsonSchema(klass: KClass<T>): JsonObject {
    val properties = klass.memberProperties.map { prop ->
        val type = prop.returnType.classifier as KClass<*>
        val isNullable = prop.returnType.isMarkedNullable
        prop.name to when {
            type.java.isEnum -> generateEnumSchema(type)
            type.isData -> generateJsonSchema(type) // Recursive call for nested custom types
            type.isSubclassOf(Collection::class) -> generateCollectionSchema(prop)
            else -> JsonObject(mapOf("type" to JsonPrimitive(mapKotlinTypeToJsonType(type, isNullable))))
        }
    }.toMap()

    return JsonObject(
        mapOf(
            "type" to JsonPrimitive("object"),
            "properties" to JsonObject(properties)
        )
    )
}

// Generate schema for collections specifying item types
private fun generateCollectionSchema(prop: KProperty1<*, *>): JsonObject {
    val elementType = (prop.returnType.arguments.firstOrNull()?.type?.classifier as? KClass<*>) ?: Any::class
    return JsonObject(
        mapOf(
            "type" to JsonPrimitive("array"),
            "items" to JsonObject(mapOf("type" to JsonPrimitive(mapKotlinTypeToJsonType(elementType, prop.returnType.isMarkedNullable))))
        )
    )
}

// Map Kotlin types to JSON types
@OptIn(ExperimentalUuidApi::class)
private fun mapKotlinTypeToJsonType(kClass: KClass<*>, isNullable: Boolean): String {
    val baseType = when (kClass) {
        String::class, Uuid::class -> "string"
        Int::class, Long::class, Short::class -> "integer"
        Float::class, Double::class -> "number"
        Boolean::class -> "boolean"
        List::class, Set::class, Collection::class -> "array"
        Map::class -> "object"
        Uuid::class -> "string"
        LocalDate::class -> "string"
        LocalTime::class -> "string"
        LocalDateTime::class -> "string"
        Instant::class -> "string"
        else -> "object"
    }
    return if (isNullable) "null, $baseType" else baseType
}

private fun generateEnumSchema(klass: KClass<*>): JsonObject {
    val enumValues = klass.java.enumConstants.map { JsonPrimitive(it.toString()) } // Correct transformation
    return JsonObject(
        mapOf(
            "type" to JsonPrimitive("string"),
            "enum" to JsonArray(enumValues)  // Use JsonArray for a list of JsonPrimitives
        )
    )
}
//
//private companion object {
//    val INT = TypeDefinition(
//        type = "number",
//        format = "int32"
//    )
//
//    val LONG = TypeDefinition(
//        type = "number",
//        format = "int64"
//    )
//
//    val DOUBLE = TypeDefinition(
//        type = "number",
//        format = "double"
//    )
//
//    val FLOAT = TypeDefinition(
//        type = "number",
//        format = "float"
//    )
//
//    val STRING = TypeDefinition(
//        type = "string"
//    )
//
//    val UUID = TypeDefinition(
//        type = "string",
//        format = "uuid"
//    )
//
//    val BOOLEAN = TypeDefinition(
//        type = "boolean"
//    )
//}
