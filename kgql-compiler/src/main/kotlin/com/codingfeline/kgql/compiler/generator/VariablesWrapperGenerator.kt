package com.codingfeline.kgql.compiler.generator

import com.codingfeline.kgql.compiler.KgqlCustomTypeMapper
import com.codingfeline.kgql.core.KgqlRequestBody
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import graphql.language.VariableDefinition
import kotlinx.serialization.Serializable

class VariableWrapperGenerator(
    val variables: List<VariableDefinition>,
    val typeMapper: KgqlCustomTypeMapper
) {

    fun generateType(): TypeSpec {
        val classSpec = TypeSpec.classBuilder("Variables")
            .addModifiers(KModifier.DATA)
            .addAnnotation(Serializable::class)
            .primaryConstructor(generateConstructor(variables))
            .addProperties(generateProperties(variables))

        return classSpec.build()
    }

    private fun generateConstructor(variables: List<VariableDefinition>): FunSpec {
        return FunSpec.constructorBuilder()
            .addParameters(variables.map {
                val type = typeMapper.get(it.type)
                val spec = ParameterSpec.builder(it.name, type)
                    .addAnnotation(generateSerialName(it.name))

                if (type.isNullable) {
                    spec.defaultValue("null")
                }

                spec.build()
            })
            .build()
    }

    private fun generateProperties(variables: List<VariableDefinition>): List<PropertySpec> {
        return variables.map {
            val type = typeMapper.get(it.type)
            val spec = PropertySpec.builder(it.name, type)
                .initializer(it.name)

            spec.build()
        }
    }
}

data class Request(
    override val variables: String?
) : KgqlRequestBody<String> {
    override val operationName: String? = null
    override val query: String = ""
}
