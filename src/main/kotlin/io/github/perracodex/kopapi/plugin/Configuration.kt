/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.plugin

import io.github.perracodex.kopapi.core.KopapiException
import io.github.perracodex.kopapi.dsl.api.elements.ApiSecurity
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiInfo
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiServerConfig

/**
 * Represents the final immutable configuration for the Kopapi plugin.
 */
internal data class Configuration(
    val isEnabled: Boolean,
    val debugUrl: String,
    val openapiJsonUrl: String,
    val openapiYamlUrl: String,
    val swaggerUrl: String,
    val apiInfo: ApiInfo?,
    val apiServers: Set<ApiServerConfig>?,
    val apiSecuritySchemes: Set<ApiSecurity>?
) {
    init {
        val urls: List<String> = listOf(debugUrl, openapiJsonUrl, openapiYamlUrl, swaggerUrl)
        val duplicates: Map<String, Int> = urls.groupingBy { it }.eachCount().filter { it.value > 1 }
        if (duplicates.isNotEmpty()) {
            throw KopapiException("Duplicate URLs found: ${duplicates.keys}")
        }
    }
}
