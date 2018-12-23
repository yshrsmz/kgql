package com.codingfeline.kgql.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.errors.SyncIssueHandlerImpl
import com.android.build.gradle.options.SyncOptions
import com.android.builder.core.DefaultManifestParser
import com.codingfeline.kgql.VERSION
import com.codingfeline.kgql.core.KgqlFileType
import com.codingfeline.kgql.core.KgqlPropertiesFile
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
import java.io.File

@Suppress("unused")
class KgqlPlugin : Plugin<Project> {
    override fun apply(project: Project) {
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
            (hasPlugin("com.android.application") || hasPlugin("com.android.library"))
                && hasPlugin("org.jetbrains.kotlin.android")
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
        val outputDirectory = File(project.buildDir, "kgql")

        val kotlinSrcs = if (isMultiplatform) {
            val sourceSets = project.extensions.getByType(KotlinMultiplatformExtension::class.java).sourceSets
            val sourceSet = (sourceSets.getByName("commonMain") as DefaultKotlinSourceSet)
            println("sourceSet2: ${sourceSet.apiConfigurationName}")
            // add runtime dependency
            project.configurations.getByName(sourceSet.apiConfigurationName).dependencies.add(
                    project.dependencies.create("com.codingfeline.kgql:core:${VERSION}")
            )
            sourceSet.kotlin
        } else {
            val sourceSets = project.property("sourceSets") as SourceSetContainer
            sourceSets.getByName("main").kotlin!!
        }
        kotlinSrcs.srcDirs(outputDirectory.toRelativeString(project.projectDir))

        project.afterEvaluate { p ->
            val packageName = requireNotNull(extension.packageName) { "property packageName must be provided" }
            val sourceSet = extension.sourceSet ?: p.files("src/main/kgql")
            val typeMap = extension.typeMapper ?: emptyMap()

            val ideaDir = File(p.rootDir, ".idea")
            if (ideaDir.exists()) {
                val propsDir = File(ideaDir, "kgql/${p.projectDir.toRelativeString(p.rootDir)}")
                propsDir.mkdirs()

                val properties = KgqlPropertiesFile(
                    packageName = packageName,
                    sourceSets = listOf(sourceSet.map { it.toRelativeString(p.projectDir) }),
                    outputDirectory = outputDirectory.toRelativeString(p.projectDir)
                )
                properties.toFile(File(propsDir, KgqlPropertiesFile.NAME))
            }

            val task = p.tasks.register("generateKgqlInterface", KgqlTask::class.java) { task ->
                println("register task: $packageName, ${sourceSet.files}, $outputDirectory")
                task.packageName = packageName
                task.sourceFolders = sourceSet.files
                task.outputDirectory = outputDirectory
                task.typeMap = typeMap
                task.source(sourceSet)
                task.include("**${File.separatorChar}*.${KgqlFileType.EXTENSION}")
                task.group = "kgql"
                task.description = "Generate Kotlin interfaces for .gql files"

            }

            if (isMultiplatform) {
                p.extensions.getByType(KotlinMultiplatformExtension::class.java).targets.forEach { target ->
                    target.compilations.forEach { compilationUnit ->
                        if (compilationUnit is KotlinNativeCompilation) {
                            // Honestly the way native compiles kotlin seems completely arbitrary and some order
                            // of the following tasks, so just set the dependency for all of them and let gradle
                            // figure it out.
                            p.tasks.named(compilationUnit.compileAllTaskName).configure { it.dependsOn(task) }
                            p.tasks.named(compilationUnit.compileKotlinTaskName).configure { it.dependsOn(task) }
                            p.tasks.named(compilationUnit.linkAllTaskName).configure { it.dependsOn(task) }
                            NativeOutputKind.values().forEach { kind ->
                                NativeBuildType.values().forEach { buildType ->
                                    compilationUnit.findLinkTask(kind, buildType)?.dependsOn(task)
                                }
                            }
                        } else {
                            p.tasks.named(compilationUnit.compileKotlinTaskName).configure { it.dependsOn(task) }
                        }
                    }
                }
            } else {
                p.tasks.named("compileKotlin").configure { it.dependsOn(task) }
            }
        }
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
        val typeMap = extension.typeMapper ?: emptyMap()

        variants.all { variant ->
            val taskName = "generate${variant.name.capitalize()}KgqlInterface"
            val taskProvider = project.tasks.register(taskName, KgqlTask::class.java) { task ->
                task.group = "kgql"
                task.outputDirectory = buildDirectory
                task.typeMap = typeMap
                task.description = "Generate Android interfaces for working with GraphQL documents"
                task.source(variant.sourceSets.map { "src/${it.name}/${KgqlFileType.FOLDER_NAME}" })
                task.include("**${File.separatorChar}*.${KgqlFileType.EXTENSION}")
                task.packageName = variant.packageName(project)
                task.sourceFolders = variant.sourceSets.map { File("${project.projectDir}/src/${it.name}/${KgqlFileType.FOLDER_NAME}") }
                sourceSets.add(task.sourceFolders.map { it.toRelativeString(project.projectDir) })
                packageName = task.packageName
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

    // Copied from kotlin plugin
    private val SourceSet.kotlin: SourceDirectorySet?
        get() {
            val convention = (getConvention("kotlin") ?: getConvention("kotlin2js")) ?: return null
            val kotlinSourceSetIface = convention.javaClass.interfaces.find { it.name == KotlinSourceSet::class.qualifiedName }
            val getKotlin = kotlinSourceSetIface?.methods?.find { it.name == "getKotlin" } ?: return null
            return getKotlin(convention) as? SourceDirectorySet
        }

    private fun Any.getConvention(name: String): Any? = (this as HasConvention).convention.plugins[name]
}
