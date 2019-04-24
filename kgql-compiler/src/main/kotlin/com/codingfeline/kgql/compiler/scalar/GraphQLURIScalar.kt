package com.codingfeline.kgql.compiler.scalar

import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.GraphQLScalarType

class GraphQLURIScalar : GraphQLScalarType(
    "URI",
    "URI as String",
    object : Coercing<String, String> {
        override fun serialize(dataFetcherResult: Any?): String {
            return dataFetcherResult as String
        }

        override fun parseValue(input: Any?): String {
            return if (input is String) {
                input
            } else {
                throw CoercingParseValueException("Expected a 'String' but was '${typeName(input)}'.")
            }
        }

        override fun parseLiteral(input: Any?): String {
            if (input !is StringValue) {
                throw CoercingParseLiteralException("Expected AST type 'StringValue' but was '${typeName(input)}'.")
            }
            return (input as StringValue).value
        }
    }
)
