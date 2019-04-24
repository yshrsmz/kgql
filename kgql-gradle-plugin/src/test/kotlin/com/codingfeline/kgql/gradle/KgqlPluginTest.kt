package com.codingfeline.kgql.gradle

import com.codingfeline.kgql.compiler.scalar.GraphQLDateTimeScalar
import com.codingfeline.kgql.compiler.scalar.GraphQLURIScalar
import com.google.common.truth.Truth.assertThat
import graphql.introspection.IntrospectionResultToSchema
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.SchemaPrinter
import groovy.json.JsonSlurper
import org.gradle.testkit.runner.GradleRunner
import org.junit.Test
import org.junit.experimental.categories.Category
import java.io.File
import java.io.StringReader

class KgqlPluginTest {

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
    fun `Applying the plugin without kotlinx-serialization applied throws`() {
        val fixtureRoot = File("src/test/kotlin-mpp-no-serialization")

        val runner = GradleRunner.create()
            .withProjectDir(fixtureRoot)
            .withPluginClasspath()

        val result = runner.withArguments("build", "--stacktrace")
            .buildAndFail()

        assertThat(result.output).contains("Kgql Gradle Plugin applied in project ':' but no kotlinx-serialization plugin was found")
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

        // Assert the plugin added the common dependency
        val dependenciesResult = runner
            .withArguments("dependencies", "--stacktrace")
            .build()
        assertThat(dependenciesResult.output).contains("com.codingfeline.kgql:core-jvm")
    }

    @Test
    fun `Applying the plugin works fine for multiplatform projects`() {
        val fixtureRoot = File("src/test/kotlin-mpp")
        val runner = GradleRunner.create()
            .withProjectDir(fixtureRoot)
            .withPluginClasspath()

        val schemaResult = runner.withArguments("clean", "fetchGraphQLSchema", "--stacktrace", "--info")
            .build()

        val result = runner
            .withArguments(
                "clean",
                "generateKgqlInterface",
                "--stacktrace",
                "--info",
                "--no-daemon"
            )
            .buildAndFail()
        assertThat(result.output).contains("BUILD SUCCESSFUL")

        // Assert the plugin added the common dependency
        val dependenciesResult = runner
            .withArguments("dependencies", "--stacktrace")
            .build()
        assertThat(dependenciesResult.output).contains("com.codingfeline.kgql:core")
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

    @Test
    fun `The generate task is a dependency of multiplatform android target`() {
        val fixtureRoot = File("src/test/kotlin-mpp-android-ios")
        val runner = GradleRunner.create()
            .withProjectDir(fixtureRoot)
            .withPluginClasspath()

        val buildDir = File(fixtureRoot, "build/kgql")
        buildDir.delete()

        val result = runner
            .withArguments("clean", "compileDebugKotlinAndroid", "--stacktrace", "--info")
            .build()
        assertThat(result.output).contains("BUILD SUCCESSFUL")
        assertThat(result.output).contains("generateKgqlInterface")
    }

    @Test
    @Category(IosTest::class)
    fun `The generate task is a dependency of multiplatform ios target`() {
        val fixtureRoot = File("src/test/kotlin-mpp")
        val runner = GradleRunner.create()
            .withProjectDir(fixtureRoot)
            .withPluginClasspath()

        val buildDir = File(fixtureRoot, "build/kgql")

        buildDir.delete()
        var result = runner
            .withArguments("clean", "linkDebugFrameworkIosArm64", "--stacktrace")
            .build()

        assertThat(result.output).contains("generateKgqlInterface")
        assertThat(buildDir.exists()).isTrue()

        buildDir.delete()
        result = runner
            .withArguments("clean", "linkDebugFrameworkIosX64", "--stacktrace")
            .build()

        assertThat(result.output).contains("generateKgqlInterface")
        assertThat(buildDir.exists()).isTrue()
    }

    @Test
    fun testSchema() {
        val wiring = RuntimeWiring.newRuntimeWiring()
            .scalar(GraphQLDateTimeScalar())
            .scalar(GraphQLURIScalar())
            .build()

        val schemaFile = File("src/test/kotlin-mpp/src/main/kgql/schema.json")

        val json = JsonSlurper().parseText(schemaFile.readText()) as Map<String, Any>
        val schema = IntrospectionResultToSchema().createSchemaDefinition(json["data"] as Map<String, Any>)

        val printedSchema = SchemaPrinter().print(schema)
        val schemaProvider = StringReader(printedSchema)
        val parser = SchemaParser()
        val schemaGenerator = SchemaGenerator()
        val typeRegistry = parser.parse(schemaProvider)
        val graphqlSchema = schemaGenerator.makeExecutableSchema(typeRegistry, wiring)

    }
}
