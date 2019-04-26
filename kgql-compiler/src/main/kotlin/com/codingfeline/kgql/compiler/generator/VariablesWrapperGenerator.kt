package com.codingfeline.kgql.compiler.generator

import com.codingfeline.kgql.compiler.KgqlCustomTypeMapper
import com.codingfeline.kgql.core.internal.KgqlValue
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import graphql.language.VariableDefinition
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

private const val VARIABLES_CLASS_NAME = "Variables"

class VariablesWrapperGenerator(
    private val variables: List<VariableDefinition>,
    private val parentObjectName: ClassName,
    private val typeMapper: KgqlCustomTypeMapper
) {
    private val classifiedVariables = variables.map { VariableType.classify(it, typeMapper) }

    private val serializerGenerator by lazy { SerializerGenerator(typeMapper) }

    fun generateType(): TypeSpec {
        val classSpec = TypeSpec.classBuilder(VARIABLES_CLASS_NAME)
            .primaryConstructor(generateConstructor(classifiedVariables))
            .addProperties(generateProperties(classifiedVariables))
            .addFunctions(generateSetterFunctions(classifiedVariables))

        classSpec.addFunction(
            generateAsJsonObjectFunction(
                classifiedVariables,
                classSpec.propertySpecs.associateBy { it.name })
        )

        return classSpec.build()
    }

    private fun generateConstructor(variables: List<VariableType>): FunSpec {
        return FunSpec.constructorBuilder()
            .addParameters(
                // constructor parameters must be required type without default value.
                variables.filter { it is VariableType.Required }
                    .map {
                        ParameterSpec.builder(it.definition.name, it.type).build()
                    }
            )
            .build()
    }

    private fun generateProperties(variables: List<VariableType>): List<PropertySpec> {
        return variables.map { def -> def.generateProperty() }
    }

    private fun generateSetterFunctions(variables: List<VariableType>): List<FunSpec> {
        return variables.filter { it !is VariableType.Required }.map { it.generateSetterFunction(parentObjectName) }
    }

    private fun generateAsJsonObjectFunction(
        variables: List<VariableType>,
        properties: Map<String, PropertySpec>
    ): FunSpec {
        return FunSpec.builder("asJsonObject")
            .returns(JsonObject::class)
            .addCode(
                buildCodeBlock {
                    val jsonFun = MemberName("kotlinx.serialization.json", "json")
                    beginControlFlow("return %M", jsonFun)
                    variables.forEach { variable ->
                        val name = variable.definition.name
                        val property = properties.getValue(name)
                        if (variable is VariableType.Required) {
                            val serializedValue = serializedValueCodeBlock(property, variable, typeMapper, false)
                            addStatement(
                                "%S to %L",
                                name,
                                serializedValue
                            )
                        } else {
                            val itProp = PropertySpec.builder("it", variable.type).build()
                            val serializedValue = serializedValueCodeBlock(itProp, variable, typeMapper, true)
                            addStatement(
                                "(%N as? %T.Some)?.let { %S to %L }",
                                property,
                                KgqlValue::class.asTypeName(),
                                name,
                                serializedValue
                            )
                        }
                    }
                    endControlFlow()
                }
            )
            .build()
    }

    private fun serializedValueCodeBlock(
        property: PropertySpec,
        variable: VariableType,
        typeMapper: KgqlCustomTypeMapper,
        isOptional: Boolean
    ): CodeBlock {
        return if (typeMapper.isPrimitive(variable.type)) {
            CodeBlock.of("%N${if (isOptional) ".value" else ""}", property)
        } else {
            val serializer = serializerGenerator.generateCodeBlock(variable.type)
            CodeBlock.of(
                "%T.plain.toJson<%T>(%L, %N${if (isOptional) ".value" else ""})",
                Json::class,
                variable.type,
                serializer,
                property
            )
        }
    }

    sealed class VariableType {
        abstract val definition: VariableDefinition
        abstract val type: TypeName

        open fun generateProperty(): PropertySpec {
            return PropertySpec.builder(
                definition.name,
                KgqlValue::class.asTypeName().plusParameter(type),
                KModifier.PRIVATE
            )
                .mutable(true)
                .initializer("%T", KgqlValue.None::class)
                .build()
        }

        open fun generateSetterFunction(parentObjectName: ClassName): FunSpec {
            val param = ParameterSpec.builder(definition.name, type).build()
            return FunSpec.builder(definition.name)
                .addParameter(param)
                .returns(parentObjectName.nestedClass(VARIABLES_CLASS_NAME))
                .addCode(
                    CodeBlock.builder()
                        .addStatement("this.${definition.name} = %T(%N)", KgqlValue.Some::class, param)
                        .addStatement("return this")
                        .build()
                )
                .build()
        }

        open fun appendStatementForAsJsonObject(builder: CodeBlock.Builder) {

        }

        data class Required(
            override val definition: VariableDefinition,
            override val type: TypeName
        ) : VariableType() {
            override fun generateProperty(): PropertySpec {
                return PropertySpec.builder(definition.name, type, KModifier.PRIVATE)
                    .initializer(definition.name)
                    .build()
            }

            override fun generateSetterFunction(parentObjectName: ClassName): FunSpec {
                throw UnsupportedOperationException("Required Variable(${definition.name}) does not need setter")
            }
        }

        data class NonNullOptional(
            override val definition: VariableDefinition,
            override val type: TypeName
        ) : VariableType() {

        }

        data class Optional(
            override val definition: VariableDefinition,
            override val type: TypeName
        ) : VariableType() {

        }

        companion object {
            fun classify(variable: VariableDefinition, typeMapper: KgqlCustomTypeMapper): VariableType {
                val type = typeMapper.get(variable.type)
                return if (!type.isNullable && variable.defaultValue == null) {
                    // required and non-null
                    Required(variable, type)
                } else if (!type.isNullable) {
                    // non-null, but optional
                    NonNullOptional(variable, type)
                } else {
                    // optional
                    Optional(variable, type)
                }
            }
        }
    }
}
