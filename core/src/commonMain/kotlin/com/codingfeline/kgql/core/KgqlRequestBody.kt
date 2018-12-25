package com.codingfeline.kgql.core

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class KgqlRequestBody(
    @Optional val operationName: String? = null,
    val query: String,
    @Optional val variables: Map<String, Any>? = null
)
