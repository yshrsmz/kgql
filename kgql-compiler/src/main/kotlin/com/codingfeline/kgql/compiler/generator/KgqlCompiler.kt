package com.codingfeline.kgql.compiler.generator

import com.codingfeline.kgql.compiler.GraphQLCustomTypeFQName
import com.codingfeline.kgql.compiler.GraphQLCustomTypeName
import com.codingfeline.kgql.compiler.KgqlFile
import com.codingfeline.kgql.compiler.Logger
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import kotlinx.serialization.SerialName
import java.io.Closeable

private typealias FileAppender = (fileName: String) -> Appendable

object KgqlCompiler {

    fun compile(
        file: KgqlFile,
        typeMap: Map<GraphQLCustomTypeName, GraphQLCustomTypeFQName>,
        output: FileAppender,
        logger: Logger
    ) {
        writeDocumentWrapperFile(file, typeMap, output, logger)
    }

    fun writeDocumentWrapperFile(
        sourceFile: KgqlFile,
        typeMap: Map<GraphQLCustomTypeName, GraphQLCustomTypeFQName>,
        output: FileAppender,
        logger: Logger
    ) {
        val documentWrapperType = DocumentWrapperGenerator(sourceFile, typeMap).generateType(logger)
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

fun generateSerialName(name: String): AnnotationSpec {
    return AnnotationSpec.builder(SerialName::class)
        .addMember("value = %S", name)
        .build()
}
