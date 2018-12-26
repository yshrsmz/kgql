package com.codingfeline.kgql.compiler

import com.codingfeline.kgql.compiler.generator.KgqlCompiler
import java.io.File

class KgqlEnvironment(
    /**
     * The GraphQL source directories for this environment
     */
    private val sourceFolders: List<File>,

    private val sourceFiles: List<File>,
    /**
     * The package name to be used for generated KgqlDocuments class.
     */
    private val packageName: String? = null,
    /**
     * An output directory to place the generated class files
     */
    private val outputDirectory: File? = null,
    private val typeMap: Map<GraphQLCustomTypeName, GraphQLCustomTypeFQName>
) {

    sealed class CompilationStatus {
        class Success : CompilationStatus()
        class Failure(val errors: List<String>) : CompilationStatus()
    }

    fun generateKgqlFiles(logger: (String) -> Unit): CompilationStatus {
        val errors = ArrayList<String>()

        val writer = writer@{ fileName: String ->
            val file = File(fileName)
            if (!file.exists()) {
                file.parentFile.mkdirs()
                file.createNewFile()
            }
            return@writer file.writer()
        }

        sourceFiles.forEach {
            val file = KgqlFile(
                packageName = packageName!!,
                outputDirectory = outputDirectory!!,
                source = it)
            KgqlCompiler.compile(file, typeMap, writer)
        }

        return CompilationStatus.Success()
    }
}