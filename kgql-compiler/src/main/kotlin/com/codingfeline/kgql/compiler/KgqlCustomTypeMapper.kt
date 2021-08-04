package com.codingfeline.kgql.compiler

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.Type

class KgqlCustomTypeMapper(
    private val typeMap: Map<GraphQLCustomTypeName, GraphQLCustomTypeFQName>
) {
    fun get(type: Type<*>): TypeName {
        return when (type) {
            is NonNullType -> get(type.type).copy(nullable = false)
            is ListType -> ClassName("kotlin.collections", "List")
                .plusParameter(get(type.type)).copy(nullable = true)
            else -> {
                when ((type as graphql.language.TypeName).name) {
                    // GraphQL embedded type
                    "ID" -> String::class.asTypeName()
                    "String" -> String::class.asTypeName()
                    "Int" -> Int::class.asTypeName()
                    "Float" -> Float::class.asTypeName()
                    "Boolean" -> Boolean::class.asTypeName()
                    else -> mapCustomType(type)
                }.copy(nullable = true)
            }
        }
    }

    private fun mapCustomType(type: graphql.language.TypeName): TypeName {
        return typeMap[type.name]?.let { fqName ->
            val parts = fqName.split('.')
            ClassName(parts.dropLast(1).joinToString("."), parts.last())
        } ?: ANY
    }

    fun isCustomType(type: TypeName): Boolean {
        return typeMap.values.contains(type.copy(nullable = false, annotations = emptyList()).toString())
    }

    fun hasCustomType(gqlTypeName: String): Boolean {
        return typeMap.containsKey(gqlTypeName)
    }
}
