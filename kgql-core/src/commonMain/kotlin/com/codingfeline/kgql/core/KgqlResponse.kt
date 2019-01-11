package com.codingfeline.kgql.core

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class KgqlResponse<T>(
    @Optional val data: T? = null,
    @Optional val errors: List<KgqlError>? = null
)

@Serializable
data class KgqlError(
    val message: String
)
