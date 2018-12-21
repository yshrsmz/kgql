package com.codingfeline.kgql.core.compiler

import com.codingfeline.kgql.core.KgqlFile
import com.squareup.kotlinpoet.FileSpec
import graphql.parser.Parser
import java.io.Closeable

private typealias FileAppender = (fileName: String) -> Appendable

object KgqlCompiler {

    fun compile(file: KgqlFile, output: FileAppender) {
        writeDocumentWrapperFile(file, output)
        val document = Parser().parseDocument(file.sourceFile.readText())
        println(document)
    }

    fun writeDocumentWrapperFile(sourceFile: KgqlFile, output: FileAppender) {
        val documentWrapperType = DocumentWrapperGenerator(sourceFile).type()
        FileSpec.builder(sourceFile.packageName, sourceFile.sourceFile.nameWithoutExtension)
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
