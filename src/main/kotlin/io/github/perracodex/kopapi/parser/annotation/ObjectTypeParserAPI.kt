/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.parser.annotation

/**
 * Annotation for controlled access to the ObjectTypeParser API.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "Only to be used within the ObjectTypeParser API.")
@Retention(AnnotationRetention.BINARY)
internal annotation class ObjectTypeParserAPI
