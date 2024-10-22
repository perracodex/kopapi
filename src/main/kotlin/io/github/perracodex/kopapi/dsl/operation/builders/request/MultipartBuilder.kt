/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.builders.request

import io.github.perracodex.kopapi.dsl.markers.OperationDsl
import io.github.perracodex.kopapi.schema.MultipartSchema
import io.github.perracodex.kopapi.system.KopapiException
import io.github.perracodex.kopapi.utils.string.MultilineString
import io.github.perracodex.kopapi.utils.trimOrNull
import io.ktor.http.content.*
import kotlin.collections.set


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
    internal val parts: MutableMap<String, MultipartSchema> = mutableMapOf()

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
     * // Specify the part type explicitly.
     * multipart(contentType = ContentType.MultiPart.Signed) {
     *     part<PartData.FormItem>("myFormPart") {
     *     description = "The form data."
     * }
     * ```
     *
     * @param T The type of the part, typically a subclass of [PartData].
     * @param name The name of the part.
     * @param configure A lambda receiver for configuring the part's metadata.
     */
    public inline fun <reified T : PartData> part(
        name: String,
        configure: PartBuilder.() -> Unit = {}
    ) {
        val partBuilder: PartBuilder = PartBuilder(name = name).apply(configure)

        val schema: MultipartSchema = when (T::class) {
            PartData.FileItem::class ->
                MultipartSchema.FileItem(
                    name = name,
                    isRequired = partBuilder.required,
                    description = partBuilder.description.trimOrNull()
                )

            PartData.FormItem::class ->
                MultipartSchema.FormItem(
                    name = name,
                    isRequired = partBuilder.required,
                    description = partBuilder.description.trimOrNull()
                )

            PartData.BinaryItem::class ->
                MultipartSchema.BinaryItem(
                    name = name,
                    isRequired = partBuilder.required,
                    description = partBuilder.description.trimOrNull()
                )

            PartData.BinaryChannelItem::class ->
                MultipartSchema.BinaryChannelItem(
                    name = name,
                    isRequired = partBuilder.required,
                    description = partBuilder.description.trimOrNull()
                )

            else -> throw KopapiException("Unsupported PartData type: ${T::class}")
        }

        parts[name] = schema
    }

    /**
     * Builds the multipart schema object.
     */
    internal fun build(): MultipartSchema.Object {
        val requiredFields: List<String> = parts.filterValues { it.isRequired }.keys.toList()

        return MultipartSchema.Object(
            description = description.trimOrNull(),
            properties = parts.toMutableMap(),
            requiredFields = requiredFields.takeIf { it.isNotEmpty() },
        )
    }
}
