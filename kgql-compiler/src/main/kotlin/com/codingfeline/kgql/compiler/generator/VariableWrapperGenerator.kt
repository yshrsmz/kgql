package com.codingfeline.kgql.compiler.generator

import com.codingfeline.kgql.compiler.KgqlCustomTypeMapper
import com.squareup.kotlinpoet.*
import graphql.language.VariableDefinition
import kotlinx.serialization.Serializable

class VariableWrapperGenerator(
    val variables: List<VariableDefinition>,
    val typeMapper: KgqlCustomTypeMapper
) {

    fun generateType(): TypeSpec {
        val classSpec = TypeSpec.classBuilder("Variables")
            .addModifiers(KModifier.DATA)
            .addAnnotation(Serializable::class.java)
            .primaryConstructor(generateConstructor(variables))
            .addProperties(generateProperties(variables))

        return classSpec.build()
    }

    private fun generateConstructor(variables: List<VariableDefinition>): FunSpec {
        return FunSpec.constructorBuilder()
            .addParameters(variables.map {
                ParameterSpec.builder(it.name, typeMapper.get(it.type)).build()
            })
            .build()
    }

    private fun generateProperties(variables: List<VariableDefinition>): List<PropertySpec> {
        return variables.map {
            PropertySpec.builder(it.name, typeMapper.get(it.type))
                .initializer(it.name)
                .build()
        }
    }
}
