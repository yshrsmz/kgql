package com.codingfeline.kgql.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.codingfeline.kgql.VERSION
import com.codingfeline.kgql.compiler.KgqlFileType
import com.codingfeline.kgql.gradle.android.packageName
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.HasConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeOutputKind
import org.jetbrains.kotlin.gradle.plugin.sources.DefaultKotlinSourceSet
import org.jetbrains.kotlinx.serialization.gradle.SerializationGradleSubplugin
import java.io.File

@Suppress("unused")
open class KgqlPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create("kgql", KgqlExtension::class.java)

        var kotlin = false
        var serialization = false
        var android = false

        target.plugins.all {
            when (it) {
                is KotlinBasePluginWrapper -> kotlin = true
                is SerializationGradleSubplugin -> serialization = true
                is BasePlugin<*> -> android = true
            }
        }

        val hasAndroidAndKotlinAndSerializationPlugin = (target.plugins.hasPlugin("com.android.application") ||
                target.plugins.hasPlugin("com.android.library")) &&
                target.plugins.hasPlugin("org.jetbrains.kotlin.android") &&
                target.plugins.hasPlugin("org.jetbrains.kotlin.plugin.serialization")

        if (hasAndroidAndKotlinAndSerializationPlugin) {
            // The kotlin plugin does it's own magic after evaluate, but it needs to know about our
            // generated code. So run Now instead of after evaluations
            configureAndroid(target, extension)
            return
        }

        target.afterEvaluate { p ->
            if (!kotlin) {
                throw IllegalStateException("Kgql Gradle Plugin applied in project '${p.path}' but no supported Kotlin plugin was found")
            }
            if (!serialization) {
                throw IllegalStateException("Kgql Gradle Plugin applied in project '${p.path}' but no kotlinx-serialization plugin was found")
            }
            val isMultiplatform = p.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")
            if (android && !isMultiplatform) {
                configureAndroid(p, extension)
            } else {
                configureKotlin(p, extension, isMultiplatform)
            }
        }
    }

    private fun configureKotlin(project: Project, extension: KgqlExtension, isMultiplatform: Boolean) {
        val outputDirectory = File(project.buildDir, "kgql")
        val logger = project.logger

        val kotlinSrc = if (isMultiplatform) {
            val sourceSets = project.extensions.getByType(KotlinMultiplatformExtension::class.java).sourceSets
            val sourceSet = (sourceSets.getByName("commonMain") as DefaultKotlinSourceSet)
            project.configurations.getByName(sourceSet.apiConfigurationName).dependencies.add(
                project.dependencies.create("com.codingfeline.kgql:core:${VERSION}")
            )
            sourceSet.kotlin
        } else {
            val sourceSets = project.property("sourceSets") as SourceSetContainer
            sourceSets.getByName("main").kotlin!!
        }
        kotlinSrc.srcDirs(outputDirectory.toRelativeString(project.projectDir))

        project.afterEvaluate { p ->
            val packageName = requireNotNull(extension.packageName) { "property packageName must be provided" }
            val sourceSet = extension.sourceSet ?: p.files("src/main/kgql")
            val typeMap = extension.typeMapper ?: mutableMapOf()

            val task = p.tasks.register("generateKgqlInterface", KgqlTask::class.java) {
                it.packageName = packageName
                it.sourceFolders = sourceSet.files
                it.outputDirectory = outputDirectory
                it.typeMap = typeMap
                it.source(sourceSet)
                it.include(KgqlFileType.EXTENSIONS.map { ext -> "**${File.separatorChar}*.$ext" })
                it.group = "kgql"
                it.description = "Generate Kotlin interface for .gql files"
            }

            if (isMultiplatform) {
                p.extensions.getByType(KotlinMultiplatformExtension::class.java).targets.forEach { target ->
                    logger.debug("target: $target")
                    target.compilations.forEach { compilationUnit ->
                        logger.debug("compilation: $compilationUnit")
                        if (compilationUnit is KotlinNativeCompilation) {
                            // Honestly the way native compiles kotlin seems completely arbitrary and some order
                            // of the following tasks, so just set the dependency for all of them and let gradle
                            // figure it out.

                            p.tasks.named(compilationUnit.compileAllTaskName).configure {
                                logger.debug("task to depend found1: $it.name")
                                it.dependsOn(task)
                            }
                            p.tasks.named(compilationUnit.compileKotlinTaskName).configure {
                                logger.debug("task to depend found2: $it.name")
                                it.dependsOn(task)
                            }

                            // for 1.3.0-style target config
                            NativeOutputKind.values().forEach { kind ->
                                NativeBuildType.values().forEach { buildType ->
                                    @Suppress("DEPRECATION") val t = compilationUnit.findLinkTask(kind, buildType)
                                    if (t != null) {
                                        logger.debug("task to depend found3: $t.name")
                                        t.dependsOn(task)
                                    }
                                }
                            }

                            // for 1.3.20-style target config
                            compilationUnit.target.binaries.forEach { binary ->
                                p.tasks.named(binary.linkTask.name).configure {
                                    logger.debug("task to depend found4: $it.name")
                                    it.dependsOn(task)
                                }
                            }
                        } else {
                            p.tasks.named(compilationUnit.compileKotlinTaskName).configure {
                                logger.debug("task to depend found5: $it.name")
                                it.dependsOn(task)
                            }
                        }
                    }
                }
            } else {
                p.tasks.named("compileKotlin").configure {
                    logger.debug("task to depend found6: $it.name")
                    it.dependsOn(task)
                }
            }
        }
    }

    private fun configureAndroid(project: Project, extension: KgqlExtension) {
        val variants: DomainObjectSet<out BaseVariant> = when {
            project.plugins.hasPlugin("com.android.application") -> {
                project.extensions.getByType(AppExtension::class.java)
                    .applicationVariants
            }
            project.plugins.hasPlugin("com.android.library") -> {
                project.extensions.getByType(LibraryExtension::class.java)
                    .libraryVariants
            }
            else -> {
                throw IllegalStateException("Unknown Android plugin in project '${project.path}'")
            }
        }
        configureAndroid(project, extension, variants)
    }

    private fun configureAndroid(
        project: Project,
        extension: KgqlExtension,
        variants: DomainObjectSet<out BaseVariant>
    ) {
        val apiDeps = project.configurations.getByName("api").dependencies
        apiDeps.add(project.dependencies.create("com.codingfeline.kgql:core-jvm:$VERSION"))

        val sourceSets = mutableListOf<List<String>>()
        val buildDirectory = listOf("generated", "source", "kgql").fold(project.buildDir, ::File)
        val typeMap = extension.typeMapper ?: mutableMapOf()

        variants.all { variant ->
            val taskName = "generate${variant.name.capitalize()}KgqlInterface"
            val taskProvider = project.tasks.register(taskName, KgqlTask::class.java) { task ->
                task.group = "kgql"
                task.outputDirectory = buildDirectory
                task.typeMap = typeMap
                task.description = "Generate Android interface for working with GraphQL documents"
                task.source(variant.sourceSets.map { "src/${it.name}/${KgqlFileType.FOLDER_NAME}" })
                task.include(KgqlFileType.EXTENSIONS.map { ext -> "**${File.separatorChar}*.$ext" })
                task.packageName = project.packageName()
                task.sourceFolders =
                    variant.sourceSets.map { File("${project.projectDir}/src/${task.name}/${KgqlFileType.FOLDER_NAME}") }
                sourceSets.add(task.sourceFolders.map { it.toRelativeString(project.projectDir) })
            }
            // TODO Use task configuration avoidance once released. https://issuetracker.google.com/issues/117343589
            variant.registerJavaGeneratingTask(taskProvider.get(), taskProvider.get().outputDirectory)
        }
    }

    // Copied from kotlin plugin
    private val SourceSet.kotlin: SourceDirectorySet?
        get() {
            val convention = (getConvention("kotlin") ?: getConvention("kotlin2js")) ?: return null
            val kotlinSourceSetIface =
                convention.javaClass.interfaces.find { it.name == KotlinSourceSet::class.qualifiedName }
            val getKotlin = kotlinSourceSetIface?.methods?.find { it.name == "getKotlin" } ?: return null
            return getKotlin(convention) as? SourceDirectorySet

        }

    private fun Any.getConvention(name: String): Any? = (this as HasConvention).convention.plugins[name]

}
