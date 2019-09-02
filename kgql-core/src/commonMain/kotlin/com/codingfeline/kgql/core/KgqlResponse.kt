package com.codingfeline.kgql.core

import kotlinx.serialization.SerialName
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
    @SerialName("message") val message: String,
    @SerialName("locations") val locations: List<KgqlErrorLocation> = emptyList(),
    @SerialName("description") val description: String,
    @SerialName("validationErrorType") val validationErrorType: String,
    @SerialName("queryPath") val queryPath: List<String> = emptyList()
)

@Serializable
data class KgqlErrorLocation(
    @SerialName("line") val line: Int,
    @SerialName("column") val column: Int
)
