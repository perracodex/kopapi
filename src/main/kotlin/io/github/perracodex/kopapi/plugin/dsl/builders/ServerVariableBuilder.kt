/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.plugin.dsl.builders

import io.github.perracodex.kopapi.plugin.dsl.elements.ApiServerVariable
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull

/**
 * Builder for server variables.
 *
 * ### Sample Usage
 * ```
 * variable("environment") {
 *      description = "Specifies the environment (production, etc.)"
 *      defaultValue = "production"
 *      choices = setOf("production", "staging", "development")
 * }
 * ```
 * Multiple descriptions can be defined to construct a final multiline description.
 *
 * @property description A description of the server variable.
 * @property defaultValue The default value of the server variable
 * @property choices The [Set] of possible values of the server variable.
 *
 * @see [ServerConfigBuilder]
 * @see [ServerBuilder]
 */
public class ServerVariableBuilder {
    public var description: String by MultilineString()
    public var defaultValue: String = ""
    public var choices: Set<String> = linkedSetOf()

    /**
     * Builds the final immutable [ApiServerVariable].
     */
    internal fun build(): ApiServerVariable {
        require(defaultValue.isEmpty() || defaultValue in choices) {
            "The server variable default value '$defaultValue' must be one of the choices."
        }

        return ApiServerVariable(
            description = description.trimOrNull(),
            defaultValue = defaultValue.trim(),
            choices = choices.map { it.trim() }
                .filter { it.isNotEmpty() }
                .toCollection(linkedSetOf())
        )
    }
}
