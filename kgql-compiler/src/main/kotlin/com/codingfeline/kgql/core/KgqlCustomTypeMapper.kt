package com.codingfeline.kgql.core

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.Type

class KgqlCustomTypeMapper(
    val typeMap: Map<GraphQLCustomTypeName, GraphQLCustomTypeFQName>
) {
    fun get(type: Type<*>): TypeName {
        return when (type) {
            is NonNullType -> get(type.type).copy(nullable = true)
            is ListType -> ClassName("kotlin.collections", "List")
                .plusParameter(get(type.type))
            else -> {
                when ((type as graphql.language.TypeName).name) {
                    "String" -> String::class.asTypeName()
                    "Int" -> Int::class.asTypeName()
                    "Float" -> Float::class.asTypeName()
                    "Boolean" -> Boolean::class.asTypeName()
                    else -> mapCustomType(type)
                }
            }
        }
    }

    private fun mapCustomType(type: graphql.language.TypeName): TypeName {
        return typeMap[type.name]?.let { fqName ->
            val parts = fqName.split('.')
            ClassName(parts.dropLast(1).joinToString("."), parts.last())
        } ?: ANY
    }
}
