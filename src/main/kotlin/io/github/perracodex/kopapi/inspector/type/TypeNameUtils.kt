/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.type

import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.javaType

/**
 * Extension function to return the Java type name of a KType.
 */
@TypeInspectorAPI
internal fun KType.nativeName(): String {
    return this.javaType.typeName
}

/**
 * Extension function to safely get a type name for a [KType].
 * If the name cannot be determined, it creates a fallback name based on the type structure.
 */
@TypeInspectorAPI
internal fun KType.safeName(): String {
    // Attempt to retrieve the KClass from the classifier and use its safe name if possible.
    val kClass: KClass<*>? = this.classifier as? KClass<*>
    return kClass?.safeName()
        ?: "UnknownType_${this.toString().cleanName()}"
}

/**
 * Extension function to safely get a class name.
 * If The name cannot be determined, it creates a fallback name based on the class type.
 */
@TypeInspectorAPI
internal fun KClass<*>.safeName(): String {
    return this.simpleName
        ?: this.qualifiedName?.substringAfterLast(delimiter = '.')
        ?: "UnknownClass_${this.toString().cleanName()}"
}

/**
 * Extension function to clean a string by replacing all non-alphanumeric characters with underscores.
 */
@TypeInspectorAPI
internal fun String.cleanName(): String {
    return this.replace(Regex(pattern = "[^A-Za-z0-9_]"), replacement = "_")
}
