package com.codingfeline.kgql.core.compiler

import java.io.File

private typealias FileAppender = (fileName: String) -> Appendable

object KgqlCompiler {

    fun compile(file: File, output: FileAppender) {
    }

    fun writeDocumentWrapperFile(sourceFile: File, output: FileAppender) {
    }
}
