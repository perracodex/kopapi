/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.path

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiServerConfig

/**
 * Represents a path item in the OpenAPI schema.
 *
 * @property path The relative path to the endpoint, starting with a forward slash (/).
 * @property summary An optional short summary of the path's purpose.
 * @property description An optional detailed explanation of the path.
 * @property servers An optional list of server configurations specific to this path.
 */
internal data class ApiPath(
    @JsonProperty("path") val path: String,
    @JsonProperty("summary") val summary: String?,
    @JsonProperty("description") val description: String?,
    @JsonProperty("servers") val servers: Set<ApiServerConfig>?
)
