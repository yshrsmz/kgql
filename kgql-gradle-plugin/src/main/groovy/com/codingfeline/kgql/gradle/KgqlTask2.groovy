package com.codingfeline.kgql.gradle

import com.codingfeline.kgql.VersionKt
import com.codingfeline.kgql.compiler.KgqlEnvironment
import com.codingfeline.kgql.compiler.KgqlException
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

class KgqlTask2 extends SourceTask {
    @Input
    String pluginVersion() {
        return VersionKt.getVERSION()
    }

    @OutputDirectory
    File outputDirectory = null

    Iterable<File> sourceFolders
    String packageName
    Map<String, String> typeMap

    @TaskAction
    void generateKgqlFiles() {
        println("generateKgqlFiles")
        outputDirectory?.deleteDir()
        outputDirectory.mkdirs()

        def environment = new KgqlEnvironment(
                sourceFolders.findAll { it.exists() }.toList(),
                getSource().toList(),
                packageName,
                outputDirectory,
                typeMap
        )

        def generationStatus = environment.generateKgqlFiles { info -> logger.log(LogLevel.INFO, info) }

        switch (generationStatus) {
            case KgqlEnvironment.CompilationStatus.Failure:
                logger.log(LogLevel.ERROR, "")
                (generationStatus as KgqlEnvironment.CompilationStatus.Failure).errors.forEach {
                    logger.log(LogLevel.ERROR, it)
                }
                throw new KgqlException("Generation failed; see the generator error output for details.")
                break

        }
    }
}
