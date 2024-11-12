/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.parameters.delegate

import io.github.perracodex.kopapi.dsl.markers.KopapiDsl
import io.github.perracodex.kopapi.dsl.parameters.builders.ParametersBuilder

/**
 * Handles the registration of parameters.
 */
@KopapiDsl
public interface IParameterConfigurable {
    /**
     * Adds a collection of parameters defined within a `parameters { ... }` block.
     *
     * The `parameters` block serves only as organizational syntactic sugar.
     * Parameters can be defined directly without needing to use the `parameters` block.
     *
     * #### Usage
     * ```
     * parameters {
     *     pathParameter<Uuid>("id") { description = "The unique identifier." }
     *     queryParameter<String>("search") { description = "Search term." }
     * }
     * ```
     *
     * @param builder The builder used to configure the parameters.
     */
    public fun parameters(builder: ParametersBuilder.() -> Unit)
}
