package com.codingfeline.kgql.compiler.generator

import com.codingfeline.kgql.core.KgqlRequestBody
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import graphql.language.OperationDefinition
import kotlinx.serialization.Serializable

class RequestBodyGenerator(
    val operation: OperationDefinition,
    val parentObjectFqName: String,
    val variablesSpec: TypeSpec?,
    val documentProp: PropertySpec
) {

    fun generateType(): TypeSpec {
        val variablesType = variablesSpec.typeNameOrUnit()
        val spec = TypeSpec.classBuilder("Request")
            .addModifiers(KModifier.DATA)
            .addAnnotation(Serializable::class)
            .addSuperinterface(
                KgqlRequestBody::class.asTypeName().plusParameter(variablesType)
            )

        val constructorSpec = FunSpec.constructorBuilder()

        val variablesParameterSpec = ParameterSpec
            .builder(
                "variables",
                variablesType.copy(nullable = true),
                KModifier.OVERRIDE
            )
            .addAnnotation(generateSerialName("variables"))

        if (variablesSpec == null) {
            variablesParameterSpec.defaultValue("null")
        }

        constructorSpec
            .addParameter(variablesParameterSpec.build())
            .addParameter(
                ParameterSpec
                    .builder(
                        "operationName",
                        String::class.asTypeName().copy(nullable = true),
                        KModifier.OVERRIDE
                    )
                    .addAnnotation(generateSerialName("operationName"))
                    .defaultValue(operation.name?.let { "\"${operation.name}\"" } ?: "null")
                    .build()
            )
            .addParameter(
                ParameterSpec
                    .builder(
                        "query",
                        String::class,
                        KModifier.OVERRIDE
                    )
                    .defaultValue(documentProp.name)
                    .addAnnotation(generateSerialName("query"))
                    .build()
            )

        spec.primaryConstructor(constructorSpec.build())

        spec
            .addProperty(
                PropertySpec
                    .builder(
                        "operationName",
                        String::class.asTypeName().copy(nullable = true),
                        KModifier.OVERRIDE
                    )
                    .initializer("operationName")
                    .build()
            )
            .addProperty(
                PropertySpec
                    .builder("query", String::class, KModifier.OVERRIDE)
                    .initializer("query")
                    .build()
            )
            .addProperty(
                PropertySpec
                    .builder("variables", variablesType.copy(nullable = true), KModifier.OVERRIDE)
                    .initializer("variables")
                    .build()
            )

        return spec.build()
    }

    private fun TypeSpec?.typeNameOrUnit(): TypeName {
        return if (this == null) {
            Unit::class.asTypeName()
        } else {
            ClassName.bestGuess("$parentObjectFqName.$name")
        }
    }
}
