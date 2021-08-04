package com.codingfeline.kgql.compiler.generator

import com.codingfeline.kgql.compiler.GraphQLCustomTypeFQName
import com.codingfeline.kgql.compiler.GraphQLCustomTypeName
import com.codingfeline.kgql.compiler.KgqlCustomTypeMapper
import com.codingfeline.kgql.compiler.KgqlFile
import com.codingfeline.kgql.compiler.Logger
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import graphql.language.OperationDefinition
import graphql.parser.Parser

class DocumentWrapperGenerator(
    private val sourceFile: KgqlFile,
    typeMap: Map<GraphQLCustomTypeName, GraphQLCustomTypeFQName>
) {

    private val rawDocument = sourceFile.source.readText()
    private val document = Parser().parseDocument(rawDocument)
    private val typeMapper = KgqlCustomTypeMapper(typeMap)

    private val className = "${sourceFile.source.nameWithoutExtension.capitalize()}Document"

    fun generateType(logger: Logger): TypeSpec {
        logger("Generating $className...")

        val objectType = TypeSpec.objectBuilder(className)
            .addModifiers(KModifier.INTERNAL)

        val fqName = "${sourceFile.packageName}.$className"

        // add raw document property
        val documentProp = PropertySpec.builder("document", String::class)
            .addModifiers(KModifier.PRIVATE)
            .initializer("%S", rawDocument)
            .build()

        objectType.addProperty(documentProp)

        val operationWrapperGenerator = OperationWrapperGenerator(documentProp, typeMapper, fqName)
        val operations = document.definitions.filterIsInstance<OperationDefinition>()
            .map { operationWrapperGenerator.generateObject(it) }

        objectType.addTypes(operations)

        return objectType.build()
    }
}

