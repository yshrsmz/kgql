package com.codingfeline.kgql.core

/**
 * Root interface for GraphQL request body
 */
public interface KgqlRequestBody<T> {
    public val operationName: String?
    public val query: String
    public val variables: T?
}
