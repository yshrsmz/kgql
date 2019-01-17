package com.codingfeline.kgql.core

/**
 * Root interface for GraphQL request body
 */
interface KgqlRequestBody<T> {
    val operationName: String?
    val query: String
    val variables: T?
}
