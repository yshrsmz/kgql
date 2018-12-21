package com.codingfeline.kgql.core.compiler

import com.codingfeline.kgql.core.DOCUMENT_WRAPPER_SUFFIX
import com.codingfeline.kgql.core.KgqlFile
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.OperationDefinition
import graphql.language.Type
import graphql.language.VariableDefinition
import graphql.parser.Parser

class DocumentWrapperGenerator(val sourceFile: KgqlFile) {

    val rawDocument = sourceFile.sourceFile.readText()
    val document = Parser().parseDocument(rawDocument)

    fun type(): TypeSpec {
        val capitalizedName = sourceFile.sourceFile.nameWithoutExtension.capitalize()
        val objectType = TypeSpec.objectBuilder("$capitalizedName$DOCUMENT_WRAPPER_SUFFIX")

        println(document)

        // add raw document property
        val documentProp = PropertySpec.builder("document", String::class)
            .addModifiers(KModifier.PRIVATE)
            .initializer("%S", rawDocument)
            .build()

        objectType.addProperty(documentProp)

        val operations = document.definitions.filter { it is OperationDefinition }
            .map { generateOperationFunction(it as OperationDefinition) }

        objectType.addFunctions(operations)

        return objectType.build()
    }

    fun generateOperationFunction(operation: OperationDefinition): FunSpec {
        val spec = FunSpec.builder("${operation.name}${operation.operation.name.toLowerCase().capitalize()}".decapitalize())

        spec.addParameters(operation.variableDefinitions.map { generateParameterSpecFromVariable(it) })

        return spec.build()
    }

    fun generateParameterSpecFromVariable(variable: VariableDefinition): ParameterSpec {
        return ParameterSpec.builder(name = variable.name, type = mapGraphQLType(variable.type))
            .build()
    }
}

fun mapGraphQLType(type: Type<*>): TypeName {
    return when (type) {
        is NonNullType -> mapGraphQLType(type.type).copy(nullable = true)
        is ListType -> ClassName("kotlin.collections", "List")
            .plusParameter(mapGraphQLType(type.type))
        else -> {
            when ((type as graphql.language.TypeName).name) {
                "String" -> String::class.asTypeName()
                "Int" -> Int::class.asTypeName()
                "Float" -> Float::class.asTypeName()
                "Boolean" -> Boolean::class.asTypeName()
                else -> ANY
            }
        }
    }
}
