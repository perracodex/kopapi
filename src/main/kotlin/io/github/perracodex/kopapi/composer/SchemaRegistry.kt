/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.composer

import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.github.perracodex.kopapi.composer.security.SecuritySchemeVerifier
import io.github.perracodex.kopapi.dsl.operation.elements.ApiOperation
import io.github.perracodex.kopapi.dsl.operation.elements.ApiResponse
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiConfiguration
import io.github.perracodex.kopapi.inspector.TypeSchemaProvider
import io.github.perracodex.kopapi.inspector.schema.Schema
import io.github.perracodex.kopapi.inspector.schema.SchemaConflicts
import io.github.perracodex.kopapi.inspector.schema.TypeSchema
import io.github.perracodex.kopapi.serialization.SerializationUtils
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.system.Tracer
import kotlin.collections.set
import kotlin.reflect.KType

/**
 * Singleton for registering and serving API information.
 */
@OptIn(ComposerAPI::class)
internal object SchemaRegistry {
    private val tracer = Tracer<SchemaRegistry>()

    /**
     * Represents the format of the final OpenAPI schema output.
     */
    enum class Format {
        /** JSON format. */
        JSON,

        /** YAML format. */
        YAML
    }

    /** Represents the different sections in the debug JSON output. */
    enum class Section {
        API_INFO,
        API_SERVERS,
        API_SECURITY_SCHEMES,
        API_OPERATION,
        TYPE_SCHEMAS,
        SCHEMA_CONFLICTS,
        API_CONFIGURATION
    }

    /**
     * The enabled state of the schema provider. Defaults to `true`.
     * Overridden by the plugin configuration.
     */
    private var isEnabled: Boolean = true

    /** Set of registered API Operations metadata. */
    private val apiOperation: MutableSet<ApiOperation> = mutableSetOf()

    /** Set of generated type schemas derived from registered API metadata. */
    private val typeSchemas: MutableSet<TypeSchema> = mutableSetOf()

    /** Set of detected schema conflicts during schema generation. */
    private val schemaConflicts: MutableSet<SchemaConflicts.Conflict> = mutableSetOf()

    /** Cached JSON representations for debugging, categorized by section type. */
    private val debugJsonCache: MutableMap<Section, Set<String>> = mutableMapOf()

    /** Cached OpenAPI schema representations, categorized by format. */
    private val openApiSchemaCache: MutableMap<Format, String> = mutableMapOf()

    /** Information about the API, such as title, version, and description. */
    var apiConfiguration: ApiConfiguration? = null
        private set

    /**
     * Registers the [ApiConfiguration] object for the Kopapi plugin.
     *
     * This method should be called only once during the application lifecycle.
     *
     * @param apiConfiguration The [ApiConfiguration] object representing the plugin configuration.
     */
    fun registerApiConfiguration(apiConfiguration: ApiConfiguration) {
        synchronized(this) {
            if (SchemaRegistry.apiConfiguration != null) {
                throw KopapiException("API Configuration has already been registered.")
            }
            isEnabled = apiConfiguration.isEnabled
            if (isEnabled) {
                SchemaRegistry.apiConfiguration = apiConfiguration
                SecuritySchemeVerifier.assert(
                    global = apiConfiguration.apiSecuritySchemes,
                    apiOperations = apiOperation
                )
            } else {
                clear()
            }
        }
    }

    /**
     * Registers the [ApiOperation] for a concrete API endpoint.
     *
     * @param operation The [ApiOperation] object representing the metadata of the API endpoint.
     */
    fun registerApiOperation(operation: ApiOperation) {
        synchronized(apiOperation) {
            if (isEnabled) {
                apiOperation.add(operation)
                SecuritySchemeVerifier.assert(
                    global = apiConfiguration?.apiSecuritySchemes,
                    apiOperations = apiOperation
                )
            }
        }
    }

    /**
     * Clears all cached data.
     * This method should be called if the plugin is disabled.
     *
     * `Routes.api` definitions can be registered before or after the plugin is installed,
     * so if the plugin is disabled, we need to clear all cached data after the application
     * starts to free up resources.
     */
    fun clear() {
        apiOperation.clear()
        typeSchemas.clear()
        schemaConflicts.clear()
        debugJsonCache.clear()
        openApiSchemaCache.clear()
        apiConfiguration = null
    }

    /**
     * Processes and collects the [TypeSchema] objects from the registered [ApiOperation] objects.
     */
    private fun processTypeSchemas() {
        if (typeSchemas.isNotEmpty()) {
            return
        }

        val inspector = TypeSchemaProvider()

        apiOperation.forEach { metadata ->
            // Inspect each parameter type.
            metadata.parameters?.forEach { parameter ->
                inspectType(inspector = inspector, type = parameter.type)
            }

            // Inspect the request body type.
            metadata.requestBody?.let { requestBody ->
                inspectType(inspector = inspector, type = requestBody.type)
            }

            // Inspect each response type.
            metadata.responses?.forEach { response ->
                val apiResponse: ApiResponse = response.value
                apiResponse.type?.let { type ->
                    apiResponse.content?.let { content ->
                        content.values.forEach { schemaReference ->
                            val typeSchema: TypeSchema? = inspectType(inspector = inspector, type = type)
                            if (typeSchema?.schema is Schema.Reference) {
                                schemaReference.schema.ref = (typeSchema.schema as Schema.Reference).ref
                            }
                        }
                    }
                }
            }
        }

        // Collect and store sorted schemas.
        inspector.getTypeSchemas().sortedWith(
            compareBy { it.name }
        ).forEach { schema ->
            typeSchemas.add(schema)
        }

        // Collect and store sorted schema conflicts.
        inspector.getConflicts().sortedWith(
            compareBy { it.name }
        ).forEach { conflict ->
            schemaConflicts.add(conflict)
        }
    }

    /**
     * Inspects a type using the provided [TypeSchemaProvider] if it's not of type [Unit].
     *
     * @param inspector The [TypeSchemaProvider] instance used for inspection.
     * @param type The [KType] to inspect.
     * @return The [TypeSchema] object representing the inspected type, or `null` if the type is [Unit].
     */
    private fun inspectType(inspector: TypeSchemaProvider, type: KType): TypeSchema? {
        if (type.classifier != Unit::class) {
            return inspector.inspect(kType = type)
        }
        return null
    }

    /**
     * Retrieves the serialized JSON data for a specific section in raw unformatted format.
     *
     * @param section The [Section] indicating the category of the object.
     * @return A set of JSON strings representing the data for the specified section.
     */
    fun getDebugSection(section: Section): Set<String> {
        return when (section) {
            Section.API_CONFIGURATION -> getConfigurationSectionJson(
                instance = apiConfiguration,
                section = Section.API_CONFIGURATION
            )

            Section.API_INFO -> getConfigurationSectionJson(
                instance = apiConfiguration?.apiInfo,
                section = Section.API_INFO
            )

            Section.API_SERVERS -> getConfigurationSectionJson(
                instance = apiConfiguration?.apiServers,
                section = Section.API_SERVERS
            )

            Section.API_SECURITY_SCHEMES -> getConfigurationSectionJson(
                instance = apiConfiguration?.apiSecuritySchemes,
                section = Section.API_SECURITY_SCHEMES
            )

            Section.API_OPERATION -> {
                getOrGenerateDebugJson(
                    instance = apiOperation,
                    section = Section.API_OPERATION
                )
            }

            Section.TYPE_SCHEMAS -> {
                getOrGenerateDebugJson(
                    instance = typeSchemas,
                    section = Section.TYPE_SCHEMAS
                )
            }

            Section.SCHEMA_CONFLICTS -> {
                getOrGenerateDebugJson(
                    instance = schemaConflicts,
                    section = Section.SCHEMA_CONFLICTS
                )
            }
        }
    }

    /**
     * Helper method to handle [ApiConfiguration] sections serialization.
     *
     * @param T The type of the instance to serialize.
     * @param instance The instance to serialize.
     * @param section The [Section] to process.
     * @return A set of JSON strings representing the serialized data for the section.
     */
    private fun <T : Any> getConfigurationSectionJson(instance: T?, section: Section): Set<String> {
        return instance?.let {
            getOrGenerateDebugJson(instance = it, section = section)
        } ?: run {
            tracer.warning("No API configuration found.")
            emptySet()
        }
    }

    /**
     * Serializes an object into a JSON string, utilizing caching for efficiency.
     *
     * @param T The type of the object to serialize.
     * @param instance The object to serialize.
     * @param section The [Section] indicating the category of the object.
     * @return A list containing a single JSON string representing the serialized object.
     */
    private fun <T : Any> getOrGenerateDebugJson(instance: T?, section: Section): Set<String> {
        if (instance == null) {
            return emptySet()
        }

        return debugJsonCache[section] ?: run {
            val json: String = SerializationUtils.toRawJson(instance)
            debugJsonCache[section] = setOf(json)
            setOf(json)
        }
    }

    /**
     * Serializes a set of objects into a list of JSON strings, utilizing caching for efficiency.
     *
     * @param T The type of objects to serialize.
     * @param instance The set of objects to serialize.
     * @param section The [Section] indicating the category of objects.
     * @return A list of JSON strings representing the serialized objects.
     */
    private fun <T : Any> getOrGenerateDebugJson(instance: Set<T>, section: Section): Set<String> {
        processTypeSchemas()

        return debugJsonCache[section] ?: run {
            instance
                .map { SerializationUtils.toRawJson(instance = it) }
                .toSortedSet()
                .also { sortedSet ->
                    debugJsonCache[section] = sortedSet
                }
        }
    }

    /**
     * Serves the OpenAPI schema in the specified format.
     *
     * @param format The [Format] of the OpenAPI schema to serve.
     * @return The OpenAPI schema in the specified format.
     */
    fun getOpenApiSchema(format: Format): String {
        if (!isEnabled) {
            throw KopapiException("Attempted to generate OpenAPI schema while plugin is disabled.")
        }

        openApiSchemaCache.getOrDefault(key = format, defaultValue = null)?.let { schema ->
            return schema
        }

        apiConfiguration?.let { configuration ->
            processTypeSchemas()

            val schema: String = SchemaComposer(
                apiConfiguration = configuration,
                apiOperations = apiOperation,
                schemaConflicts = schemaConflicts
            ).compose(format = format)

            openApiSchemaCache[format] = schema
            return schema
        } ?: throw KopapiException("API Configuration not found.")
    }
}
