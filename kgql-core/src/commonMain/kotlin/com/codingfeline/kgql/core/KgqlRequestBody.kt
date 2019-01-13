package com.codingfeline.kgql.core

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

/**
 * Root class for GraphQL request body
 */
@Serializable
data class KgqlRequestBody<T>(
    @Optional val operationName: String? = null,
    val query: String,
    @Optional val variables: T? = null
)
