package com.codingfeline.kgql.gradle

import com.codingfeline.kgql.VERSION
import com.codingfeline.kgql.core.KgqlEnvironment
import com.codingfeline.kgql.core.KgqlException
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class KgqlTask : SourceTask() {
    @Input
    fun pluginVersion() = VERSION

    @get:OutputDirectory
    var outputDirectory: File? = null

    lateinit var sourceFolders: Iterable<File>
    lateinit var packageName: String

    @TaskAction
    fun generateKgqlFiles() {
        println("generateKgqlFiles")
        outputDirectory?.deleteRecursively()

        val environment = KgqlEnvironment(
            sourceFolders = sourceFolders.filter { it.exists() },
            sourceFiles = getSource().toList(),
            packageName = packageName,
            outputDirectory = outputDirectory!!
        )

        println("includes: ${includes}")
        println("source: ${getSource().map { it.toString() }}")

        logger.log(LogLevel.INFO, "KgqlTask-sourceFolders: $sourceFolders")
        logger.log(LogLevel.INFO, "kgqlTask-packageName: $packageName")
        logger.log(LogLevel.INFO, "kgqlTask-outputDirectory: $outputDirectory")

        val generationStatus = environment.generateKgqlFiles { info -> logger.log(LogLevel.INFO, info) }

        when (generationStatus) {
            is KgqlEnvironment.CompilationStatus.Failure -> {
                logger.log(LogLevel.ERROR, "")
                generationStatus.errors.forEach { logger.log(LogLevel.ERROR, it) }
                throw KgqlException("Generation failed; see the generator error output for details.")
            }
        }
    }
}
