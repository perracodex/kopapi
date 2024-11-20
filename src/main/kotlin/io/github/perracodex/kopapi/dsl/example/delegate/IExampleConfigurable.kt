/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.dsl.example.delegate

import io.github.perracodex.kopapi.dsl.example.builder.ExampleBuilder
import io.github.perracodex.kopapi.dsl.marker.KopapiDsl

/**
 * Handles the registration of operation examples.
 */
@KopapiDsl
public interface IExampleConfigurable {
    /**
     * Adds an example to the operation.
     *
     * #### Usage
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
     * @receiver [ExampleBuilder] The builder used to configure the example.
     *
     * @param name Optional name of the example. If omitted, a unique name is auto-generated.
     */
    public fun example(name: String? = null, builder: ExampleBuilder.() -> Unit)

    /**
     * Adds a collection of examples defined within a `examples { ... }` block.
     *
     * The `examples` block serves only as organizational syntactic sugar.
     * Examples can be defined directly without needing to use the `examples` block.
     *
     * Example names are optional, when omitted, unique names are auto-generated.
     *
     * #### Usage
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
     * @receiver [IExampleConfigurable] The builder used to configure the examples.
     *
     * @see [ExampleBuilder.example]
     */
    public fun examples(builder: IExampleConfigurable.() -> Unit)
}
