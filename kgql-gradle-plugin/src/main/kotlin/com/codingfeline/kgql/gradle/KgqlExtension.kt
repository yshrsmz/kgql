package com.codingfeline.kgql.gradle

import org.gradle.api.Project
import org.gradle.api.file.FileCollection

open class KgqlExtension {
    internal lateinit var project: Project

    var packageName: String? = null
    var sourceSet: FileCollection? = null
    var typeMapper: MutableMap<String, String>? = null

    internal fun toConfig(): KgqlConfig {
        return KgqlConfig(
            project = project,
            packageName = packageName,
            sourceSet = sourceSet,
            typeMapper = typeMapper
        )
    }
}
