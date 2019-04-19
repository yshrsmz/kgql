package com.codingfeline.kgql.gradle

import com.codingfeline.kgql.VERSION
import graphql.introspection.IntrospectionResultToSchema
import graphql.language.AstPrinter
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.SchemaPrinter
import groovy.json.JsonSlurper
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.StringReader


@UnstableDefault
@KtorExperimentalAPI
open class FetchSchemaTask : DefaultTask() {
    @Input
    fun pluginVersion() = VERSION

    @Input
    lateinit var endpoint: String

    @Input
    lateinit var requestHeaders: Map<String, String>

    @get:OutputDirectory
    var outputDirectory: File? = null

    private val client by lazy { HttpClient(CIO) }

    @TaskAction
    fun fetchGraphQLSchema() {
        runBlocking {
            val result = client.get<String>(endpoint) {
                if (requestHeaders.isNotEmpty()) {
                    headers {
                        requestHeaders.entries.forEach { entry ->
                            append(entry.key, entry.value)
                        }
                    }
                }
                this.body = Json.stringify(SchemaRequest.serializer(), SchemaRequest())
            }

            val parsed = slurp(result)
            @Suppress("UNCHECKED_CAST") val schema =
                IntrospectionResultToSchema().createSchemaDefinition(parsed["data"] as Map<String, Any>)

            val printedSchema = SchemaPrinter().print(schema)
            val schemaProvider = StringReader(printedSchema)
            val parser = SchemaParser()
            val schemaGenerator = SchemaGenerator()
            val typeRegistry = parser.parse(schemaProvider)
            val graphqlSchema =
                schemaGenerator.makeExecutableSchema(typeRegistry, RuntimeWiring.newRuntimeWiring().build())

            print(AstPrinter.printAst(schema))
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun slurp(input: String): Map<String, Any> {
        val slurper = JsonSlurper()
        return slurper.parseText(input) as Map<String, Any>
    }
}

@Serializable
data class SchemaRequest(
    val operationName: String = "IntrospectionQuery",
    val query: String = SCHEMA_QUERY
)

// https://github.com/graphql/graphql-js/blob/master/src/utilities/introspectionQuery.js
private const val SCHEMA_QUERY = """
query IntrospectionQuery {
  __schema {
    queryType { name }
    mutationType { name }
    subscriptionType { name }
    types {
      ...FullType
    }
    directives {
      name
      description
      locations
      args {
        ...InputValue
      }
    }
  }
}

fragment FullType on __Type {
  kind
  name
  description
  fields(includeDeprecated: true) {
    name
    description
    args {
      ...InputValue
    }
    type {
      ...TypeRef
    }
    isDeprecated
    deprecationReason
  }
  inputFields {
    ...InputValue
  }
  interfaces {
    ...TypeRef
  }
  enumValues(includeDeprecated: true) {
    name
    description
    isDeprecated
    deprecationReason
  }
  possibleTypes {
    ...TypeRef
  }
}

fragment InputValue on __InputValue {
  name
  description
  type { ...TypeRef }
  defaultValue
}

fragment TypeRef on __Type {
  kind
  name
  ofType {
    kind
    name
    ofType {
      kind
      name
      ofType {
        kind
        name
        ofType {
          kind
          name
          ofType {
            kind
            name
            ofType {
              kind
              name
              ofType {
                kind
                name
              }
            }
          }
        }
      }
    }
  }
}
"""
