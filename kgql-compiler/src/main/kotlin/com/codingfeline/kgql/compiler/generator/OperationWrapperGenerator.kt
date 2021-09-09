package com.codingfeline.kgql.compiler.generator

import com.codingfeline.kgql.compiler.KgqlCustomTypeMapper
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
import kotlinx.serialization.KSerializer

private const val PARAM_VARIABLES_NAME = "variables"

class OperationWrapperGenerator(
    private val documentProp: PropertySpec,
    private val typeMapper: KgqlCustomTypeMapper,
    private val parentFQName: String
) {

    fun generateObject(operation: OperationDefinition): TypeSpec {
        val name = "${(operation.name ?: "").capitalize()}${operation.operation.name.toLowerCase().capitalize()}"
        val hasVariables = operation.variableDefinitions.isNotEmpty()
        var variableSpec: TypeSpec? = null

        val objectSpec = TypeSpec.objectBuilder(name = name)

        val operationNamePropSpec = PropertySpec.builder(
            "operationName",
            String::class.asTypeName().copy(nullable = true)
        )
            .addModifiers(KModifier.PUBLIC)
            .initializer("%S", operation.name)
            .build()

        objectSpec.addProperty(operationNamePropSpec)

        if (hasVariables) {
            variableSpec = VariableWrapperGenerator(operation.variableDefinitions, typeMapper).generateType()
            objectSpec.addType(variableSpec)
        }

        val requestBodySpec =
            RequestBodyGenerator(
                operationNameProp = operationNamePropSpec,
                parentDocumentFqName = parentFQName,
                parentObjectName = name,
                variablesSpec = variableSpec,
                documentProp = documentProp
            ).generateType()
        objectSpec.addType(requestBodySpec)

        val operationFunSpec = generateOperationFunction(
            parentObjectName = name,
            variablesSpec = variableSpec,
            requestBodySpec = requestBodySpec
        )
        objectSpec.addFunction(operationFunSpec)
        objectSpec.addFunction(generateSerializerFunction(requestBodySpec, name))

        return objectSpec.build()
    }

    private fun generateParameterSpecFromVariable(variables: TypeName): ParameterSpec {
        return ParameterSpec.builder(
            name = PARAM_VARIABLES_NAME,
            type = variables
        )
            .build()
    }

    private fun generateOperationFunction(
        parentObjectName: String,
        variablesSpec: TypeSpec?,
        requestBodySpec: TypeSpec
    ): FunSpec {
        val variablesType: TypeName = variablesSpec.typeNameOrUnit(parentObjectName)

        val spec = FunSpec.builder("requestBody")
            .returns(requestBodySpec.typeNameOrUnit(parentObjectName))

        if (variablesSpec != null) {
            spec.addParameter(generateParameterSpecFromVariable(variablesType))
        }

        if (variablesSpec != null) {
            spec.addStatement(
                "return %N(variables = variables)",
                requestBodySpec
            )
        } else {
            spec.addStatement(
                "return %N()",
                requestBodySpec
            )
        }

        spec.addKdoc(
            """
            |Create an instance of [%N] which then you can encode to JSON string
        """.trimMargin(), requestBodySpec
        )

        return spec.build()
    }

    private fun generateSerializerFunction(requestBodySpec: TypeSpec, parentObjectName: String): FunSpec {
        val spec = FunSpec.builder("serializer")
            .returns(KSerializer::class.asTypeName().plusParameter(requestBodySpec.typeNameOrUnit(parentObjectName)))

        spec.addStatement("return Request.serializer()")

        return spec.build()
    }

    private fun TypeSpec?.typeNameOrUnit(parentObjectName: String): TypeName {
        return if (this == null) {
            Unit::class.asTypeName()
        } else {
            ClassName.bestGuess("$parentFQName.$parentObjectName.${name}")
        }
    }
}
