/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.dsl.operation.builder.request

import io.github.perracodex.kopapi.dsl.example.delegate.ExampleDelegate
import io.github.perracodex.kopapi.dsl.example.delegate.IExampleConfigurable
import io.github.perracodex.kopapi.dsl.example.element.IExample
import io.github.perracodex.kopapi.dsl.marker.KopapiDsl
import io.github.perracodex.kopapi.dsl.operation.element.ApiMultipart
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.util.orNull
import io.github.perracodex.kopapi.util.string.MultilineString
import io.github.perracodex.kopapi.util.trimOrNull
import io.ktor.http.*
import io.ktor.http.content.*
import kotlin.reflect.typeOf

/**
 * Builder for constructing multipart request bodies.
 *
 * Allows defining individual parts with names and types, along with their descriptions.
 *
 * @property description An optional description of the multipart request.
 * @property contentType The content type of the multipart request. Default: `FormData`.
 */
@KopapiDsl
public class MultipartBuilder internal constructor(
    private val examplesDelegate: ExampleDelegate = ExampleDelegate()
) : IExampleConfigurable by examplesDelegate {

    public var description: String by MultilineString()
    public var contentType: ContentType? = null

    @Suppress("PropertyName", "VariableNaming")
    @PublishedApi
    internal val _config: Config = Config()

    /**
     * Adds a part to the multipart request body.
     *
     * #### Usage
     * ```
     * // Implicit ContentType.MultiPart.FormData (default).
     * multipart {
     *      part<PartData.FileItem>("file") {
     *          description = "The file to upload."
     *          contentType = setOf(
     *              ContentType.Image.JPEG,
     *              ContentType.Image.PNG
     *          )
     *      }
     *      part<PartData.FormItem>("metadata") {
     *          description = "Metadata about the file, provided as JSON."
     *      }
     * }
     * ```
     * ```
     * // Explicit ContentType.MultiPart.Signed.
     * multipart {
     *      contentType = ContentType.MultiPart.Signed
     *
     *      part<PartData.FileItem>("secureFile") {
     *          description = "A securely uploaded file."
     *          contentType = setOf(
     *              ContentType.Image.JPEG,
     *              ContentType.Image.PNG
     *          )
     *      }
     *      part<PartData.FormItem>("metadata") {
     *          description = "Additional metadata about the file."
     *      }
     * }
     * ```
     *
     * @receiver [PartBuilder] The builder used to configure the part's metadata.
     *
     * @param T The type of the part, typically a subclass of [PartData].
     * @param name The name of the part.
     */
    public inline fun <reified T : PartData> part(
        name: String,
        noinline builder: PartBuilder.() -> Unit = {}
    ) {
        val partName: String = name.trim()
        if (_config.parts.find { it.name.equals(partName, ignoreCase = true) } != null) {
            throw KopapiException("Part with name '$name' has already being defined in the multipart request.")
        }

        val partBuilder: PartBuilder = PartBuilder(name = name).apply(builder)

        val part = ApiMultipart.Part(
            type = typeOf<T>(),
            name = partName,
            contentType = partBuilder.contentType?.orNull(),
            schemaType = partBuilder.schemaType,
            schemaFormat = partBuilder.schemaFormat.trimOrNull(),
            description = partBuilder.description.trimOrNull(),
            isRequired = partBuilder.required,
            headers = partBuilder.buildHeaders(),
        )

        _config.parts.add(part)
    }

    @PublishedApi
    internal inner class Config {
        /** Holds the parts of the multipart request. */
        val parts: MutableList<ApiMultipart.Part> = mutableListOf()

        /**
         * Returns the registered examples.
         */
        fun buildExamples(): IExample? {
            return examplesDelegate.build()
        }
    }
}
