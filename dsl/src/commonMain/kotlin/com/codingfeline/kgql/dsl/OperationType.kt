package com.codingfeline.kgql.dsl

enum class OperationType(val value: String) {
    QUERY("query"),
    MUTATION("mutation"),
    SUBSCRIPTION("subscription")
}
