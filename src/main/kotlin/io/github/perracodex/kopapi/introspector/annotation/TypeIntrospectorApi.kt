/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

package io.github.perracodex.kopapi.introspector.annotation

/**
 * Annotation for controlled access to the TypeIntrospector API.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR, message = "Only to be used within the TypeIntrospector API.")
@Retention(AnnotationRetention.BINARY)
internal annotation class TypeIntrospectorApi
