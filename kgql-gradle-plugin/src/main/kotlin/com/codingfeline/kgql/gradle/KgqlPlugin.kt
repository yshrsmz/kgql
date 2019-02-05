package com.codingfeline.kgql.gradle

import com.android.build.gradle.BasePlugin
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper
import org.jetbrains.kotlinx.serialization.gradle.SerializationGradleSubplugin

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

    }

    private fun configureAndroid(project: Project, extension: KgqlExtension) {

    }

    private fun configureAndroid(project: Project, extension: KgqlExtension, variants: DomainObjectSet<BaseVariant>) {

    }

}
