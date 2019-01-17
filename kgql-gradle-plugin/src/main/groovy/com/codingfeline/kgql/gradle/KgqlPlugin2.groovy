package com.codingfeline.kgql.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.errors.SyncIssueHandlerImpl
import com.android.build.gradle.options.SyncOptions
import com.android.builder.core.DefaultManifestParser
import com.codingfeline.kgql.VersionKt
import com.codingfeline.kgql.compiler.KgqlFileType
import com.codingfeline.kgql.compiler.KgqlPropertiesFile
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
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

import java.util.function.BooleanSupplier

class KgqlPlugin2 implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def extension = project.extensions.create('kgql', KgqlExtension2.class)

        boolean kotlin = false
        boolean serialization = false
        boolean android = false

        project.plugins.all {
            switch (it) {
                case KotlinBasePluginWrapper:
                    kotlin = true
                    break
                case SerializationGradleSubplugin:
                    serialization = true
                    break
                case BasePlugin:
                    android = true
                    break
            }
        }

        boolean hasAndroidAndKotlinAndSerializationPlugin =
                (project.plugins.hasPlugin('com.android.application') ||
                        project.plugins.hasPlugin('com.android.library')) &&
                        project.plugins.hasPlugin('org.jetbrains.kotlin.android') &&
                        project.plugins.hasPlugin('org.jetbrains.kotlin.plugin.serialization')
        if (hasAndroidAndKotlinAndSerializationPlugin) {
            // The kotlin plugin does it's own magic after evaluate, but it needs to know about our
            // generated code. So run Now instead of after evaluations
            configureAndroid(project, extension, false)
            return
        }

        project.afterEvaluate { Project p ->
            if (!kotlin) {
                throw new IllegalStateException("Kgql Gradle Plugin applied in project '${project.path}' but no supported Kotlin plugin was found")
            }
            if (!serialization) {
                throw new IllegalStateException("Kgql Gradle Plugin applied in project '${project.path}' but no kotlinx-serialization plugin was found")
            }
            boolean isMultiplatform = project.plugins.hasPlugin('org.jetbrains.kotlin.multiplatform')
            if (android && !isMultiplatform) {
                configureAndroid(p, extension, true)
            } else {
                configureKotlin(p, extension, isMultiplatform, true)
            }
        }
    }

    private void configureKotlin(Project project, KgqlExtension2 extension, boolean isMultiplatform, boolean evaluated) {
        File outputDirectory = new File(project.buildDir, 'kgql')

        def kotlinSrcs
        if (isMultiplatform) {
            def sourceSets = project.extensions.getByType(KotlinMultiplatformExtension.class).sourceSets
            def sourceSet = (sourceSets.getByName('commonMain') as DefaultKotlinSourceSet)
            project.configurations.getByName(sourceSet.apiConfigurationName).dependencies.add(
                    project.dependencies.create("com.codingfeline.kgql:core:${VersionKt.VERSION}")
            )
            kotlinSrcs = sourceSet.kotlin
        } else {
            def sourceSets = project.property('sourceSets') as SourceSetContainer
            kotlinSrcs = getKotlinSourceDirectorySret(sourceSets.getByName('main'))
        }
        kotlinSrcs.srcDirs(project.projectDir.toPath().relativize(outputDirectory.toPath()).toString())

        def action = { Project p ->
            def packageName = Objects.requireNonNull(extension.packageName, "property packageName must be provided")
            def sourceSet = extension.sourceSet ?: p.files('src/main/kgql')
            def typeMap = extension.typeMapper ?: new HashMap<String, String>()

            def ideaDir = new File(p.rootDir, '.idea')
            if (ideaDir.exists()) {
                def propsDir = new File(ideaDir, "kgql/${p.rootDir.toPath().relativize(p.projectDir.toPath()).toString()}")
                propsDir.mkdirs()

                def sourceSets = new ArrayList()
                sourceSets.add(sourceSet.collect { p.projectDir.toPath().relativize(it.toPath()).toString() })
                def properties = new KgqlPropertiesFile(
                        packageName,
                        sourceSets,
                        p.projectDir.toPath().relativize(outputDirectory.toPath()).toString()
                )
                properties.toFile(new File(propsDir, KgqlPropertiesFile.NAME))
            }

            KgqlTask2 task = p.tasks.create('generateKgqlInterface', KgqlTask2.class) {
                it.packageName = packageName
                it.sourceFolders = sourceSet.files
                it.outputDirectory = outputDirectory
                it.typeMap = typeMap
                it.source(sourceSet)
                it.include("**${File.separatorChar}*.${KgqlFileType.EXTENSION}")
                it.group = 'kgql'
                it.description = 'Generate Kotlin interface for .gql files'
            } as KgqlTask2

            if (isMultiplatform) {
                p.extensions.getByType(KotlinMultiplatformExtension.class).targets.forEach { target ->
                    target.compilations.forEach { compilationUnit ->
                        if (compilationUnit instanceof KotlinNativeCompilation) {
                            // Honestly the way native compiles kotlin seems completely arbitrary and some order
                            // of the following tasks, so just set the dependency for all of them and let gradle
                            // figure it out.
                            p.tasks.getByName(compilationUnit.compileAllTaskName).configure {
                                (it as Task).dependsOn(task)
                            }
                            p.tasks.getByName(compilationUnit.compileKotlinTaskName).configure {
                                (it as Task).dependsOn(task)
                            }
                            p.tasks.findByName((compilationUnit as KotlinNativeCompilation).linkAllTaskName)?.configure {
                                (it as Task).dependsOn(task)
                            }
                            p.tasks.findByName(getAlternateLinkAllTaskName(compilationUnit as KotlinNativeCompilation))?.configure {
                                (it as Task).dependsOn(task)
                            }
                            NativeOutputKind.values().each { kind ->
                                NativeBuildType.values().each { buildType ->
                                    (compilationUnit as KotlinNativeCompilation).findLinkTask(kind, buildType)?.dependsOn(task)
                                }
                            }
                        } else {
                            p.tasks.getByName(compilationUnit.compileKotlinTaskName).configure {
                                (it as Task).dependsOn(task)
                            }
                        }
                    }
                }
            } else {
                p.tasks.getByName('compileKotlin').configure { (it as Task).dependsOn(task) }
            }
        }

        if (evaluated) {
            action.call(project)
        } else {
            project.afterEvaluate(action)
        }
    }

    private void configureAndroid(Project project, KgqlExtension2 extension, boolean evaluated) {
        DomainObjectSet<BaseVariant> variants
        if (project.plugins.hasPlugin('com.android.application')) {
            variants = project.extensions.getByType(AppExtension).applicationVariants
        } else if (project.plugins.hasPlugin('com.android.library')) {
            variants = project.extensions.getByType(LibraryExtension).libraryVariants
        } else {
            throw IllegalStateException("Unknown Android plugin in project ${project.path}")
        }
        configureAndroid(project, extension, variants as DomainObjectSet<BaseVariant>, evaluated)
    }

    private void configureAndroid(Project project, KgqlExtension2 extension, DomainObjectSet<BaseVariant> variants, boolean evaluated) {
        String packageName = null
        List<List<String>> sourceSets = new ArrayList()
        File buildDirectory = new File(new File(new File('generated'), 'source'), 'kgql')
        Map<String, String> typeMap = extension.typeMapper ?: new HashMap()

        variants.all { BaseVariant variant ->
            String taskName = "generate${variant.name.capitalize()}KgqlInterface"
            KgqlTask2 task = project.tasks.create(taskName, KgqlTask2.class) {
                it.group = 'kgql'
                it.outputDirectory = buildDirectory
                it.typeMap = typeMap
                it.description = 'Generate Android interface for working with GraphQL documents'
                it.source(variant.sourceSets.collect { "src/${it.name}/${KgqlFileType.FOLDER_NAME}" })
                it.include("**${File.separatorChar}*.${KgqlFileType.EXTENSION}")
                it.packageName = getPackageName(variant, project)
                it.sourceFolders = variant.sourceSets.collect {
                    new File("${project.projectDir}/src/${it.name}/${KgqlFileType.FOLDER_NAME}")
                } as Iterable<File>
                sourceSets.add(it.sourceFolders.collect { dir -> project.projectDir.toPath().relativize(dir.toPath()).toString() })
                packageName = it.packageName
            } as KgqlTask2
            variant.registerJavaGeneratingTask(task, task.outputDirectory)
        }

        def action = { Project p ->
            File ideaDir = new File(project.rootDir, '.idea')
            if (ideaDir.exists()) {
                File propsDir = new File(ideaDir, "kgql/${p.rootDir.toPath().relativize(p.projectDir.toPath()).toString()}")
                propsDir.mkdirs()

                KgqlPropertiesFile properties = new KgqlPropertiesFile(
                        packageName,
                        sourceSets,
                        p.rootDir.toPath().relativize(buildDirectory.toPath()).toString()
                )
                properties.toFile(new File(propsDir, KgqlPropertiesFile.NAME))
            }
        }

        if (evaluated) {
            action.call(project)
        } else {
            project.afterEvaluate(action)
        }
    }

    private String getPackageName(BaseVariant variant, Project project) {
        return variant.sourceSets.collect { it.manifestFile }
                .findAll { it.exists() }
                .collect {

            new DefaultManifestParser(
                    it,
                    {
                        true
                    } as BooleanSupplier,
                    new SyncIssueHandlerImpl(SyncOptions.EvaluationMode.STANDARD, project.logger)).package
        }
        .findAll { it != null }
                .first()
    }

    private SourceDirectorySet getKotlinSourceDirectorySret(SourceSet sourceSet) {
        def convention = getConvention(sourceSet, 'kotlin') ?: getConvention(sourceSet, 'kotlin2js') ?: null
        def kotlinSourceSetIface = convention.class.interfaces.find {
            it.name == KotlinSourceSet.getClass().getCanonicalName()
        }
        def getKotlin = kotlinSourceSetIface?.methods?.find { it.name == 'getKotlin' } ?: null
        return getKotlin.invoke(convention) as SourceDirectorySet
    }

    private Object getConvention(Object target, String name) {
        return (target as HasConvention).convention.plugins[name]
    }

    private String getAlternateLinkAllTaskName(KotlinNativeCompilation compilation) {
        def compilationName = ''
        if (compilation.compilationName != 'main') {
            compilationName = compilation.compilationName
        }
        return "${compilation.target.disambiguationClassifier.uncapitalize()}${compilationName.capitalize()}Link"
    }
}
