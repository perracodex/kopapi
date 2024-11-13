/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.view

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.perracodex.kopapi.schema.SchemaRegistry
import io.github.perracodex.kopapi.serialization.SerializationUtils
import io.github.perracodex.kopapi.type.OpenApiFormat
import io.github.perracodex.kopapi.view.annotation.DebugViewApi
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Utility class for producing structured debug sections for API operations and type schemas.
 */
@DebugViewApi
internal class DebugViewUtils {
    private val apiOperationJson: Set<String> = SchemaRegistry.getDebugSection(section = SchemaRegistry.Section.API_OPERATION)
    private val typeSchemasJson: Set<String> = SchemaRegistry.getDebugSection(section = SchemaRegistry.Section.TYPE_SCHEMAS)
    private val openApiYaml: String = SchemaRegistry.getOpenApiSchema(format = OpenApiFormat.YAML, cacheAllFormats = true)
    private val openApiJson: String = SchemaRegistry.getOpenApiSchema(format = OpenApiFormat.JSON, cacheAllFormats = true)
    private val serializationUtils = SerializationUtils()

    /**
     * Extracts API operations and type schemas into structured sections.
     *
     * @return A DebugInfo object containing structured debug sections for API operations and type schemas.
     */
    suspend fun extractSections(): DebugInfo = coroutineScope {
        val operationsOutputTask: Deferred<Map<String, DebugInfo.Section>> = async {
            extractApiOperationSections(apiOperationJson)
        }
        val schemasOutputTask: Deferred<Map<String, DebugInfo.Section>> = async {
            extractTypeSchemaSections(typeSchemasJson)
        }

        val allApiOperationsYamlSectionTask: Deferred<String> = async {
            extractSection(
                mapper = serializationUtils.openApiYamlMapper,
                rootNodeStr = openApiYaml,
                rootKeys = listOf("paths"),
                wrapKeys = emptyList() // Do not wrap under "paths"
            )
        }
        val allApiOperationsJsonSectionTask: Deferred<String> = async {
            extractSection(
                mapper = serializationUtils.openApiJsonMapper,
                rootNodeStr = openApiJson,
                rootKeys = listOf("paths"),
                wrapKeys = emptyList() // Do not wrap under "paths"
            )
        }
        val allTypeSchemasYamlSectionTask: Deferred<String> = async {
            extractSection(
                mapper = serializationUtils.openApiYamlMapper,
                rootNodeStr = openApiYaml,
                rootKeys = listOf("components", "schemas"),
                wrapKeys = emptyList() // Do not wrap under "schemas"
            )
        }
        val allTypeSchemasJsonSectionTask: Deferred<String> = async {
            extractSection(
                mapper = serializationUtils.openApiJsonMapper,
                rootNodeStr = openApiJson,
                rootKeys = listOf("components", "schemas"),
                wrapKeys = emptyList() // Do not wrap under "schemas"
            )
        }

        val operationsOutput: Map<String, DebugInfo.Section> = operationsOutputTask.await()
        val schemasOutput: Map<String, DebugInfo.Section> = schemasOutputTask.await()
        val allApiOperationsYamlSection: String = allApiOperationsYamlSectionTask.await()
        val allApiOperationsJsonSection: String = allApiOperationsJsonSectionTask.await()
        val allTypeSchemasYamlSection: String = allTypeSchemasYamlSectionTask.await()
        val allTypeSchemasJsonSection: String = allTypeSchemasJsonSectionTask.await()

        return@coroutineScope DebugInfo(
            apiOperationSections = operationsOutput,
            typeSchemaSections = schemasOutput,
            allApiOperationsYamlSection = allApiOperationsYamlSection,
            allApiOperationsJsonSection = allApiOperationsJsonSection,
            allTypeSchemasYamlSection = allTypeSchemasYamlSection,
            allTypeSchemasJsonSection = allTypeSchemasJsonSection
        )
    }

    /**
     * Generic method to extract a section from the OpenAPI document.
     *
     * @param mapper The ObjectMapper to use (JSON or YAML).
     * @param rootNodeStr The OpenAPI document as a string.
     * @param rootKeys The keys to navigate to the desired section.
     * @param extractKeys The keys to extract the specific node (optional).
     * @param wrapKeys The list of keys to wrap the extracted node (optional).
     * @return The extracted section as a string, or an empty string if not found.
     */
    private fun extractSection(
        mapper: ObjectMapper,
        rootNodeStr: String,
        rootKeys: List<String>,
        extractKeys: List<String> = emptyList(),
        wrapKeys: List<String> = emptyList()
    ): String {
        val rootNode: JsonNode? = mapper.readTree(rootNodeStr)
        var currentNode: JsonNode? = rootNode

        // Navigate to the root keys.
        for (key in rootKeys) {
            currentNode = currentNode?.get(key)
            if (currentNode == null) return ""
        }

        // Navigate to the extract keys.
        for (key in extractKeys) {
            currentNode = currentNode?.get(key)
            if (currentNode == null) return ""
        }

        // Wrap the node under the specified keys.
        val resultNode: JsonNode? = if (wrapKeys.isNotEmpty() && currentNode != null) {
            var wrappedNode: JsonNode? = currentNode
            for (key in wrapKeys.reversed()) {
                wrappedNode = mapper.createObjectNode().apply {
                    set<JsonNode>(key, wrappedNode)
                }
            }
            wrappedNode
        } else {
            currentNode ?: return ""
        }

        return mapper.writeValueAsString(resultNode)
    }

    /**
     * Extracts and structures API operation sections from raw JSON.
     *
     * @param apiOperations Set of raw JSON strings for API operations.
     * @return A map of composite keys to Section for the extracted API operations.
     */
    private suspend fun extractApiOperationSections(
        apiOperations: Set<String>
    ): Map<String, DebugInfo.Section> = coroutineScope {
        val tasks: List<Deferred<Pair<String, DebugInfo.Section>>> = apiOperations.map { rawSection ->
            async {
                val jsonObject: JsonNode = serializationUtils.openApiJsonMapper.readTree(rawSection)
                val path: String = jsonObject["path"]?.asText() ?: ""
                val method: String = jsonObject["method"]?.asText()?.lowercase() ?: ""
                val operationId: String = jsonObject["operationId"]?.asText(null) ?: ""

                val yamlSectionDeferred: Deferred<String> = async {
                    extractSection(
                        mapper = serializationUtils.openApiYamlMapper,
                        rootNodeStr = openApiYaml,
                        rootKeys = listOf("paths"),
                        extractKeys = listOf(path, method),
                        wrapKeys = listOf(path, method) // Wrap under path and method.
                    )
                }
                val jsonSectionDeferred: Deferred<String> = async {
                    extractSection(
                        mapper = serializationUtils.openApiJsonMapper,
                        rootNodeStr = openApiJson,
                        rootKeys = listOf("paths"),
                        extractKeys = listOf(path, method),
                        wrapKeys = listOf(path, method) // Wrap under path and method.
                    )
                }

                val yamlSection: String = yamlSectionDeferred.await()
                val jsonSection: String = jsonSectionDeferred.await()

                val compositeKey: String = listOf(path, method, operationId)
                    .filter { it.isNotEmpty() }
                    .joinToString(separator = " → ")

                return@async compositeKey to DebugInfo.Section(
                    rawSection = rawSection,
                    yamlSection = yamlSection,
                    jsonSection = jsonSection
                )
            }
        }

        return@coroutineScope tasks.awaitAll().toMap()
    }

    /**
     * Extracts and structures type schema sections from raw JSON.
     *
     * @param typeSchemas Set of raw JSON strings for type schemas.
     * @return A map of composite keys to Section for the extracted type schemas.
     */
    private suspend fun extractTypeSchemaSections(
        typeSchemas: Set<String>
    ): Map<String, DebugInfo.Section> = coroutineScope {
        val tasks: List<Deferred<Pair<String, DebugInfo.Section>>> = typeSchemas.map { rawSection ->
            async {
                val jsonObject: JsonNode = serializationUtils.openApiJsonMapper.readTree(rawSection)
                val name: String = jsonObject["name"]?.asText() ?: ""
                val type: String = jsonObject["type"]?.asText() ?: ""

                val yamlSectionDeferred: Deferred<String> = async {
                    extractSection(
                        mapper = serializationUtils.openApiYamlMapper,
                        rootNodeStr = openApiYaml,
                        rootKeys = listOf("components", "schemas"),
                        extractKeys = listOf(name),
                        wrapKeys = listOf(name) // Wrap under the schema name.
                    )
                }
                val jsonSectionDeferred: Deferred<String> = async {
                    extractSection(
                        mapper = serializationUtils.openApiJsonMapper,
                        rootNodeStr = openApiJson,
                        rootKeys = listOf("components", "schemas"),
                        extractKeys = listOf(name),
                        wrapKeys = listOf(name) // Wrap under the schema name.
                    )
                }

                val yamlSection: String = yamlSectionDeferred.await()
                val jsonSection: String = jsonSectionDeferred.await()

                val compositeKey = "$name → $type"

                return@async compositeKey to DebugInfo.Section(
                    rawSection = rawSection,
                    yamlSection = yamlSection,
                    jsonSection = jsonSection
                )
            }
        }

        return@coroutineScope tasks.awaitAll().toMap()
    }
}
