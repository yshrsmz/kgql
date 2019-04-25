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

        println("schema path: $schemaJson")
        logger.log(LogLevel.INFO, "schema path: $schemaJson")

        val schema = JsonSlurper().parse(schemaJson) as Map<String, Any>
        val enums = getTypes(schema).filter { type -> filterEnum(type as Map<String, Any>) }

        val enumNameSet = enums.map { (it as Map<String, Any>)["name"] as String }.toSet()

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

    private fun getTypes(schema: Map<String, Any>): List<Any> {
        val data = schema["data"] as Map<String, Any>
        val schema = data["__schema"] as Map<String, Any>
        return schema["types"] as List<Any>
    }

    private fun filterEnum(type: Map<String, Any>): Boolean {
        return (type["kind"] as String).toLowerCase() == "ENUM"
    }
}
