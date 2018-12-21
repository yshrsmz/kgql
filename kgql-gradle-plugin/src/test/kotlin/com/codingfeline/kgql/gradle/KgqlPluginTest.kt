package com.codingfeline.kgql.gradle

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.Test
import java.io.File

class KgqlPluginTest {

    val query = """
        query User(${"$"}login: String!) {
          user(login: ${"$"}login) {
            id
            login
            bio
            avatarUrl
            company
            createdAt
          }
        }

        fragment userField on User {
            id
            login
        }
    """.trimIndent()

    fun test() {
//        val document = Parser().parseDocument(query)
//
//        println(document)
    }

    @Test
    fun `Applying the plugin without Kotlin applied throws`() {
        val fixtureRoot = File("src/test/no-kotlin")

        val runner = GradleRunner.create()
            .withProjectDir(fixtureRoot)
            .withPluginClasspath()

        val result = runner.withArguments("build", "--stacktrace")
            .buildAndFail()

        assertThat(result.output).contains("Kgql Gradle Plugin applied in project ':' but no supported Kotlin plugin was found")
    }

    @Test
    fun `Applying the plugin without Kotlin applied throws for Android`() {
        val fixtureRoot = File("src/test/no-kotlin-android")
        val runner = GradleRunner.create()
            .withProjectDir(fixtureRoot)
            .withPluginClasspath()

        val result = runner
            .withArguments("build", "--stacktrace")
            .buildAndFail()
        assertThat(result.output)
            .contains("Kgql Gradle Plugin applied in project ':' but no supported Kotlin plugin was found")
    }

    @Test
    fun `Applying the android plugin works fine for library projects`() {
        val androidHome = androidHome()
        val fixtureRoot = File("src/test/library-project")
        File(fixtureRoot, "local.properties").writeText("sdk.dir=$androidHome\n")

        val runner = GradleRunner.create()
            .withProjectDir(fixtureRoot)
            .withPluginClasspath()

        val result = runner
            .withArguments("clean", "generateDebugKgqlInterface", "--stacktrace")
            .build()
        assertThat(result.output).contains("BUILD SUCCESSFUL")
    }

    @Test
    fun `Applying the plugin works fine for multiplatform projects`() {
        val fixtureRoot = File("src/test/kotlin-mpp")
        val runner = GradleRunner.create()
            .withProjectDir(fixtureRoot)
            .withPluginClasspath()

        val result = runner
            .withArguments("clean", "generateKgqlInterface", "--stacktrace", "--info")
            .build()
        assertThat(result.output).contains("BUILD SUCCESSFULa")

        // Assert the plugin added the common dependency
//        val dependenciesResult = runner
//            .withArguments("dependencies", "--stacktrace")
//            .build()
//        assertThat(dependenciesResult.output).contains("com.squareup.sqldelight:runtime")
    }

    @Test
    fun `The generate task is a dependency of multiplatform js target`() {
        val fixtureRoot = File("src/test/kotlin-mpp")
        val runner = GradleRunner.create()
            .withProjectDir(fixtureRoot)
            .withPluginClasspath()

        val buildDir = File(fixtureRoot, "build/kgql")

        buildDir.delete()
        val result = runner
            .withArguments("clean", "compileKotlinJs", "--stacktrace")
            .build()
        assertThat(result.output).contains("generateKgqlInterface")
        assertThat(buildDir.exists()).isTrue()
    }

    @Test
    fun `The generate task is a dependency of multiplatform jvm target`() {
        val fixtureRoot = File("src/test/kotlin-mpp")
        val runner = GradleRunner.create()
            .withProjectDir(fixtureRoot)
            .withPluginClasspath()

        val buildDir = File(fixtureRoot, "build/kgql")

        buildDir.delete()
        val result = runner
            .withArguments("clean", "compileKotlinJvm", "--stacktrace")
            .build()
        assertThat(result.output).contains("generateKgqlInterface")
        assertThat(buildDir.exists()).isTrue()
    }
}
