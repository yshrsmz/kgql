package com.codingfeline.kgql.compiler.generator

import com.codingfeline.kgql.compiler.KgqlCustomTypeMapper
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import kotlinx.serialization.internal.EnumSerializer
import kotlinx.serialization.internal.NullableSerializer

class SerializerGenerator(
    private val typeMapper: KgqlCustomTypeMapper
) {

    fun generateCodeBlock(type: TypeName): CodeBlock {
        return visitType(type).foldRight(CodeBlock.of(""), { value, acc -> value.generateCodeBlock(acc) })
    }

    private fun visitType(type: TypeName): List<SerializerPart> {
        return when (type) {
            is ParameterizedTypeName -> {
                // should be List type
                listOf(SerializerPart.List(type.isNullable)) + visitType(type.typeArguments.first())
            }
            else -> {
                if (typeMapper.isCustomType(type)) {
                    if (typeMapper.isEnum(type)) {
                        listOf(
                            SerializerPart.Enum(
                                type.copy(
                                    nullable = false,
                                    annotations = emptyList()
                                ), type.isNullable
                            )
                        )
                    } else {
                        listOf(
                            SerializerPart.Custom(
                                type.copy(
                                    nullable = false,
                                    annotations = emptyList()
                                ), type.isNullable
                            )
                        )
                    }
                } else {
                    listOf(
                        SerializerPart.Primitive(
                            type.copy(
                                nullable = false,
                                annotations = emptyList()
                            ), type.isNullable
                        )
                    )
                }
            }
        }
    }


    sealed class SerializerPart {
        abstract val isNullable: Boolean
        abstract fun generateCodeBlockInternal(block: CodeBlock): CodeBlock

        private fun wrapWithNullableSerializerIfNeeded(block: CodeBlock): CodeBlock {
            return if (isNullable) {
                buildCodeBlock {
                    add("%T(%L)", NullableSerializer::class.asTypeName(), block)
                }
            } else {
                block
            }
        }

        fun generateCodeBlock(block: CodeBlock): CodeBlock {
            return wrapWithNullableSerializerIfNeeded(generateCodeBlockInternal(block))
        }

        data class List(override val isNullable: Boolean = true) : SerializerPart() {
            override fun generateCodeBlockInternal(block: CodeBlock): CodeBlock {
                return buildCodeBlock {
                    add("%L.%M", block, listSerializer)
                }
            }
        }

        data class Primitive(val typeName: TypeName, override val isNullable: Boolean = true) : SerializerPart() {
            override fun generateCodeBlockInternal(block: CodeBlock): CodeBlock {
                return buildCodeBlock {
                    add("%T.%M()", typeName, primitiveSerializer)
                }
            }
        }

        data class Custom(val typeName: TypeName, override val isNullable: Boolean = true) : SerializerPart() {
            override fun generateCodeBlockInternal(block: CodeBlock): CodeBlock {
                return buildCodeBlock {
                    add("%T.serializer()", typeName)
                }
            }
        }

        data class Enum(val type: TypeName, override val isNullable: Boolean = true) : SerializerPart() {
            override fun generateCodeBlockInternal(block: CodeBlock): CodeBlock {
                return buildCodeBlock {
                    add("%T(%T::class)", EnumSerializer::class.asTypeName(), type)
                }
            }
        }

        companion object {
            val primitiveSerializer = MemberName("kotlinx.serialization", "serializer")
            val listSerializer = MemberName("kotlinx.serialization", "list")
        }
    }
}
