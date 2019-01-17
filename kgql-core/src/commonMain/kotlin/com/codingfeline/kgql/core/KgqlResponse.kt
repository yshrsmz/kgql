package com.codingfeline.kgql.core

import kotlinx.serialization.Serializable

/**
 * Root interface for GraphQL response
 */
interface KgqlResponse<T> {
    val data: T?
    val errors: List<KgqlError>?
}

/**
 * GraphQL error
 */
@Serializable
data class KgqlError(
    val message: String
)
