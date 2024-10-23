/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.request

import io.github.perracodex.kopapi.dsl.markers.OperationDsl
import io.github.perracodex.kopapi.dsl.operation.elements.ApiMultipart
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.http.*
import io.ktor.http.content.*
import kotlin.reflect.typeOf

/**
 * Builder for constructing multipart request bodies.
 *
 * Allows defining individual parts with names and types, along with their descriptions.
 *
 * @property description An optional description of the multipart request.
 */
@OperationDsl
public class MultipartBuilder internal constructor() {
    /** Holds the parts of the multipart request. */
    @PublishedApi
    internal val parts: MutableList<ApiMultipart.Part> = mutableListOf()

    public var description: String by MultilineString()

    /**
     * Adds a part to the multipart request body.
     *
     * #### Sample Usage
     * ```
     * // Implicit ContentType.MultiPart.FormData (default).
     * multipart {
     *      part<PartData.FileItem>("file") {
     *          description = "The file to upload."
     *      }
     *      part<PartData.FormItem>("metadata") {
     *          description = "Metadata about the file, provided as JSON."
     *      }
     * }
     *
     * // Explicit ContentType.MultiPart.Encrypted.
     * multipart(contentType = ContentType.MultiPart.Encrypted) {
     *      part<PartData.FileItem>("secureFile") {
     *          description = "A securely uploaded file."
     *      }
     *      part<PartData.FormItem>("metadata") {
     *          description = "Additional metadata about the file."
     *      }
     * }
     * ```
     *
     * @param T The type of the part, typically a subclass of [PartData].
     * @param name The name of the part.
     * @param contentType Optional content type for the part.
     * @param configure A lambda receiver for configuring the part's metadata.
     */
    public inline fun <reified T : PartData> part(
        name: String,
        contentType: ContentType? = null,
        configure: PartBuilder.() -> Unit = {}
    ) {
        val partName: String = name.trim()
        if (parts.find { it.name.equals(partName, ignoreCase = true) } != null) {
            throw KopapiException("Part with name '$name' has already being defined in the multipart request.")
        }

        val partBuilder: PartBuilder = PartBuilder(name = name).apply(configure)

        val part = ApiMultipart.Part(
            type = typeOf<T>(),
            name = partName,
            contentType = contentType,
            schemaType = partBuilder.schemaType,
            schemaFormat = partBuilder.schemaFormat,
            description = partBuilder.description.trimOrNull(),
            isRequired = partBuilder.required
        )

        parts.add(part)
    }
}
