/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.common.example.configurables

import io.github.perracodex.kopapi.dsl.common.example.builders.ExampleBuilder
import io.github.perracodex.kopapi.dsl.markers.KopapiDsl

/**
 * Handles the registration of operation examples.
 */
@KopapiDsl
public interface IExampleConfigurable {
    /**
     * Adds an example to the operation.
     *
     * #### Sample Usage
     * ```
     * examples {
     *     example(name = "BasicEmployee") {
     *         summary = "An example of a basic employee"
     *         description = "A simple example showing essential employee data."
     *         value = mapOf("id" to "1", "name" to "John", "role" to "Dev")
     *     }
     *     example(name = "ManagerExample") {
     *         summary = "An example of a manager"
     *         description = "Shows an employee who holds a QA position."
     *         value = mapOf("id" to "2", "name" to "Smith", "role" to "QA")
     *     }
     * }
     * ```
     *
     * @param name Optional name of the example. If not provided, a unique name is generated.
     * @param init A lambda to configure the example's properties.
     */
    public fun example(name: String? = null, init: ExampleBuilder.() -> Unit)

    /**
     * Adds a collection of examples defined within a `examples { ... }` block.
     *
     * The `examples` block serves only as organizational syntactic sugar.
     * Examples can be defined directly without needing to use the `examples` block.
     *
     * #### Sample Usage
     * ```
     * examples {
     *     example(name = "BasicEmployee") {
     *         summary = "An example of a basic employee"
     *         description = "A simple example showing essential employee data."
     *         value = mapOf("id" to "1", "name" to "John", "role" to "Dev")
     *     }
     *     example(name = "ManagerExample") {
     *         summary = "An example of a manager"
     *         description = "Shows an employee who holds a QA position."
     *         value = mapOf("id" to "2", "name" to "Smith", "role" to "QA")
     *     }
     * }
     * ```
     *
     * @see [ExampleBuilder.example]
     */
    public fun examples(init: IExampleConfigurable.() -> Unit)
}
