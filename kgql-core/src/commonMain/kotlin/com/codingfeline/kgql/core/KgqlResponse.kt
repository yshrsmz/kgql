package com.codingfeline.kgql.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Root interface for GraphQL response
 */
public interface KgqlResponse<T> {
    public val data: T?
    public val errors: List<KgqlError>?
}

/**
 * GraphQL error
 */
@Serializable
public data class KgqlError(
    @SerialName("message") val message: String,
    @SerialName("locations") val locations: List<KgqlErrorLocation> = emptyList(),
    @SerialName("description") val description: String,
    @SerialName("validationErrorType") val validationErrorType: String,
    @SerialName("queryPath") val queryPath: List<String> = emptyList()
)

@Serializable
public data class KgqlErrorLocation(
    @SerialName("line") val line: Int,
    @SerialName("column") val column: Int
)
