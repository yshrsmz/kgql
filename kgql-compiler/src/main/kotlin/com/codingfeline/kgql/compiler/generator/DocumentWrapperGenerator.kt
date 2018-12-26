package com.codingfeline.kgql.compiler.generator

import com.codingfeline.kgql.compiler.GraphQLCustomTypeFQName
import com.codingfeline.kgql.compiler.GraphQLCustomTypeName
import com.codingfeline.kgql.compiler.KgqlCustomTypeMapper
import com.codingfeline.kgql.compiler.KgqlFile
import com.codingfeline.kgql.core.KgqlRequestBody
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import graphql.language.OperationDefinition
import graphql.parser.Parser

private const val PARAM_VARIABLES_NAME = "variables"

class DocumentWrapperGenerator(
    val sourceFile: KgqlFile,
    typeMap: Map<GraphQLCustomTypeName, GraphQLCustomTypeFQName>
) {

    val rawDocument = sourceFile.source.readText()
    val document = Parser().parseDocument(rawDocument)
    val typeMapper = KgqlCustomTypeMapper(typeMap)

    val className = "${sourceFile.source.nameWithoutExtension.capitalize()}DocumentWrapper"

    fun type(): TypeSpec {
        val objectType = TypeSpec.objectBuilder(className)

        // add raw document property
        val documentProp = PropertySpec.builder("document", String::class)
            .addModifiers(KModifier.PRIVATE)
            .initializer("%S", rawDocument)
            .build()

        objectType.addProperty(documentProp)

        val operations = document.definitions.filter { it is OperationDefinition }
            .map { it as OperationDefinition }

        val variablesMap = operations.filter { it.variableDefinitions.isNotEmpty() }
            .associateBy({ it.name }, { generateVariableWrapper(it) })
        objectType.addTypes(variablesMap.values)

        objectType.addFunctions(operations.map { generateOperationFunction(it, documentProp, variablesMap[it.name]) })

        return objectType.build()
    }

    fun generateOperationFunction(operation: OperationDefinition, documentProp: PropertySpec, variablesSpec: TypeSpec?): FunSpec {
        val spec = FunSpec.builder(
            "${operation.name ?: ""}${operation.operation.name.toLowerCase().capitalize()}".decapitalize())
            .returns(returnType = KgqlRequestBody::class)

        if (variablesSpec != null) {
            spec.addParameter(generateParameterSpecFromVariable(variablesSpec))
        }

        val operationName = if (operation.name != null) {
            "\"${operation.name}\""
        } else "null"

        val variablesLiteral = if (variablesSpec != null) {
            PARAM_VARIABLES_NAME
        } else "null"

        spec.addStatement(
            "return KgqlRequestBody(operationName=%L, query=%N, variables=%L)",
            operationName,
            documentProp,
            variablesLiteral)

        return spec.build()
    }

    fun generateParameterSpecFromVariable(variables: TypeSpec): ParameterSpec {
        return ParameterSpec.builder(
            name = PARAM_VARIABLES_NAME,
            type = ClassName.bestGuess("${sourceFile.packageName}.$className.${variables.name}"))
            .build()
    }

    fun generateVariableWrapper(operation: OperationDefinition): TypeSpec {
        return VariableWrapperGenerator(operation.name, operation.variableDefinitions, typeMapper).type()
    }
}

