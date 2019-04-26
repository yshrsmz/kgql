package com.codingfeline.kgql.compiler.generator

import com.codingfeline.kgql.compiler.KgqlCustomTypeMapper
import com.google.common.truth.Truth
import com.squareup.kotlinpoet.TypeName
import graphql.language.OperationDefinition
import graphql.parser.Parser
import org.junit.Test


class SerializerGeneratorTest {

    @Test
    fun `generate serializer for nullable primitives`() {
        val query = """
            query test(${'$'}string: String,
                       ${'$'}int: Int,
                       ${'$'}float: Float,
                       ${'$'}boolean: Boolean) {
                item
            }
        """.trimIndent()

        val mapper = KgqlCustomTypeMapper(emptyMap(), emptySet())

        val types = getVariableTypes(query, mapper)

        val generator = SerializerGenerator(mapper)

        val serializers = types.map { generator.generateCodeBlock(it).toString() }

        Truth.assertThat(serializers)
            .containsExactly(
                "kotlinx.serialization.internal.NullableSerializer(kotlin.String.kotlinx.serialization.serializer())",
                "kotlinx.serialization.internal.NullableSerializer(kotlin.Int.kotlinx.serialization.serializer())",
                "kotlinx.serialization.internal.NullableSerializer(kotlin.Float.kotlinx.serialization.serializer())",
                "kotlinx.serialization.internal.NullableSerializer(kotlin.Boolean.kotlinx.serialization.serializer())"
            )
    }

    @Test
    fun `generate serializer for non-null primitives`() {
        val query = """
            query test(${'$'}string: String!,
                       ${'$'}int: Int!,
                       ${'$'}float: Float!,
                       ${'$'}boolean: Boolean!) {
                item
            }
        """.trimIndent()

        val mapper = KgqlCustomTypeMapper(emptyMap(), emptySet())

        val types = getVariableTypes(query, mapper)

        val generator = SerializerGenerator(mapper)

        val serializers = types.map { generator.generateCodeBlock(it).toString() }

        Truth.assertThat(serializers)
            .containsExactly(
                "kotlin.String.kotlinx.serialization.serializer()",
                "kotlin.Int.kotlinx.serialization.serializer()",
                "kotlin.Float.kotlinx.serialization.serializer()",
                "kotlin.Boolean.kotlinx.serialization.serializer()"
            )
    }

    @Test
    fun `generate serializer for List of primitives`() {
        val query = """
            query test(${'$'}stringList: [String],
                       ${'$'}intList: [Int!],
                       ${'$'}floatList: [Float]!,
                       ${'$'}booleanList: [Boolean!]!) {
                item
            }
        """.trimIndent()

        val mapper = KgqlCustomTypeMapper(emptyMap(), emptySet())

        val types = getVariableTypes(query, mapper)

        val generator = SerializerGenerator(mapper)

        val serializers = types.map { generator.generateCodeBlock(it).toString() }

        Truth.assertThat(serializers)
            .containsExactly(
                "kotlinx.serialization.internal.NullableSerializer(kotlinx.serialization.internal.NullableSerializer(kotlin.String.kotlinx.serialization.serializer()).kotlinx.serialization.list)",
                "kotlinx.serialization.internal.NullableSerializer(kotlin.Int.kotlinx.serialization.serializer().kotlinx.serialization.list)",
                "kotlinx.serialization.internal.NullableSerializer(kotlin.Float.kotlinx.serialization.serializer()).kotlinx.serialization.list",
                "kotlin.Boolean.kotlinx.serialization.serializer().kotlinx.serialization.list"
            )
    }

    @Test
    fun `generate serializer for custom types`() {
        val query = """
            query test(${'$'}customType: CustomType,
                       ${'$'}nonNullCustomType: CustomType!,
                       ${'$'}enumType: EnumType,
                       ${'$'}nonNullEnumType: EnumType!) {
                item
            }
        """.trimIndent()

        val mapper = KgqlCustomTypeMapper(
            typeMap = mapOf("CustomType" to "com.example.CustomType", "EnumType" to "com.example.EnumType"),
            enumNameSet = setOf("EnumType")
        )

        val types = getVariableTypes(query, mapper)
        val generator = SerializerGenerator(mapper)

        val serializers = types.map { generator.generateCodeBlock(it).toString() }

        Truth.assertThat(serializers)
            .containsExactly(
                "kotlinx.serialization.internal.NullableSerializer(com.example.CustomType.serializer())",
                "com.example.CustomType.serializer()",
                "kotlinx.serialization.internal.NullableSerializer(kotlinx.serialization.internal.EnumSerializer(com.example.EnumType::class))",
                "kotlinx.serialization.internal.EnumSerializer(com.example.EnumType::class)"
            )
    }

    @Test
    fun `generate serializer for List of custom types`() {

        val query = """
            query test(${'$'}customType: [CustomType],
                       ${'$'}nonNullCustomType: [CustomType!],
                       ${'$'}enumType: [EnumType],
                       ${'$'}nonNullEnumType: [EnumType!]) {
                item
            }
        """.trimIndent()

        val mapper = KgqlCustomTypeMapper(
            typeMap = mapOf("CustomType" to "com.example.CustomType", "EnumType" to "com.example.EnumType"),
            enumNameSet = setOf("EnumType")
        )

        val types = getVariableTypes(query, mapper)

        val generator = SerializerGenerator(mapper)

        val serializers = types.map { generator.generateCodeBlock(it).toString() }

        Truth.assertThat(serializers)
            .containsExactly(
                "kotlinx.serialization.internal.NullableSerializer(kotlinx.serialization.internal.NullableSerializer(com.example.CustomType.serializer()).kotlinx.serialization.list)",
                "kotlinx.serialization.internal.NullableSerializer(com.example.CustomType.serializer().kotlinx.serialization.list)",
                "kotlinx.serialization.internal.NullableSerializer(kotlinx.serialization.internal.NullableSerializer(kotlinx.serialization.internal.EnumSerializer(com.example.EnumType::class)).kotlinx.serialization.list)",
                "kotlinx.serialization.internal.NullableSerializer(kotlinx.serialization.internal.EnumSerializer(com.example.EnumType::class).kotlinx.serialization.list)"
            )
    }

    private fun getVariableTypes(query: String, mapper: KgqlCustomTypeMapper): List<TypeName> {
        val document = Parser().parseDocument(query)
        val q = document.definitions.first { it is OperationDefinition } as OperationDefinition
        return q.variableDefinitions.map { mapper.get(it.type) }
    }
}
