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
            |import kotlin.Unit
            |import kotlinx.serialization.KSerializer
            |import kotlinx.serialization.SerialName
            |import kotlinx.serialization.Serializable
            |import kotlinx.serialization.json.Json
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
            |  object Query {
            |    /**
            |     * Generate Json string of [Request]
            |     */
            |    fun requestBody(json: Json): String = json.stringify(serializer(), Request())
            |
            |    fun serializer(): KSerializer<Request> = Request.serializer()
            |
            |    @Serializable
            |    data class Request(
            |      @SerialName(value = "variables")
            |      override val variables: Unit? = null,
            |      @SerialName(value = "operationName")
            |      override val operationName: String? = null,
            |      @SerialName(value = "query")
            |      override val query: String = document
            |    ) : KgqlRequestBody<Unit>
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
            |import kotlin.Unit
            |import kotlinx.serialization.KSerializer
            |import kotlinx.serialization.SerialName
            |import kotlinx.serialization.Serializable
            |import kotlinx.serialization.json.Json
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
            |  object CodeOfConductQuery {
            |    /**
            |     * Generate Json string of [Request]
            |     */
            |    fun requestBody(json: Json): String = json.stringify(serializer(), Request())
            |
            |    fun serializer(): KSerializer<Request> = Request.serializer()
            |
            |    @Serializable
            |    data class Request(
            |      @SerialName(value = "variables")
            |      override val variables: Unit? = null,
            |      @SerialName(value = "operationName")
            |      override val operationName: String? = "CodeOfConduct",
            |      @SerialName(value = "query")
            |      override val query: String = document
            |    ) : KgqlRequestBody<Unit>
            |  }
            |
            |  object TestQuery {
            |    /**
            |     * Generate Json string of [Request]
            |     */
            |    fun requestBody(json: Json): String = json.stringify(serializer(), Request())
            |
            |    fun serializer(): KSerializer<Request> = Request.serializer()
            |
            |    @Serializable
            |    data class Request(
            |      @SerialName(value = "variables")
            |      override val variables: Unit? = null,
            |      @SerialName(value = "operationName")
            |      override val operationName: String? = "Test",
            |      @SerialName(value = "query")
            |      override val query: String = document
            |    ) : KgqlRequestBody<Unit>
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
                |import kotlinx.serialization.json.Json
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
                |  object WithVariablesQuery {
                |    /**
                |     * Generate Json string of [Request]
                |     */
                |    fun requestBody(variables: Variables, json: Json): String = json.stringify(serializer(),
                |        Request(variables = variables))
                |
                |    fun serializer(): KSerializer<Request> = Request.serializer()
                |
                |    @Serializable
                |    data class Variables(
                |      @SerialName(value = "login")
                |      val login: String
                |    )
                |
                |    @Serializable
                |    data class Request(
                |      @SerialName(value = "variables")
                |      override val variables: Variables?,
                |      @SerialName(value = "operationName")
                |      override val operationName: String? = "WithVariables",
                |      @SerialName(value = "query")
                |      override val query: String = document
                |    ) : KgqlRequestBody<Variables>
                |  }
                |}
                |
            """.trimMargin()
        )
    }
}
