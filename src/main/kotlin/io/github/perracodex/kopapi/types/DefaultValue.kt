/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package io.github.perracodex.kopapi.types

import io.github.perracodex.kopapi.utils.orNull

/**
 * Represents allowable default values for OpenAPI parameters.
 *
 * - Supports simple types (e.g.,`String`, `Int`, `Boolean`), including arrays of primitives and enums.
 * - Complex objects are not supported.
 */
@Suppress("ClassName")
public sealed class DefaultValue {
    // -------------------------
    // Primitive Types.
    // -------------------------

    /**
     * Default value of type `String`.
     *
     * #### Usage
     * ```
     * DefaultValue.ofString("hello")
     * ```
     *
     * @property value The default string value for the parameter.
     */
    public data class ofString(val value: String) : DefaultValue()

    /**
     * Default value of type `Int`.
     *
     * #### Usage
     * ```
     * DefaultValue.ofInt(1)
     * ```
     *
     * @property value The default integer value for the parameter.
     */
    public data class ofInt(val value: Int) : DefaultValue()

    /**
     * Default value of type `Long`.
     *
     * #### Usage
     * ```
     * DefaultValue.ofLong(1L)
     * ```
     *
     * @property value The default long value for the parameter.
     */
    public data class ofLong(val value: Long) : DefaultValue()

    /**
     * Default value of type `Float`.
     *
     * #### Usage
     * ```
     * DefaultValue.ofFloat(1.0f)
     * ```
     *
     * @property value The default float value for the parameter.
     */
    public data class ofFloat(val value: Float) : DefaultValue()

    /**
     * Default value of type `Double`.
     *
     * #### Usage
     * ```
     * DefaultValue.ofDouble(1.0)
     * ```
     *
     * @property value The default double value for the parameter.
     */
    public data class ofDouble(val value: Double) : DefaultValue()

    /**
     * Default value of type `Boolean`.
     *
     * #### Usage
     * ```
     * DefaultValue.ofBoolean(true)
     * ```
     *
     * @property value The default boolean value for the parameter.
     */
    public data class ofBoolean(val value: Boolean) : DefaultValue()

    // -------------------------
    // Arrays of Primitive Types.
    // -------------------------

    /**
     * Default value for an array of `String`.
     *
     * #### Usage
     * ```
     * DefaultValue.ofStringArray("value1", "value2", "value3")
     * ```
     *
     * @property value The default list of string values for the parameter.
     */
    public data class ofStringArray private constructor(val value: List<String>) : DefaultValue() {
        public constructor(vararg value: String) : this(value = value.toList())
    }

    /**
     * Default value for an array of `Int`.
     *
     * #### Usage
     * ```
     * DefaultValue.ofIntArray(1, 2, 3)
     * ```
     *
     * @property value The default list of integer values for the parameter.
     */
    public data class ofIntArray private constructor(val value: List<Int>) : DefaultValue() {
        public constructor(vararg value: Int) : this(value = value.toList())
    }

    /**
     * Default value for an array of `Long`.
     *
     * #### Usage
     * ```
     * DefaultValue.ofLongArray(1L, 2L, 3L)
     * ```
     *
     * @property value The default list of long values for the parameter.
     */
    public data class ofLongArray private constructor(val value: List<Long>) : DefaultValue() {
        public constructor(vararg value: Long) : this(value = value.toList())
    }

    /**
     * Default value for an array of `Double`.
     *
     * #### Usage
     * ```
     * DefaultValue.ofDoubleArray(1.0, 2.0, 3.0)
     * ```
     *
     * @property value The default list of double values for the parameter.
     */
    public data class ofDoubleArray private constructor(val value: List<Double>) : DefaultValue() {
        public constructor(vararg value: Double) : this(value = value.toList())
    }

    /**
     * Default value for an array of `Float`.
     *
     * #### Usage
     * ```
     * DefaultValue.ofFloatArray(1.0f, 2.0f, 3.0f)
     * ```
     *
     * @property value The default list of float values for the parameter.
     */
    public data class ofFloatArray private constructor(val value: List<Float>) : DefaultValue() {
        public constructor(vararg value: Float) : this(value = value.toList())
    }

    /**
     * Default value for an array of `Boolean`.
     *
     * #### Usage
     * ```
     * DefaultValue.ofBooleanArray(true, false, true)
     * ```
     *
     * @property value The default list of boolean values for the parameter.
     */
    public data class ofBooleanArray private constructor(val value: List<Boolean>) : DefaultValue() {
        public constructor(vararg value: Boolean) : this(value = value.toList())
    }

    // -------------------------
    // Date and Date-Time Types.
    // -------------------------

    /**
     * Default value for a single `Date`.
     *
     * #### Usage
     * ```
     * DefaultValue.ofDate("2024-12-31")
     * ```
     *
     * @property value The default date value for the parameter. Format should be "date".
     */
    public data class ofDate(val value: String) : DefaultValue() // Format should be "date"

    /**
     * Default value for a single `Date-Time`.
     *
     * #### Usage
     * ```
     * DefaultValue.ofDateTime("2024-12-31T23:59:59Z")
     * ```
     *
     * @property value The default date-time value for the parameter. Format should be "date-time".
     */
    public data class ofDateTime(val value: String) : DefaultValue() // Format should be "date-time"

    /**
     * Default value for an array of `Date`.
     *
     * #### Usage
     * ```
     * DefaultValue.ofDateArray("2024-12-31", "2025-01-01")
     * ```
     *
     * @property value The default list of date values for the parameter. Format should be "date".
     */
    public data class ofDateArray private constructor(val value: List<String>) : DefaultValue() {
        public constructor(vararg value: String) : this(value = value.toList())
    }

    /**
     * Default value for an array of `Date-Time`.
     *
     * #### Usage
     * ```
     * DefaultValue.ofDateTimeArray("2024-12-31T23:59:59Z", "2025-01-01T00:00:00Z")
     * ```
     *
     * @property value The default list of date-time values for the parameter. Format should be "date-time".
     */
    public data class ofDateTimeArray private constructor(val value: List<String>) : DefaultValue() {
        public constructor(vararg value: String) : this(value = value.toList())
    }

    // -------------------------
    // Enum Types.
    // -------------------------

    /**
     * Default value for enum types.
     *
     * #### Usage
     * ```
     * DefaultValue.ofEnum(MyEnum.DEFAULT)
     * ```
     *
     * @param T The enum type.
     * @property value The default enum value for the parameter.
     */
    public data class ofEnum<T : Enum<T>>(val value: T) : DefaultValue()

    /**
     * Default value for an array of enum types.
     *
     * #### Usage
     * ```
     * DefaultValue.ofEnumArray(MyEnum.DEFAULT, MyEnum.ALTERNATE)
     * ```
     *
     * @param T The enum type.
     * @property value The default list of enum values for the parameter.
     */
    public data class ofEnumArray<T : Enum<T>> private constructor(val value: List<T>) : DefaultValue() {
        public constructor(vararg value: T) : this(value = value.toList())
    }

    /**
     * Converts the default value to its actual value representation.
     *
     * For single values, it returns the value directly.
     * For arrays, it returns the list only if it's not empty; otherwise, returns `null`.
     * Enums are converted to their `name` strings.
     *
     * @return The actual value of the default, or `null` if the array is empty.
     */
    public fun toValue(): Any? {
        return when (this) {
            is ofString -> this.value
            is ofInt -> this.value
            is ofLong -> this.value
            is ofFloat -> this.value
            is ofDouble -> this.value
            is ofBoolean -> this.value
            is ofDate -> this.value
            is ofDateTime -> this.value
            is ofEnum<*> -> this.value.name
            is ofStringArray -> this.value.orNull()
            is ofIntArray -> this.value.orNull()
            is ofLongArray -> this.value.orNull()
            is ofDoubleArray -> this.value.orNull()
            is ofFloatArray -> this.value.orNull()
            is ofBooleanArray -> this.value.orNull()
            is ofDateArray -> this.value.orNull()
            is ofDateTimeArray -> this.value.orNull()
            is ofEnumArray<*> -> this.value.orNull()?.map { it.name }
        }
    }
}
