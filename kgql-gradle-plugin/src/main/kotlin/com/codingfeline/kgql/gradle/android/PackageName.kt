package com.codingfeline.kgql.gradle.android

import com.android.build.gradle.BaseExtension
import com.android.ide.common.symbols.getPackageNameFromManifest
import org.gradle.api.Project

internal fun Project.packageName(): String {
    val androidExtensions = extensions.getByType(BaseExtension::class.java)
    androidExtensions.sourceSets
        .map { it.manifest.srcFile }
        .filter { it.exists() }
        .forEach {
            return getPackageNameFromManifest(it)
        }
    throw IllegalStateException("No source sets available")
}
