/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.dsl.operation.elements

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.perracodex.kopapi.inspector.schema.Schema
import kotlin.reflect.KType

/**
 * Represents the [Schema] definition for content in an API operation.
 *
 * This class is used to wrap the schema under the `schema` key for any content type
 * (e.g., `application/json`, `application/xml`), ensuring compatibility with the OpenAPI
 * specification.
 *
 * @property type The [KType] for which the content schema will be generated.
 * @property schema The [Schema] representing the structure of the content.
 */
internal data class ContentSchema(
    @JsonIgnore
    val type: KType,
    @JsonProperty("schema")
    var schema: Schema?
)
