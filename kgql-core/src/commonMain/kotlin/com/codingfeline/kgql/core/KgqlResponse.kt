package com.codingfeline.kgql.core

import kotlinx.serialization.Serializable

/**
 * Root interface for GraphQL response
 */
interface KgqlResponse<T> {
    /**
     * Actual result
     */
    val data: T?

    /**
     * Error
     */
    val errors: List<KgqlError>?
}

/**
 * GraphQL error
 */
@Serializable
data class KgqlError(
    /**
     * Error message
     */
    val message: String
)
