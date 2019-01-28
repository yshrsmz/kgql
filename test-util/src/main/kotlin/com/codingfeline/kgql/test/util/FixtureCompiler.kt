package com.codingfeline.kgql.test.util

import com.codingfeline.kgql.compiler.GraphQLCustomTypeName
import com.codingfeline.kgql.compiler.GraphQLCustomTypeFQName
import com.codingfeline.kgql.compiler.KgqlFile
import com.codingfeline.kgql.compiler.Logger
import com.codingfeline.kgql.compiler.generator.KgqlCompiler
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FilenameFilter

private typealias CompilationMethod = (KgqlFile, Map<GraphQLCustomTypeName, GraphQLCustomTypeFQName>, (String) -> Appendable, (String) -> Unit) -> Unit

object FixtureCompiler {

    fun compileGql(
        gql: String,
        temporaryFolder: TemporaryFolder,
        compilationMethod: CompilationMethod = KgqlCompiler::compile,
        typeMap: Map<GraphQLCustomTypeName, GraphQLCustomTypeFQName> = emptyMap(),
        fileName: String = "Test.gql"
    ): CompilationResult {
        writeGql(gql, temporaryFolder, fileName)
        return compileFixture(temporaryFolder.fixtureRoot().path, compilationMethod, typeMap)
    }

    fun writeGql(
        gql: String,
        temporaryFolder: TemporaryFolder,
        fileName: String
    ): File {
        val srcRootDir = temporaryFolder.fixtureRoot().apply { mkdirs() }
        val fixtureSrcDir = File(srcRootDir, "com/example").apply { mkdirs() }
        return File(fixtureSrcDir, fileName).apply {
            createNewFile()
            writeText(gql)
        }
    }

    fun compileFixture(
        fixtureRoot: String,
        compilationMethod: CompilationMethod,
        typeMap: Map<GraphQLCustomTypeName, GraphQLCustomTypeFQName> = emptyMap(),
        writer: ((String) -> Appendable)? = null,
        outputDirectory: File = File(fixtureRoot, "output")
    ): CompilationResult {
        val compilerOutput = mutableMapOf<File, StringBuilder>()
        val errors = mutableListOf<String>()
        val sourceFiles = StringBuilder()
        val parser = TestEnvironment(outputDirectory)
        val fixtureRootDir = File(fixtureRoot)

        if (!fixtureRootDir.exists()) {
            throw IllegalArgumentException("$fixtureRoot does not exist")
        }

        val environment = parser.build(fixtureRootDir.path)
        val fileWriter = writer ?: fileWriter@{ fileName: String ->
            val builder = StringBuilder()
            compilerOutput += File(fileName) to builder
            return@fileWriter builder
        }

        val logger: Logger = {}

        var file: KgqlFile? = null

        environment.forEachSourceFile {
            compilationMethod(it, typeMap, fileWriter, logger)
            file = it
        }

        return CompilationResult(outputDirectory, compilerOutput, errors, sourceFiles.toString(), file!!)
    }

    data class CompilationResult(
        val outputDirectory: File,
        val compilerOutput: Map<File, StringBuilder>,
        val errors: List<String>,
        val sourceFiles: String,
        val compiledFile: KgqlFile
    )
}

fun TemporaryFolder.fixtureRoot() = File(root, "src/test/test-fixture")
