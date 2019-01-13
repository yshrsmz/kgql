package com.codingfeline.kgql.core

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

/**
 * Root class for GraphQL response
 */
@Serializable
data class KgqlResponse<T>(
    @Optional val data: T? = null,
    @Optional val errors: List<KgqlError>? = null
)

/**
 * GraphQL error
 */
@Serializable
data class KgqlError(
    val message: String
)
