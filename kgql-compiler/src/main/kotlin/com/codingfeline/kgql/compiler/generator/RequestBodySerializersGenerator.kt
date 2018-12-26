package com.codingfeline.kgql.compiler.generator

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.internal.UnitSerializer

class RequestBodySerializersGenerator(
    private val documentWrapperSpec: TypeSpec.Builder
) {

    fun generateFunctions(): List<FunSpec> {
//        val companion = TypeSpec.companionObjectBuilder()
//
//        companion.addFunctions(documentWrapperSpec.funSpecs.map { generateSerializerFunction(it) })
//
//        return companion.build()

        return documentWrapperSpec.funSpecs.map { generateSerializerFunction(it) }
    }

    private fun generateSerializerFunction(funSpec: FunSpec): FunSpec {
        val function = FunSpec.builder(name = "${funSpec.name}Serializer")
            .returns(KSerializer::class.asTypeName().plusParameter(typeArgument = funSpec.returnType!!))

        funSpec.parameters.forEach {
            println(it)
            println(it.type)
        }

        val variableSerializerLiteral: String = funSpec.parameters.firstOrNull()?.let { "${it.type}.serializer()" }
            ?: kotlin.run { UnitSerializer::class.asTypeName().toString() }
        function.addStatement(
            "return KgqlRequestBody.serializer(%L)",
            variableSerializerLiteral
        )

        return function.build()
    }
}
