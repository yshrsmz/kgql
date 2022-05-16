package com.codingfeline.kgql.gradle.android

import com.android.build.gradle.BaseExtension
import org.gradle.api.GradleException
import org.gradle.api.Project

internal fun Project.packageName(): String {
    val androidExtensions = extensions.getByType(BaseExtension::class.java)
    return androidExtensions.namespace ?: throw GradleException(
        """
        |SqlDelight requires a package name to be set. This can be done via the android namespace:
        |
        |android {
        |  namespace "com.sample.mygraphql"
        |}
        |
        |or the kgql configuration:
        |
        |kgql {
        |    packageName = "com.sample.mygraphql"
        |}
  """.trimMargin()
    )
}
