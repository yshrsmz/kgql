package com.codingfeline.kgql.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.options.SyncOptions
import com.codingfeline.kgql.core.KgqlFileType
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper

import java.util.function.BooleanSupplier

class KgqlPlugin2 implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def extension = project.extensions.create('kgql', KgqlExtension2)

        boolean kotlin = false
        boolean android = false


        project.plugins.all {
            switch (it) {
                case KotlinBasePluginWrapper:
                    kotlin = true
                    break
                case BasePlugin:
                    android = true
                    break
            }
        }

        boolean hasAndroidAndKotlinPlugin =
                (project.plugins.hasPlugin('com.android.application') ||
                        project.plugins.hasPlugin('com.android.library')) &&
                        project.plugins.hasPlugin('org.jetbrains.kotlin.android')
        if (hasAndroidAndKotlinPlugin) {
            // The kotlin plugin does it's own magic after evaluate, but it needs to know about our
            // generated code. So run Now instead of after evaluations
        }

    }

    private void configureAndroid(Project project, KgqlExtension2 extension) {
        DomainObjectSet<BaseVariant> variants
        if (project.plugins.hasPlugin('com.android.application')) {
            variants = project.extensions.getByType(AppExtension).applicationVariants
        } else if (project.plugins.hasPlugin('com.android.library')) {
            variants = project.extensions.getByType(LibraryExtension).libraryVariants
        } else {
            throw IllegalStateException("Unknown Android plugin in project ${project.path}")
        }

    }

    private void configureAndroid(Project project, KgqlExtension2 extension, DomainObjectSet<BaseVariant> variants) {
        String packageName = null
        List<List<String>> sourceSets = ArrayList()
        File buildDirectory = File(File(File('generated'), 'source'), 'kgql')
        Map<String, String> typeMap = extension.typeMapper ?: HashMap()

        variants.all { BaseVariant variant ->
            String taskName = "generate${variant.name.capitalize()}KgqlInterface"
            KgqlTask2 task = project.tasks.create(taskName, KgqlTask2) {
                it.group = 'kgql'
                it.outputDirectory = buildDirectory
                it.typeMap = typeMap
                it.description = 'Generate Android interface for working with GraphQL documents'
                it.source(variant.sourceSets.collect { "src/${it.name}/${KgqlFileType.FOLDER_NAME}" })
                it.include("**${File.separatorChar}*.${KgqlFileType.EXTENSION}")
                it.packageName = getPackageName(variant, project)
                it.sourceFolders = variant.sourceSets.collect {
                    File("${project.projectDir}/src/${it.name}/${KgqlFileType.FOLDER_NAME}")
                } as Iterable<File>
                sourceSets.add(it.sourceFolders.collect { dir -> dir.toPath().relativize(project.projectDir.toPath()).toString() })
                packageName = it.packageName
            } as KgqlTask2
            variant.registerJavaGeneratingTask(task, task.outputDirectory)
        }

        project.afterEvaluate {}
    }

    private String getPackageName(BaseVariant variant, Project project) {
        return variant.sourceSets.collect { it.manifestFile }
                .findAll { it.exists() }
                .collect {
            DefaultManifestParser(
                    it,
                    {
                        true
                    } as BooleanSupplier,
                    SyncIssueHandlerImpl(SyncOptions.EvaluationMode.STANDARD, project.logger)).package
        }
        .findAll { it != null }
                .first()
    }
}
