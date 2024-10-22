/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.request

import io.github.perracodex.kopapi.dsl.markers.OperationDsl
import io.github.perracodex.kopapi.dsl.operation.elements.ApiMultipart
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
public class MultipartBuilder {
    /** Holds the parts of the multipart request. */
    @PublishedApi
    internal val parts: MutableList<ApiMultipart.Part> = mutableListOf()

    public var description: String by MultilineString()

    /**
     * Adds a part to the multipart request body.
     *
     * #### Sample Usage
     * ```
     * // Default ContentType.MultiPart.FormData
     * multipart {
     *      part<PartData.FileItem>("myFilePart") {
     *          description = "The file to upload."
     *      }
     * }
     *
     * // Specify the part with explicit details.
     * multipart(contentType = ContentType.MultiPart.Mixed) {
     *      // Upload the profile picture as an image (PNG)
     *      part<PartData.FileItem>("img", contentType=ContentType.Image.PNG) {
     *          description = "The profile picture."
     *          schemaType = ApiType.STRING
     *          schemaFormat = ApiFormat.BINARY
     *      }
     *
     *      // Add a form field for employee's name (plain text)
     *      part<PartData.FormItem>("employeeName") {
     *          description = "The employee's full name."
     *          schemaType = ApiType.STRING
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
        val partBuilder: PartBuilder = PartBuilder(name = name).apply(configure)

        val part = ApiMultipart.Part(
            type = typeOf<T>(),
            name = name.trim(),
            contentType = contentType,
            schemaType = partBuilder.schemaType,
            schemaFormat = partBuilder.schemaFormat,
            description = partBuilder.description.trimOrNull(),
            isRequired = partBuilder.required
        )

        parts.removeIf { it.name.equals(part.name, ignoreCase = true) }.also {
            parts.add(part)
        }
    }
}
