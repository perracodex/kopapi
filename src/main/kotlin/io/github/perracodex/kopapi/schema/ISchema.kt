/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.schema

import io.github.perracodex.kopapi.composer.OpenAPiSchema
import io.github.perracodex.kopapi.composer.annotation.ComposerAPI
import io.github.perracodex.kopapi.types.Composition

/**
 * Marker interface for schema types used in OpenAPI.
 * It is used to mark classes as valid schema types in the OpenAPI generation process.
 */
internal interface ISchema {

    companion object {
        /**
         * Determines the appropriate [OpenAPiSchema.ContentSchema] based on the given composition
         * and a list of `Schema` objects.
         *
         * - If only one schema is present, it returns that schema directly.
         * - If multiple schemas are present, it combines them according to
         *   the specified `composition` type, defaulting to `Composition.ANY_OF`.
         *
         * @param composition The [Composition] type to apply when combining multiple schemas.
         *                    Defaults to `Composition.ANY_OF` if null.
         * @param schemas The list of [ElementSchema] objects to be combined. Assumes the list is non-empty and preprocessed.
         * @return An [OpenAPiSchema.ContentSchema] representing the combined schema.
         */
        @OptIn(ComposerAPI::class)
        fun determineSchema(composition: Composition?, schemas: List<ElementSchema>): OpenAPiSchema.ContentSchema {
            val combinedSchema: ISchema = when {
                schemas.size == 1 -> schemas.first()
                else -> when (composition ?: Composition.ANY_OF) {
                    Composition.ANY_OF -> CompositionSchema.AnyOf(anyOf = schemas)
                    Composition.ALL_OF -> CompositionSchema.AllOf(allOf = schemas)
                    Composition.ONE_OF -> CompositionSchema.OneOf(oneOf = schemas)
                }
            }

            return OpenAPiSchema.ContentSchema(schema = combinedSchema)
        }
    }
}
