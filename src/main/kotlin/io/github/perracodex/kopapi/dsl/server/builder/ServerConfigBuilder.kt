/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.server.builder

import io.github.perracodex.kopapi.dsl.marker.KopapiDsl
import io.github.perracodex.kopapi.dsl.plugin.element.ApiServerConfig
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.util.orNull
import io.github.perracodex.kopapi.util.string.MultilineString
import io.github.perracodex.kopapi.util.trimOrNull
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
@KopapiDsl
public class ServerConfigBuilder internal constructor(
    private val urlString: String
) {
    public var description: String by MultilineString()

    /** Holds constructed server variables. */
    private val variables: MutableMap<String, ApiServerConfig.Variable> = mutableMapOf()

    /**
     * Adds a server variable.
     *
     * #### Usage
     * ```
     * variable(name = "environment", defaultValue = "production") {
     *      choices = setOf("production", "staging", "development")
     *      description = "Specifies the environment (production, etc.)"
     * }
     * ```
     * Multiple descriptions can be defined to construct a final multiline description.
     *
     * @receiver [ServerVariableBuilder] The builder used to configure a server variable.
     *
     * @param name The name of the variable.
     * @param defaultValue The default value of the variable.
     *
     * @see [ServerBuilder]
     */
    public fun variable(
        name: String,
        defaultValue: String,
        builder: ServerVariableBuilder.() -> Unit
    ) {
        if (name.isBlank()) {
            throw KopapiException("Server variable name cannot be blank.")
        }
        if (defaultValue.isBlank()) {
            throw KopapiException("Server variable default value cannot be blank.")
        }
        variables[name.trim()] = ServerVariableBuilder(
            defaultValue = defaultValue.trim()
        ).apply(builder).build()
    }

    /**
     * Builds the final immutable [ApiServerConfig].
     */
    internal fun build(): ApiServerConfig = ApiServerConfig(
        url = urlString.trim(),
        description = description.trimOrNull(),
        variables = variables.orNull()?.toMap()
    )
}
