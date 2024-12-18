/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.dsl.path.element

import io.github.perracodex.kopapi.dsl.operation.element.ApiParameter
import io.github.perracodex.kopapi.dsl.plugin.element.ApiServerConfig

/**
 * Represents a path item in the OpenAPI schema.
 *
 * @property path The relative path to the endpoint, starting with a forward slash (/).
 * @property summary Optional short summary of the path's purpose.
 * @property description Optional detailed explanation of the path.
 * @property servers Optional set of server configurations specific to this path.
 * @property parameters Optional set of parameters applicable to all API Operations within this path.
 * @property errors A set of errors encountered during the construction process.
 */
internal data class ApiPath(
    val path: String,
    val summary: String?,
    val description: String?,
    val servers: Set<ApiServerConfig>?,
    val parameters: Set<ApiParameter>?,
    val errors: Set<String>?
)
