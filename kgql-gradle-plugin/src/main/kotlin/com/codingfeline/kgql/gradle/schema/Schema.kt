package com.codingfeline.kgql.gradle.schema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Response(
    val data: ResponseData? = null
)

@Serializable
data class ResponseData(
    @SerialName("__schema") val schema: Schema

)

@Serializable
data class Schema(
    val queryType: Type? = null,
    val mutationType: Type? = null,
    val subscriptionType: Type? = null
)

@Serializable
data class Type(
    val name: String
)
