package com.codingfeline.kgql.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.errors.SyncIssueHandlerImpl
import com.android.build.gradle.options.SyncOptions
import com.android.builder.core.DefaultManifestParser
import com.codingfeline.kgql.compiler.KgqlFileType
import com.codingfeline.kgql.compiler.KgqlPropertiesFile
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper
import java.io.File

class KgqlPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        println("apply")
        val extension = project.extensions.create("kgql", KgqlExtension::class.java)

        var kotlin = false
        var android = false

        project.plugins.all {
            when (it) {
                is KotlinBasePluginWrapper -> {
                    kotlin = true
                }
                is BasePlugin<*> -> {
                    android = true
                }
            }
        }

        val hasKotlinAndroidPlugin = project.plugins.run {
            hasPlugin("com.android.application") ||
                hasPlugin("com.android.library") &&
                hasPlugin("org.jetbrains.kotlin.android")
        }
        if (hasKotlinAndroidPlugin) {
            // The kotlin plugin does it's own magic after evaluate, but it needs to know about our
            // generated code. So run Now instead of after evaluations
            configureAndroid(project, extension)
            return
        }

        project.afterEvaluate {
            if (!kotlin) {
                throw IllegalStateException("Kgql Gradle Plugin applied in project '${project.path}' but no supported Kotlin plugin was found")
            }
            val isMultiplatform = project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")
            if (android && !isMultiplatform) {
                configureAndroid(project, extension)
            } else {
                configureKotlin(project, extension, isMultiplatform)
            }
        }
    }

    private fun configureKotlin(project: Project, extension: KgqlExtension, isMultiplatform: Boolean) {
    }

    private fun configureAndroid(project: Project, extension: KgqlExtension) {
        val variants: DomainObjectSet<out BaseVariant> = when {
            project.plugins.hasPlugin("com.android.application") -> {
                project.extensions.getByType(AppExtension::class.java).applicationVariants
            }
            project.plugins.hasPlugin("com.android.library") -> {
                project.extensions.getByType(LibraryExtension::class.java).libraryVariants
            }
            else -> {
                throw IllegalStateException("Unknown Android plugin in project ${project.path}")
            }
        }
        configureAndroid(project, extension, variants)
    }

    private fun configureAndroid(project: Project, extension: KgqlExtension, variants: DomainObjectSet<out BaseVariant>) {
//        val apiDeps = project.configurations.getByName("api").dependencies

        var packageName: String? = null
        val sourceSets = mutableListOf<List<String>>()
        val buildDirectory = listOf("generated", "source", "kgql").fold(project.buildDir, ::File)

        variants.all { variant ->
            val taskName = "generate${variant.name.capitalize()}KgqlInterface"
            val taskProvider = project.tasks.register(taskName, KgqlTask::class.java) { task ->
                task.apply {
                    group = "kgql"
                    outputDirectory = buildDirectory
                    description = "Generate Android interfaces for working with GraphQL documents"
                    source(variant.sourceSets.map { "src/${it.name}/${KgqlFileType.FOLDER_NAME}" })
                    include("**${File.separatorChar}*.${KgqlFileType.EXTENSION}")
                    this.packageName = variant.packageName(project)
                    sourceFolders = variant.sourceSets.map { File("${project.projectDir}/src/${it.name}/${KgqlFileType.FOLDER_NAME}") }
                    sourceSets.add(sourceFolders.map { it.toRelativeString(project.projectDir) })
                    packageName = this.packageName
                }
            }
            variant.registerJavaGeneratingTask(taskProvider.get(), taskProvider.get().outputDirectory)
        }

        project.afterEvaluate {
            val ideaDir = File(project.rootDir, ".idea")
            if (ideaDir.exists()) {
                val propsDir = File(ideaDir, "kgql/${project.projectDir.toRelativeString(project.rootDir)}")
                propsDir.mkdirs()

                val properties = KgqlPropertiesFile(
                    packageName = packageName!!,
                    sourceSets = sourceSets,
                    outputDirectory = buildDirectory.toRelativeString(project.projectDir)
                )
                properties.toFile(File(propsDir, KgqlPropertiesFile.NAME))
            }
        }
    }

    private fun BaseVariant.packageName(project: Project): String {
        return sourceSets.map { it.manifestFile }
            .filter { it.exists() }
            .mapNotNull {
                DefaultManifestParser(it, { true }, SyncIssueHandlerImpl(SyncOptions.EvaluationMode.STANDARD, project.logger)).`package`
            }
            .first()
    }
}
