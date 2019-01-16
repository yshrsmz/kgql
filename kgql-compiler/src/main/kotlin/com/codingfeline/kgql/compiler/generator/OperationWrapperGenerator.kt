package com.codingfeline.kgql.compiler.generator

import com.codingfeline.kgql.compiler.KgqlCustomTypeMapper
import com.codingfeline.kgql.core.KgqlRequestBody
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import graphql.language.OperationDefinition
import kotlinx.serialization.KSerializer
import kotlinx.serialization.internal.UnitSerializer

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

        if (hasVariables) {
            variableSpec = generateVariable(operation)
            objectSpec.addType(variableSpec)
        }

        val operationFunSpec = generateOperationFunction(
            operation = operation,
            objectName = name,
            documentProp = documentProp,
            variablesSpec = variableSpec
        )
        objectSpec.addFunction(operationFunSpec)
        objectSpec.addFunction(generateSerializerFunction(operationFunSpec))

        return objectSpec.build()
    }

    private fun generateVariable(operation: OperationDefinition): TypeSpec {
        return VariableWrapperGenerator(operation.variableDefinitions, typeMapper).generateType()
    }

    private fun generateParameterSpecFromVariable(variables: TypeName): ParameterSpec {
        return ParameterSpec.builder(
            name = PARAM_VARIABLES_NAME,
            type = variables
        )
            .build()
    }

    private fun generateOperationFunction(
        operation: OperationDefinition,
        objectName: String,
        documentProp: PropertySpec,
        variablesSpec: TypeSpec?
    ): FunSpec {
        val variablesType: TypeName = if (variablesSpec == null) {
            Unit::class.asTypeName()
        } else {
            ClassName.bestGuess("$parentFQName.$objectName.${variablesSpec.name}")
        }

        // function name could be `query`, `mutation` or `subscription`
        val spec = FunSpec.builder(operation.operation.name.toLowerCase())
            .returns(KgqlRequestBody::class.asTypeName().plusParameter(variablesType))

        if (variablesSpec != null) {
            spec.addParameter(generateParameterSpecFromVariable(variablesType))
        }

        val operationName = if (operation.name != null) {
            "\"${operation.name}\""
        } else "null"

        val variablesLiteral = if (variablesSpec != null) {
            PARAM_VARIABLES_NAME
        } else "null"

        spec.addStatement(
            "return KgqlRequestBody<%T>(operationName=%L, query=%N, variables=%L)",
            variablesType,
            operationName,
            documentProp,
            variablesLiteral
        )

        return spec.build()
    }

    private fun generateSerializerFunction(operationFunSpec: FunSpec): FunSpec {
        val spec = FunSpec.builder("serializer")
            .returns(KSerializer::class.asTypeName().plusParameter(operationFunSpec.returnType!!))

        val serializerLiteral = operationFunSpec.parameters.firstOrNull()?.let { "${it.type}.serializer()" }
            ?: kotlin.run { UnitSerializer::class.asTypeName().toString() }

        spec.addStatement(
            "return KgqlRequestBody.serializer(%L)",
            serializerLiteral
        )

        return spec.build()
    }
}
