package com.codingfeline.kgql.compiler.generator

import com.codingfeline.kgql.compiler.KgqlCustomTypeMapper
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import graphql.language.VariableDefinition
import kotlinx.serialization.Serializable

class VariableWrapperGenerator(
    val operationName: String?,
    val variables: List<VariableDefinition>,
    val typeMapper: KgqlCustomTypeMapper
) {

    fun type(): TypeSpec {
        val classSpec = TypeSpec.classBuilder("${operationName?.capitalize() ?: ""}Variables")
            .addModifiers(KModifier.DATA)
            .addAnnotation(Serializable::class.java)
            .primaryConstructor(generateConstructor(variables))
            .addProperties(generateProperties(variables))

        return classSpec.build()
    }

    fun generateConstructor(variables: List<VariableDefinition>): FunSpec {
        return FunSpec.constructorBuilder()
            .addParameters(variables.map {
                ParameterSpec.builder(it.name, typeMapper.get(it.type)).build()
            })
            .build()
    }

    fun generateProperties(variables: List<VariableDefinition>): List<PropertySpec> {
        return variables.map {
            PropertySpec.builder(it.name, typeMapper.get(it.type))
                .initializer(it.name)
                .build()
        }
    }
}
