package com.codingfeline.kgql.gradle

import com.codingfeline.kgql.compiler.KgqlFileType
import com.codingfeline.kgql.gradle.kotlin.sources
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import java.io.File

class KgqlConfig(
    val project: Project,
    var packageName: String? = null,
    var sourceSet: FileCollection? = null,
    var typeMapper: MutableMap<String, String>? = null
) {
    private val generatedSourceDirectory
        get() = File(project.buildDir, "generated/kgql")

    private val sources by lazy { sources() }

    internal fun registerTask() {

        val packageName = requireNotNull(packageName) { "property packageName must be provided" }
        val sourceSet = sourceSet ?: project.files("src/main/kgql")
        val typeMap = typeMapper ?: mutableMapOf()

        sources.forEach { source ->
//            println(source)
            // Add source dependency on the generated code.

            val task = project.tasks.register(
                "generate${source.name.capitalize()}KgqlInterface",
                KgqlTask::class.java
            ) {
                it.packageName = packageName
                it.sourceFolders = sourceSet.files
                it.outputDirectory = generatedSourceDirectory
                it.typeMap = typeMap
                it.source(sourceSet)
                it.include(KgqlFileType.EXTENSIONS.map { ext -> listOf("**", "*.$ext").joinToString(File.separator) })
                it.group = KgqlPlugin.GROUP
                it.description = "Generate Kotlin interface for .gql/.graphql files"
            }

            project.tasks.named("generateKgqlInterface").configure { it.dependsOn(task) }

            source.sourceDirectorySet.srcDirs(task.map { it.outputDirectory })
        }
    }
}