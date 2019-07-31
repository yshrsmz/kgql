package com.codingfeline.kgql.gradle

import com.codingfeline.kgql.VERSION
import com.codingfeline.kgql.compiler.KgqlEnvironment
import com.codingfeline.kgql.compiler.KgqlEnvironment.CompilationStatus.Failure
import com.codingfeline.kgql.compiler.KgqlException
import groovy.json.JsonSlurper
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

    @Input
    lateinit var sourceFolders: Iterable<File>

    @Input
    lateinit var packageName: String

    @Input
    lateinit var typeMap: MutableMap<String, String>

    @Input
    lateinit var schemaJson: File

    @TaskAction
    fun generateKgqlFiles() {
        outputDirectory?.deleteRecursively()
        outputDirectory?.mkdirs()

        logger.log(LogLevel.INFO, "schema path: $schemaJson")

        val enumNameSet = extractEnumNameSet(schemaJson)

        val environment = KgqlEnvironment(
            sourceFiles = source.toList(),
            packageName = packageName,
            outputDirectory = outputDirectory,
            typeMap = typeMap,
            enumNameSet = enumNameSet
        )

        val generationStatus = environment.generateKgqlFiles { info -> logger.log(LogLevel.INFO, info) }

        when (generationStatus) {
            is Failure -> {
                logger.log(LogLevel.ERROR, "")
                generationStatus.errors.forEach { logger.log(LogLevel.ERROR, it) }
                throw KgqlException("Generation failed; see the generator error output for details.")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getTypes(schemaJson: Map<String, Any>): List<Map<String, Any>> {
        val data = schemaJson["data"] as Map<String, Any>
        val schema = data["__schema"] as Map<String, Any>
        return schema["types"] as List<Map<String, Any>>
    }

    private fun filterEnum(type: Map<String, Any>): Boolean {
        return (type["kind"] as String).toUpperCase() == "ENUM"
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractEnumNameSet(schemaFile: File): Set<String> {
        val schemaJson = JsonSlurper().parse(schemaFile) as Map<String, Any>
        val enums = getTypes(schemaJson).filter(::filterEnum)
        return enums.map { it["name"] as String }.toSet()
    }
}
