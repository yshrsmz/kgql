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
            |  public object Query {
            |    /**
            |     * Generate Json string of [Request]
            |     */
            |    public fun requestBody(json: Json): String = json.encodeToString(serializer(), Request())
            |
            |    public fun serializer(): KSerializer<Request> = Request.serializer()
            |
            |    @Serializable
            |    public data class Request(
            |      @SerialName(value = "variables")
            |      public override val variables: Unit? = null,
            |      @SerialName(value = "operationName")
            |      public override val operationName: String? = null,
            |      @SerialName(value = "query")
            |      public override val query: String = document
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
            |  public object CodeOfConductQuery {
            |    /**
            |     * Generate Json string of [Request]
            |     */
            |    public fun requestBody(json: Json): String = json.encodeToString(serializer(), Request())
            |
            |    public fun serializer(): KSerializer<Request> = Request.serializer()
            |
            |    @Serializable
            |    public data class Request(
            |      @SerialName(value = "variables")
            |      public override val variables: Unit? = null,
            |      @SerialName(value = "operationName")
            |      public override val operationName: String? = "CodeOfConduct",
            |      @SerialName(value = "query")
            |      public override val query: String = document
            |    ) : KgqlRequestBody<Unit>
            |  }
            |
            |  public object TestQuery {
            |    /**
            |     * Generate Json string of [Request]
            |     */
            |    public fun requestBody(json: Json): String = json.encodeToString(serializer(), Request())
            |
            |    public fun serializer(): KSerializer<Request> = Request.serializer()
            |
            |    @Serializable
            |    public data class Request(
            |      @SerialName(value = "variables")
            |      public override val variables: Unit? = null,
            |      @SerialName(value = "operationName")
            |      public override val operationName: String? = "Test",
            |      @SerialName(value = "query")
            |      public override val query: String = document
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
                |  public object WithVariablesQuery {
                |    /**
                |     * Generate Json string of [Request]
                |     */
                |    public fun requestBody(variables: Variables, json: Json): String =
                |        json.encodeToString(serializer(), Request(variables = variables))
                |
                |    public fun serializer(): KSerializer<Request> = Request.serializer()
                |
                |    @Serializable
                |    public data class Variables(
                |      @SerialName(value = "login")
                |      public val login: String
                |    )
                |
                |    @Serializable
                |    public data class Request(
                |      @SerialName(value = "variables")
                |      public override val variables: Variables?,
                |      @SerialName(value = "operationName")
                |      public override val operationName: String? = "WithVariables",
                |      @SerialName(value = "query")
                |      public override val query: String = document
                |    ) : KgqlRequestBody<Variables>
                |  }
                |}
                |
            """.trimMargin()
        )
    }
}
