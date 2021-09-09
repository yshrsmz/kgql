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
            |    public val operationName: String? = null
            |
            |    /**
            |     * Create an instance of [Request] which then you can encode to JSON string
            |     */
            |    public fun requestBody(): Request = Request()
            |
            |    public fun serializer(): KSerializer<Request> = Request.serializer()
            |
            |    @Serializable
            |    public data class Request(
            |      @SerialName(value = "variables")
            |      public override val variables: Unit? = null,
            |      @SerialName(value = "operationName")
            |      public override val operationName: String? = Query.operationName,
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
            |    public val operationName: String? = "CodeOfConduct"
            |
            |    /**
            |     * Create an instance of [Request] which then you can encode to JSON string
            |     */
            |    public fun requestBody(): Request = Request()
            |
            |    public fun serializer(): KSerializer<Request> = Request.serializer()
            |
            |    @Serializable
            |    public data class Request(
            |      @SerialName(value = "variables")
            |      public override val variables: Unit? = null,
            |      @SerialName(value = "operationName")
            |      public override val operationName: String? = CodeOfConductQuery.operationName,
            |      @SerialName(value = "query")
            |      public override val query: String = document
            |    ) : KgqlRequestBody<Unit>
            |  }
            |
            |  public object TestQuery {
            |    public val operationName: String? = "Test"
            |
            |    /**
            |     * Create an instance of [Request] which then you can encode to JSON string
            |     */
            |    public fun requestBody(): Request = Request()
            |
            |    public fun serializer(): KSerializer<Request> = Request.serializer()
            |
            |    @Serializable
            |    public data class Request(
            |      @SerialName(value = "variables")
            |      public override val variables: Unit? = null,
            |      @SerialName(value = "operationName")
            |      public override val operationName: String? = TestQuery.operationName,
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
                |    public val operationName: String? = "WithVariables"
                |
                |    /**
                |     * Create an instance of [Request] which then you can encode to JSON string
                |     */
                |    public fun requestBody(variables: Variables): Request = Request(variables = variables)
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
                |      public override val operationName: String? = WithVariablesQuery.operationName,
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
