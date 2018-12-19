package com.codingfeline.kgql.dsl

sealed class InputValueType(val typeName: String, open val isNullable: Boolean) {
    override fun toString(): String = "$typeName${if (!isNullable) "!" else ""}"

    data class INT(override val isNullable: Boolean = true) : InputValueType(typeName = "Int", isNullable = isNullable)
    data class FLOAT(override val isNullable: Boolean = true) : InputValueType(typeName = "Float", isNullable = isNullable)
    data class BOOLEAN(override val isNullable: Boolean = true) : InputValueType(typeName = "Boolean", isNullable = isNullable)
    data class STRING(override val isNullable: Boolean = true) : InputValueType(typeName = "String", isNullable = isNullable)
    data class ENUM(val name: String, override val isNullable: Boolean = true) : InputValueType(typeName = name, isNullable = isNullable)
    data class OBJECT(val name: String, override val isNullable: Boolean = true) : InputValueType(typeName = name, isNullable = isNullable)
}
