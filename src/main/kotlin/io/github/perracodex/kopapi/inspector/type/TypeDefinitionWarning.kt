/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.inspector.type

import io.github.perracodex.kopapi.inspector.TypeInspector
import io.github.perracodex.kopapi.inspector.annotation.TypeInspectorAPI

/**
 * Represents a warning for parsed [TypeDefinition] that share the same name but have different types.
 *
 * @param name The conflicting common name.
 * @param conflicts A set of [TypeDefinition] items with the same name but different types.
 */
internal data class TypeDefinitionWarning(
    val name: String,
    val conflicts: MutableSet<TypeDefinition>
)

/**
 * Manager for [TypeDefinition] parsing warnings.
 */
internal object TypeDefinitionWarningManager {
    /** Holds warnings generated during the parsing process. */
    private val warnings: MutableSet<TypeDefinitionWarning> = mutableSetOf()

    /**
     * Retrieves the warnings generated during the parsing process.
     *
     * @return A list of [TypeDefinitionWarning] objects.
     */
    fun get(): Set<TypeDefinitionWarning> = warnings

    /**
     * Clears the warning cache.
     */
    @TypeInspectorAPI
    fun clear() {
        warnings.clear()
    }

    /**
     * Updates the warning cache by detecting conflicting [TypeDefinition] objects
     * with the same name but different types.
     *
     * @param newTypeDefinition The [TypeDefinition] to check for conflicts.
     */
    @TypeInspectorAPI
    fun analyze(newTypeDefinition: TypeDefinition) {
        TypeInspector.getTypeDefinitions().filter { typeDefinition ->
            typeDefinition.name.equals(other = newTypeDefinition.name, ignoreCase = true)
                    && !typeDefinition.type.equals(other = newTypeDefinition.type, ignoreCase = true)
        }.forEach { existing ->
            warnings.find {
                it.name.equals(other = newTypeDefinition.name, ignoreCase = true)
            }?.conflicts?.add(existing)
                ?: warnings.add(
                    TypeDefinitionWarning(
                        name = newTypeDefinition.name,
                        conflicts = mutableSetOf(
                            existing,
                            newTypeDefinition
                        )
                    )
                )
        }
    }
}
