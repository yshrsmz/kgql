package com.codingfeline.kgql.compiler.scalar

import graphql.Internal
import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import graphql.schema.GraphQLScalarType
import java.time.DateTimeException
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


@Internal
class GraphQLDateTimeScalar : GraphQLScalarType(
    "DateTime",
    "An RFC-3339 compliant DateTime Scalar",
    object : Coercing<OffsetDateTime, String> {
        @Throws(CoercingSerializeException::class)
        override fun serialize(input: Any): String {
            val offsetDateTime: OffsetDateTime
            if (input is OffsetDateTime) {
                offsetDateTime = input
            } else if (input is ZonedDateTime) {
                offsetDateTime = input.toOffsetDateTime()
            } else if (input is String) {
                offsetDateTime = parseOffsetDateTime(input.toString()) { CoercingSerializeException() }
            } else {
                throw CoercingSerializeException(
                    "Expected something we can convert to 'java.time.OffsetDateTime' but was '${typeName(input)}'."
                )
            }
            try {
                return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(offsetDateTime)
            } catch (e: DateTimeException) {
                throw CoercingSerializeException(
                    "Unable to turn TemporalAccessor into OffsetDateTime because of : '${e.message}'."
                )
            }

        }

        @Throws(CoercingParseValueException::class)
        override fun parseValue(input: Any): OffsetDateTime {
            val offsetDateTime: OffsetDateTime
            if (input is OffsetDateTime) {
                offsetDateTime = input
            } else if (input is ZonedDateTime) {
                offsetDateTime = input.toOffsetDateTime()
            } else if (input is String) {
                offsetDateTime = parseOffsetDateTime(input.toString()) { CoercingParseValueException() }
            } else {
                throw CoercingParseValueException("Expected a 'String' but was '${typeName(input)}'.")
            }
            return offsetDateTime
        }

        @Throws(CoercingParseLiteralException::class)
        override fun parseLiteral(input: Any): OffsetDateTime {
            if (input !is StringValue) {
                throw CoercingParseLiteralException(
                    "Expected AST type 'StringValue' but was '${typeName(input)}'."
                )
            }
            return parseOffsetDateTime((input as StringValue).value) { CoercingParseLiteralException() }
        }

        private fun parseOffsetDateTime(s: String, exceptionMaker: (String) -> RuntimeException): OffsetDateTime {
            try {
                return OffsetDateTime.parse(s, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            } catch (e: DateTimeParseException) {
                throw exceptionMaker("Invalid RFC3339 value : '${s}'. because of : '${e.message}'")
            }

        }
    })

internal fun typeName(input: Any?): String {
    if (input == null) {
        return "null"
    }
    return input.javaClass.simpleName
}

val q = """
query IntrospectionQuery {
    __schema {
        types {
            kind
            name
            description
            fields(includeDeprecated: true) {
                name
                description
                ar
            }
        }
    }
}
""".trimIndent()
