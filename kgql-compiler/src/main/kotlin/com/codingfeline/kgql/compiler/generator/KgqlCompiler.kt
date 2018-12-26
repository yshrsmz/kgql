package com.codingfeline.kgql.compiler.generator

import com.codingfeline.kgql.compiler.GraphQLCustomTypeFQName
import com.codingfeline.kgql.compiler.GraphQLCustomTypeName
import com.codingfeline.kgql.compiler.KgqlFile
import com.squareup.kotlinpoet.FileSpec
import graphql.parser.Parser
import java.io.Closeable

private typealias FileAppender = (fileName: String) -> Appendable

object KgqlCompiler {

    fun compile(file: KgqlFile, typeMap: Map<GraphQLCustomTypeName, GraphQLCustomTypeFQName>, output: FileAppender) {
        writeDocumentWrapperFile(file, typeMap, output)
        val document = Parser().parseDocument(file.source.readText())
        println(document)
    }

    fun writeDocumentWrapperFile(sourceFile: KgqlFile, typeMap: Map<GraphQLCustomTypeName, GraphQLCustomTypeFQName>, output: FileAppender) {
        val documentWrapperType = DocumentWrapperGenerator(sourceFile, typeMap).type()
        FileSpec.builder(sourceFile.packageName, sourceFile.source.nameWithoutExtension)
            .apply {
                addType(documentWrapperType)
            }
            .build()
            .writeToAndClose(output("${sourceFile.outputDirectory.absolutePath}/${documentWrapperType.name}.kt"))
    }

    private fun FileSpec.writeToAndClose(appendable: Appendable) {
        writeTo(appendable)
        if (appendable is Closeable) appendable.close()
    }
}
