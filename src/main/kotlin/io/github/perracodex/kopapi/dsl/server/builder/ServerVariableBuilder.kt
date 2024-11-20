/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.dsl.server.builder

import io.github.perracodex.kopapi.dsl.marker.KopapiDsl
import io.github.perracodex.kopapi.dsl.plugin.element.ApiServerConfig
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.util.orNull
import io.github.perracodex.kopapi.util.string.MultilineString
import io.github.perracodex.kopapi.util.trimOrNull

/**
 * Builder for server variables.
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
 * @property defaultValue The default value of the server variable. If choices are defined, this value must be one of them.
 * @property choices Optional [Set] of possible values of the server variable.
 * @property description Optional description of the server variable.
 *
 * @see [ServerConfigBuilder]
 * @see [ServerBuilder]
 */
@KopapiDsl
public class ServerVariableBuilder internal constructor(
    public val defaultValue: String
) {
    public var choices: Set<String> = linkedSetOf()
    public var description: String by MultilineString()

    /**
     * Builds the final immutable [ApiServerConfig.Variable].
     */
    internal fun build(): ApiServerConfig.Variable {
        if (defaultValue.isEmpty()) {
            throw KopapiException("The server variable default value must be defined.")
        }
        if (choices.isNotEmpty() && defaultValue !in choices) {
            throw KopapiException(
                "The server variable default value '$defaultValue' must be one of the choices in '$choices'."
            )
        }

        return ApiServerConfig.Variable(
            defaultValue = defaultValue.trim(),
            choices = choices.map { it.trim() }
                .filter { it.isNotEmpty() }
                .orNull()?.toSortedSet(),
            description = description.trimOrNull()
        )
    }
}
