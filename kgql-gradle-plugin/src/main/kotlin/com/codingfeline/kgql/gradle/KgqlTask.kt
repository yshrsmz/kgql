package com.codingfeline.kgql.gradle

import com.codingfeline.kgql.VERSION
import com.codingfeline.kgql.compiler.KgqlEnvironment
import com.codingfeline.kgql.compiler.KgqlEnvironment.CompilationStatus.Failure
import com.codingfeline.kgql.compiler.KgqlException
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class KgqlTask : SourceTask() {
    @Suppress("unused")
    @Input
    val pluginVersion = VERSION

    @get:OutputDirectory
    lateinit var outputDirectory: File

    @Input
    lateinit var sourceFolders: Iterable<File>

    @Input
    lateinit var packageName: String

    @Input
    lateinit var typeMap: MutableMap<String, String>

    @TaskAction
    fun generateKgqlFiles() {
        outputDirectory.deleteRecursively()
        outputDirectory.mkdirs()

        val environment = KgqlEnvironment(
            sourceFiles = source.toList(),
            packageName = packageName,
            outputDirectory = outputDirectory,
            typeMap = typeMap
        )

        val generationStatus = environment.generateKgqlFiles { info -> logger.log(LogLevel.INFO, info) }

        when (generationStatus) {
            is Failure -> {
                logger.log(LogLevel.ERROR, "")
                generationStatus.errors.forEach { logger.log(LogLevel.ERROR, it) }
                throw KgqlException("Generation failed; see the generator error output for details.")
            }
            KgqlEnvironment.CompilationStatus.Success -> {
                // no-op
            }
        }
    }
}
