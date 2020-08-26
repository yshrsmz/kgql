package com.codingfeline.kgql.gradle

import com.codingfeline.kgql.VERSION
import com.codingfeline.kgql.gradle.android.packageName
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.sources.DefaultKotlinSourceSet
import java.util.concurrent.atomic.AtomicBoolean

open class KgqlPlugin : Plugin<Project> {
    private val android = AtomicBoolean(false)
    private val kotlin = AtomicBoolean(false)
    private val serialization = AtomicBoolean(false)

    private lateinit var extension: KgqlExtension

    override fun apply(project: Project) {
        extension = project.extensions.create("kgql", KgqlExtension::class.java)
        extension.project = project

        val androidPluginHandler = { _: Plugin<*> ->
            android.set(true)
            project.afterEvaluate { project.setupKgqlTask(afterAndroid = true) }
        }

        project.plugins.withId("com.android.application", androidPluginHandler)
        project.plugins.withId("com.android.library", androidPluginHandler)
        project.plugins.withId("com.android.instantapp", androidPluginHandler)
        project.plugins.withId("com.android.feature", androidPluginHandler)
        project.plugins.withId("com.android.dynamic-feature", androidPluginHandler)

        val kotlinPluginHandler = { _: Plugin<*> -> kotlin.set(true) }
        project.plugins.withId("org.jetbrains.kotlin.multiplatform", kotlinPluginHandler)
        project.plugins.withId("org.jetbrains.kotlin.android", kotlinPluginHandler)
        project.plugins.withId("org.jetbrains.kotlin.jvm", kotlinPluginHandler)
        project.plugins.withId("kotlin2js", kotlinPluginHandler)

        val serializationPluginHandler = { _: Plugin<*> -> serialization.set(true) }
        project.plugins.withId("org.jetbrains.kotlin.plugin.serialization", serializationPluginHandler)

        project.afterEvaluate { project.setupKgqlTask(afterAndroid = false) }
    }

    private fun Project.setupKgqlTask(afterAndroid: Boolean) {
        if (android.get() && !afterAndroid) return

        check(kotlin.get()) {
            "Kgql Gradle Plugin applied in " +
                    "project '${project.path}' but no supported Kotlin plugin was found"
        }

        check(serialization.get()) {
            "Kgql Gradle Plugin applied in " +
                    "project '${project.path}' but no kotlinx-serialization plugin was found"
        }

        val isMultiplatform = project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")

        // Add the runtime dependency
        if (isMultiplatform) {
            val sourceSets = project.extensions
                .getByType(KotlinMultiplatformExtension::class.java).sourceSets
            val sourceSet = (sourceSets.getByName("commonMain") as DefaultKotlinSourceSet)
            project.configurations.getByName(sourceSet.apiConfigurationName).dependencies
                .add(project.dependencies.create("com.codingfeline.kgql:core:$VERSION"))
        } else {
            project.configurations.getByName("api").dependencies
                .add(project.dependencies.create("com.codingfeline.kgql:core-jvm:$VERSION"))
        }

        extension.run {
            val config: KgqlConfig = toConfig()
            if (config.packageName == null && android.get() && !isMultiplatform) {
                config.packageName = project.packageName()
            }

            project.tasks.register("generateKgqlInterface") {
                it.group = GROUP
                it.description = "Aggregation task which runs every interface generation task for every given source"
            }
            config.registerTask()
        }
    }

    companion object {
        const val GROUP = "kgql"
    }
}