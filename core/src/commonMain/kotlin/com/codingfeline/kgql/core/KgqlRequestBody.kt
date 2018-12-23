package com.codingfeline.kgql.core

data class KgqlRequestBody(
    val operationName: String? = null,
    val query: String,
    val variables: Map<String, Any>
)
