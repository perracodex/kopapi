/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.dsl.example.delegate

import io.github.perracodex.kopapi.dsl.example.builder.ExampleBuilder
import io.github.perracodex.kopapi.dsl.example.element.ApiExample
import io.github.perracodex.kopapi.dsl.example.element.ApiExamplesMap
import io.github.perracodex.kopapi.dsl.example.element.IExample
import io.github.perracodex.kopapi.dsl.marker.KopapiDsl
import io.github.perracodex.kopapi.util.sanitize
import io.github.perracodex.kopapi.util.trimOrNull

/**
 * Builds examples for an operation.
 */
@KopapiDsl
internal class ExampleDelegate : IExampleConfigurable {
    /** Holds the named examples associated with the operation. */
    private val examples: MutableMap<String, ApiExample> = mutableMapOf()

    /** Consolidated cache of all examples, including those added via `example` and `examples`. */
    private var examplesCache: ApiExamplesMap = ApiExamplesMap(emptyMap())

    /** Counter to generate sequential example names when none are provided. */
    private var exampleCounter: Int = 1

    override fun example(name: String?, builder: ExampleBuilder.() -> Unit) {
        val exampleName: String = name?.sanitize() ?: generateName()

        if (exampleName.isNotBlank()) {
            val exampleInstance: ExampleBuilder = ExampleBuilder().apply(builder)
            val apiExample = ApiExample(
                summary = exampleInstance.summary.trimOrNull(),
                description = exampleInstance.description.trimOrNull(),
                value = exampleInstance.value
            )

            // Update examples and add to examplesCache
            examples[exampleName] = apiExample
            examplesCache = ApiExamplesMap(examples = examplesCache.examples + (exampleName to apiExample))
        }
    }

    override fun examples(builder: IExampleConfigurable.() -> Unit) {
        with(receiver = this) {
            builder()
        }
    }

    /**
     * Generates a unique sequential name for unnamed examples.
     *
     * @return A unique name like "Example#1", "Example#2", etc., avoiding collisions with existing names.
     */
    private fun generateName(): String {
        var generatedName: String
        do {
            generatedName = "Example#${exampleCounter++}"
        } while (generatedName in examples)
        return generatedName
    }

    /**
     * Returns the registered examples.
     */
    fun build(): IExample? = examplesCache.takeIf { it.examples.isNotEmpty() }
}
