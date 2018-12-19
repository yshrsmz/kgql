package com.codingfeline.kgql.dsl

fun query(
    operationName: String,
    variables: Set<Pair<String, InputValueType>> = emptySet(),
    builder: SelectionSet.() -> Unit): Operation {
    return Operation(
        type = OperationType.QUERY,
        name = operationName,
        variables = variables,
        fields = listOf()
    )
}

fun mutation(
    operationName: String,
    builder: () -> Unit): Operation {
    return Operation(
        type = OperationType.MUTATION,
        name = operationName,
        fields = listOf()
    )
}

fun subscription(
    operationName: String,
    builder: () -> Unit) {
}

