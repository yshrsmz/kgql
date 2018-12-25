package com.codingfeline.kgql.core.compiler

import com.codingfeline.kgql.compiler.KgqlCustomTypeMapper
import com.squareup.kotlinpoet.*
import graphql.language.VariableDefinition

class VariableWrapperGenerator(
        val operationName:String?,
        val variables:List<VariableDefinition>,
        val typeMapper: KgqlCustomTypeMapper
) {

    fun type() : TypeSpec {
        val classSpec = TypeSpec.classBuilder("${operationName?.capitalize() ?: ""}Variables")
                .addModifiers(KModifier.DATA)
                .primaryConstructor(generateConstructor(variables))
                .addProperties(generateProperties(variables))

        return classSpec.build()
    }

    fun generateConstructor(variables: List<VariableDefinition>):FunSpec {
        return FunSpec.constructorBuilder()
                .addParameters(variables.map {
                    ParameterSpec.builder(it.name, typeMapper.get(it.type)).build()
                })
                .build()
    }

    fun generateProperties(variables:List<VariableDefinition>): List<PropertySpec> {
        return variables.map {
            PropertySpec.builder(it.name, typeMapper.get(it.type))
                    .initializer(it.name)
                    .build()
        }
    }
}
