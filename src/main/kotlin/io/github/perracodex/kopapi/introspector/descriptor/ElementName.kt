/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.introspector.descriptor

import io.github.perracodex.kopapi.introspector.annotation.TypeIntrospectorApi

/**
 * Holds naming details for an element, including any renaming information.
 *
 * An element may be renamed due to annotations such as `@SerialName` or `@JsonAlias`.
 *
 * @property name The current name of the element. If renamed, this reflects the updated name.
 * @property renamedFrom The original name of the element before renaming. It is `null` if the name was not changed.
 */
@TypeIntrospectorApi
internal data class ElementName(
    val name: String,
    val renamedFrom: String? = null
)
