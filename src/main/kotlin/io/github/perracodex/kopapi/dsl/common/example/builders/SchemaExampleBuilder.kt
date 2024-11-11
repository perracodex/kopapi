/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.common.example.builders

import io.github.perracodex.kopapi.dsl.common.example.ApiExampleArray
import io.github.perracodex.kopapi.dsl.common.example.ApiInlineExample
import io.github.perracodex.kopapi.dsl.common.example.IExample
import io.github.perracodex.kopapi.dsl.markers.KopapiDsl

/**
 * Builder for inline schema examples as a list of values.
 */
@KopapiDsl
public class SchemaExampleBuilder internal constructor() {
    /** Holds inline examples for the schema as a list. */
    internal val _examples: MutableList<Any?> = mutableListOf()

    /**
     * Adds an example to the schema example list.
     *
     * #### Usage
     * ```
     * schema {
     *     examples {
     *         example("John Doe")
     *         example(mapOf("role" to "Developer", "age" to 35))
     *         example(listOf("Developer", "Project Manager"))
     *     }
     * }
     * ```
     *
     * @param value The value of the example to add.
     */
    public fun example(value: Any?) {
        _examples.add(value)
    }

    /**
     * Builds the list of inline examples.
     *
     * @param existingExamples Optional existing examples to merge with.
     * @return A merged [ApiExampleArray] containing all inline examples.
     */
    internal fun build(existingExamples: IExample?): IExample {
        val newExamplesArray = ApiExampleArray(examples = _examples.map { ApiInlineExample(value = it) }.toTypedArray())
        return if (existingExamples is ApiExampleArray) {
            ApiExampleArray(examples = existingExamples.examples + newExamplesArray.examples)
        } else {
            newExamplesArray
        }
    }
}
