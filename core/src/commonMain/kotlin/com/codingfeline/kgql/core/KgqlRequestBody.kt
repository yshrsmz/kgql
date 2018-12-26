package com.codingfeline.kgql.core

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class KgqlRequestBody<T>(
    @Optional val operationName: String? = null,
    val query: String,
    @Optional val variables: T? = null
)
