package com.codingfeline.kgql.core.compiler

import com.codingfeline.kgql.core.*
import com.squareup.kotlinpoet.*
import graphql.language.OperationDefinition
import graphql.language.VariableDefinition
import graphql.parser.Parser

class DocumentWrapperGenerator(
    val sourceFile: KgqlFile,
    typeMap: Map<GraphQLCustomTypeName, GraphQLCustomTypeFQName>
) {

    val rawDocument = sourceFile.sourceFile.readText()
    val document = Parser().parseDocument(rawDocument)
    val typeMapper = KgqlCustomTypeMapper(typeMap)

    fun type(): TypeSpec {
        val capitalizedName = sourceFile.sourceFile.nameWithoutExtension.capitalize()
        val objectType = TypeSpec.objectBuilder("$capitalizedName$DOCUMENT_WRAPPER_SUFFIX")

        // add raw document property
        val documentProp = PropertySpec.builder("document", String::class)
            .addModifiers(KModifier.PRIVATE)
            .initializer("%S", rawDocument)
            .build()

        objectType.addProperty(documentProp)

        val operations = document.definitions.filter { it is OperationDefinition }
            .map { generateOperationFunction(it as OperationDefinition, documentProp) }

        objectType.addFunctions(operations)

        return objectType.build()
    }

    fun generateOperationFunction(operation: OperationDefinition, documentProp: PropertySpec): FunSpec {
        val spec = FunSpec.builder("${operation.name}${operation.operation.name.toLowerCase().capitalize()}".decapitalize())
                .returns(returnType = KgqlRequestBody::class)

        spec.addParameters(operation.variableDefinitions.map { generateParameterSpecFromVariable(it) })

        val operationName = if (operation.name != null) {
            "\"${operation.name}\""
        } else "null"

        spec.addStatement(
                "return KgqlRequestBody(operationName=%L, query=%N, variables=null)",
                operationName,
                documentProp)

        return spec.build()
    }

    fun generateParameterSpecFromVariable(variable: VariableDefinition): ParameterSpec {
        return ParameterSpec.builder(name = variable.name, type = typeMapper.get(variable.type))
            .build()
    }
}

