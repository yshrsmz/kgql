package com.codingfeline.kgql.core

import kotlinx.serialization.json.JsonObject

/**
 * Root interface for GraphQL request body
 */
interface KgqlRequestBody {
    val operationName: String?
    val query: String
    val variables: JsonObject?
}
