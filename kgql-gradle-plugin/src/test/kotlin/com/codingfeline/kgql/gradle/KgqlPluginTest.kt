package com.codingfeline.kgql.gradle

import org.junit.Test

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
}
