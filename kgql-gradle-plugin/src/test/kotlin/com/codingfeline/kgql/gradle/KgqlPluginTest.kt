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

    @Test
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
}
