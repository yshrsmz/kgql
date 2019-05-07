package com.codingfeline.kgql.compiler

import com.codingfeline.kgql.test.util.FixtureCompiler
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DocumentWrapperTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `documentWrapper create an object for an operation in a document`() {
        val result = FixtureCompiler.compileGql(
            """
            |query {
            |  viewer {
            |    login
            |  }
            |}
            |""".trimMargin(),
            tempFolder
        )

        assertThat(result.errors).isEmpty()

        val documentWrapperFile = result.compilerOutput[File(result.outputDirectory, "com/example/TestDocument.kt")]
        assertThat(documentWrapperFile).isNotNull()
        assertThat(documentWrapperFile.toString()).isEqualTo(
            """
            |package com.example
            |
            |import com.codingfeline.kgql.core.KgqlRequestBody
            |import kotlin.String
            |import kotlinx.serialization.KSerializer
            |import kotlinx.serialization.SerialName
            |import kotlinx.serialization.Serializable
            |import kotlinx.serialization.UnstableDefault
            |import kotlinx.serialization.json.Json
            |import kotlinx.serialization.json.JsonObject
            |
            |internal object TestDocument {
            |  private val document: String = ""${'"'}
            |      |query {
            |      |  viewer {
            |      |    login
            |      |  }
            |      |}
            |      |""${'"'}.trimMargin()
            |
            |  @UnstableDefault
            |  object Query {
            |    /**
            |     * Generate Json string of [Request]
            |     */
            |    fun requestBody(json: Json = Json.plain): String = json.stringify(serializer(), Request())
            |
            |    fun serializer(): KSerializer<Request> = Request.serializer()
            |
            |    @Serializable
            |    data class Request(
            |      @SerialName(value = "variables")
            |      override val variables: JsonObject? = null,
            |      @SerialName(value = "operationName")
            |      override val operationName: String? = null,
            |      @SerialName(value = "query")
            |      override val query: String = document
            |    ) : KgqlRequestBody
            |  }
            |}
            |
        """.trimMargin()
        )
    }


    @Test
    fun `documentWrapper create objects for each operations in a document`() {
        val result = FixtureCompiler.compileGql(
            """
                |query CodeOfConduct {
                |  codesOfConduct {
                |    body
                |    key
                |    name
                |  }
                |}
                |
                |query Test{
                |  viewer {
                |    login
                |  }
                |}
            """.trimMargin(),
            tempFolder
        )

        assertThat(result.errors).isEmpty()
        val documentWrapperFile = result.compilerOutput[File(result.outputDirectory, "com/example/TestDocument.kt")]
        assertThat(documentWrapperFile).isNotNull()
        assertThat(documentWrapperFile.toString()).isEqualTo(
            """
            |package com.example
            |
            |import com.codingfeline.kgql.core.KgqlRequestBody
            |import kotlin.String
            |import kotlinx.serialization.KSerializer
            |import kotlinx.serialization.SerialName
            |import kotlinx.serialization.Serializable
            |import kotlinx.serialization.UnstableDefault
            |import kotlinx.serialization.json.Json
            |import kotlinx.serialization.json.JsonObject
            |
            |internal object TestDocument {
            |  private val document: String = ""${'"'}
            |      |query CodeOfConduct {
            |      |  codesOfConduct {
            |      |    body
            |      |    key
            |      |    name
            |      |  }
            |      |}
            |      |
            |      |query Test{
            |      |  viewer {
            |      |    login
            |      |  }
            |      |}
            |      ""${'"'}.trimMargin()
            |
            |  @UnstableDefault
            |  object CodeOfConductQuery {
            |    /**
            |     * Generate Json string of [Request]
            |     */
            |    fun requestBody(json: Json = Json.plain): String = json.stringify(serializer(), Request())
            |
            |    fun serializer(): KSerializer<Request> = Request.serializer()
            |
            |    @Serializable
            |    data class Request(
            |      @SerialName(value = "variables")
            |      override val variables: JsonObject? = null,
            |      @SerialName(value = "operationName")
            |      override val operationName: String? = "CodeOfConduct",
            |      @SerialName(value = "query")
            |      override val query: String = document
            |    ) : KgqlRequestBody
            |  }
            |
            |  @UnstableDefault
            |  object TestQuery {
            |    /**
            |     * Generate Json string of [Request]
            |     */
            |    fun requestBody(json: Json = Json.plain): String = json.stringify(serializer(), Request())
            |
            |    fun serializer(): KSerializer<Request> = Request.serializer()
            |
            |    @Serializable
            |    data class Request(
            |      @SerialName(value = "variables")
            |      override val variables: JsonObject? = null,
            |      @SerialName(value = "operationName")
            |      override val operationName: String? = "Test",
            |      @SerialName(value = "query")
            |      override val query: String = document
            |    ) : KgqlRequestBody
            |  }
            |}
            |
        """.trimMargin()
        )
    }

    @Test
    fun `documentWrapper creates Variables class if an operation has parameters`() {
        val result = FixtureCompiler.compileGql(
            """
                |query WithVariables(${"$"}login: String!) {
                |  user(login: ${'$'}login) {
                |    id
                |    login
                |    bio
                |    avatarUrl
                |    company
                |    createdAt
                |  }
                |}
            """.trimMargin(),
            tempFolder
        )

        assertThat(result.errors).isEmpty()

        val documentWrapperFile = result.compilerOutput[File(result.outputDirectory, "com/example/TestDocument.kt")]
        assertThat(documentWrapperFile).isNotNull()
        assertThat(documentWrapperFile.toString()).isEqualTo(
            """
                |package com.example
                |
                |import com.codingfeline.kgql.core.KgqlRequestBody
                |import kotlin.String
                |import kotlinx.serialization.KSerializer
                |import kotlinx.serialization.SerialName
                |import kotlinx.serialization.Serializable
                |import kotlinx.serialization.UnstableDefault
                |import kotlinx.serialization.json.Json
                |import kotlinx.serialization.json.JsonObject
                |import kotlinx.serialization.json.json
                |
                |internal object TestDocument {
                |  private val document: String = ""${'"'}
                |      |query WithVariables(${"$" + "{'$'}"}login: String!) {
                |      |  user(login: ${"$" + "{'$'}"}login) {
                |      |    id
                |      |    login
                |      |    bio
                |      |    avatarUrl
                |      |    company
                |      |    createdAt
                |      |  }
                |      |}
                |      ""${'"'}.trimMargin()
                |
                |  @UnstableDefault
                |  object WithVariablesQuery {
                |    /**
                |     * Generate Json string of [Request]
                |     */
                |    fun requestBody(variables: Variables, json: Json = Json.plain): String =
                |        json.stringify(serializer(), Request(variables = variables.asJsonObject()))
                |
                |    fun serializer(): KSerializer<Request> = Request.serializer()
                |
                |    class Variables(
                |      private val login: String
                |    ) {
                |      fun asJsonObject(): JsonObject = json {
                |        "login" to login
                |      }
                |    }
                |
                |    @Serializable
                |    data class Request(
                |      @SerialName(value = "variables")
                |      override val variables: JsonObject?,
                |      @SerialName(value = "operationName")
                |      override val operationName: String? = "WithVariables",
                |      @SerialName(value = "query")
                |      override val query: String = document
                |    ) : KgqlRequestBody
                |  }
                |}
                |
            """.trimMargin()
        )
    }
}
