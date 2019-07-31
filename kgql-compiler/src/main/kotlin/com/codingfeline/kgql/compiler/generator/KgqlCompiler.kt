package com.codingfeline.kgql.compiler.generator

import com.codingfeline.kgql.compiler.KgqlCustomTypeMapper
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
        typeMapper: KgqlCustomTypeMapper,
        output: FileAppender,
        logger: Logger
    ) {
        writeDocumentWrapperFile(file, typeMapper, output, logger)
    }

    fun writeDocumentWrapperFile(
        sourceFile: KgqlFile,
        typeMapper: KgqlCustomTypeMapper,
        output: FileAppender,
        logger: Logger
    ) {
        val packageName = sourceFile.packageName
        val outputDirectory = "${sourceFile.outputDirectory.absolutePath}/${packageName.replace(".", "/")}"
        val documentWrapperType = DocumentWrapperGenerator(sourceFile, typeMapper).generateType(logger)
        FileSpec.builder(sourceFile.packageName, sourceFile.source.nameWithoutExtension)
            .apply {
                addType(documentWrapperType)
            }
            .build()
            .writeToAndClose(output("$outputDirectory/${documentWrapperType.name}.kt"))
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
