package com.codingfeline.kgql.core

import kotlinx.serialization.json.JsonObject

/**
 * Root interface for GraphQL request body
 */
interface KgqlRequestBody {
    /**
     * Operation name.
     * If [query] contains more than two operation, you must specify this. Otherwise can be null.
     */
    val operationName: String?

    /**
     * Query document
     */
    val query: String

    /**
     * Parameters for [operationName] in [query]
     */
    val variables: JsonObject?
}
