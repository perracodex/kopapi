/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.plugin.builders.server

import io.github.perracodex.kopapi.dsl.markers.ConfigurationDsl
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiServerConfig
import io.github.perracodex.kopapi.dsl.plugin.elements.ApiServerVariable
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.utils.io.*
import kotlin.collections.set

/**
 * Builder for configuring a server.
 *
 * @property urlString The URL of the server.
 * @property description A description of the server.
 *
 * @see [ServerVariableBuilder]
 * @see [ServerBuilder]
 */
@KtorDsl
@ConfigurationDsl
public class ServerConfigBuilder(
    private val urlString: String
) {
    public var description: String by MultilineString()

    /** Holds constructed server variables. */
    private val variables: MutableMap<String, ApiServerVariable> = mutableMapOf()

    /**
     * Adds a server variable.
     *
     * ### Sample Usage
     * ```
     * variable(name = "environment", defaultValue = "production") {
     *      choices = setOf("production", "staging", "development")
     *      description = "Specifies the environment (production, etc.)"
     * }
     * ```
     * Multiple descriptions can be defined to construct a final multiline description.
     *
     * @param name The name of the variable.
     * @param defaultValue The default value of the variable.
     * @param configure The configuration block for the variable.
     *
     * @see [ServerVariableBuilder]
     * @see [ServerBuilder]
     * @see [ServerConfigBuilder]
     */
    public fun variable(
        name: String,
        defaultValue: String,
        configure: ServerVariableBuilder.() -> Unit
    ) {
        if (name.isBlank()) {
            throw KopapiException("Server variable name cannot be blank.")
        }
        if (defaultValue.isBlank()) {
            throw KopapiException("Server variable default value cannot be blank.")
        }
        variables[name.trim()] = ServerVariableBuilder(
            defaultValue = defaultValue.trim()
        ).apply(configure).build()
    }

    /**
     * Builds the final immutable [ApiServerConfig].
     */
    internal fun build(): ApiServerConfig = ApiServerConfig(
        url = urlString.trim(),
        description = description.trimOrNull(),
        variables = variables.takeIf { it.isNotEmpty() }?.toMap()
    )
}
