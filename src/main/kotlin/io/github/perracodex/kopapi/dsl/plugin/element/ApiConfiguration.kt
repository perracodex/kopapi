/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.dsl.plugin.element

import io.github.perracodex.kopapi.dsl.operation.element.ApiSecurityScheme
import io.github.perracodex.kopapi.system.KopapiException

/**
 * Represents the final immutable configuration for the Kopapi plugin.
 *
 * @property isEnabled Whether the plugin is enabled.
 * @property host The host to provide the API schema.
 * @property onDemand Whether to enable on-demand API schema generation.
 * @property logPluginRoutes Whether to log plugin routes.
 * @property enableLogging Whether to enable overall plugin logging.
 * @property apiDocs The API documentation configuration.
 * @property debugUrl The URL access the debug endpoint.
 * @property apiInfo The API Schema information details.
 * @property apiServers The API Schema server details.
 * @property apiTags The API Schema tag details.
 * @property apiSecuritySchemes The API Schema security scheme details.
 */
internal data class ApiConfiguration(
    val isEnabled: Boolean,
    val host: String?,
    val onDemand: Boolean,
    val logPluginRoutes: Boolean,
    val enableLogging: Boolean,
    val apiDocs: ApiDocs,
    val debugUrl: String,
    val apiInfo: ApiInfo?,
    val apiServers: Set<ApiServerConfig>?,
    val apiTags: Set<ApiTag>?,
    val apiSecuritySchemes: Set<ApiSecurityScheme>?
) {
    init {
        val urls: List<String> = listOf(
            debugUrl,
            apiDocs.openApiUrl,
            apiDocs.redocUrl,
            apiDocs.swagger.url
        )
        val duplicates: Map<String, Int> = urls.groupingBy { it }.eachCount().filter { it.value > 1 }
        if (duplicates.isNotEmpty()) {
            throw KopapiException("Duplicate URLs found: ${duplicates.keys}")
        }
    }
}
