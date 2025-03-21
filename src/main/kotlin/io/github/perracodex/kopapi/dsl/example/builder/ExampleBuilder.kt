/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.dsl.example.builder

import io.github.perracodex.kopapi.dsl.example.element.ApiExample
import io.github.perracodex.kopapi.dsl.example.element.ApiExamplesMap
import io.github.perracodex.kopapi.dsl.example.element.IExample
import io.github.perracodex.kopapi.dsl.marker.KopapiDsl
import io.github.perracodex.kopapi.util.sanitize
import io.github.perracodex.kopapi.util.string.MultilineString
import io.github.perracodex.kopapi.util.string.SpacedString
import io.github.perracodex.kopapi.util.trimOrNull

/**
 * Builds examples for an operation.
 *
 * @property summary A brief summary of the examples.
 * @property description A detailed description of the examples.
 * @property value The value of the example.
 */
@KopapiDsl
public class ExampleBuilder internal constructor() {
    public var summary: String by SpacedString()
    public var description: String by MultilineString()
    public var value: Any? = null

    /** Holds the named examples associated with the operation. */
    private val examples: MutableMap<String, ApiExample> = mutableMapOf()

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
     * @param name The name of the example.
     */
    public fun example(name: String, builder: ExampleBuilder.() -> Unit) {
        val exampleName: String = name.sanitize()
        if (exampleName.isNotBlank()) {
            val exampleInstance: ExampleBuilder = ExampleBuilder().apply(builder)
            examples[exampleName] = ApiExample(
                summary = exampleInstance.summary.trimOrNull(),
                description = exampleInstance.description.trimOrNull(),
                value = exampleInstance.value
            )
        }
    }

    /**
     * Builds the examples as either a named example map or a list of inline examples.
     *
     * @param existingExamples Optional existing examples to merge with.
     * @return The constructed examples wrapped in `IExample`.
     */
    internal fun build(existingExamples: IExample?): IExample {
        val combinedExamples: Map<String, ApiExample> = when {
            existingExamples is ApiExamplesMap -> existingExamples.examples + examples
            else -> examples
        }
        return ApiExamplesMap(examples = combinedExamples)
    }
}
