package com.codingfeline.kgql.compiler.generator

import com.codingfeline.kgql.compiler.KgqlCustomTypeMapper
import com.codingfeline.kgql.core.internal.KgqlValue
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import graphql.language.VariableDefinition
import kotlinx.serialization.internal.EnumSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

private const val VARIABLES_CLASS_NAME = "Variables"

class VariablesWrapperGenerator2(
    private val variables: List<VariableDefinition>,
    private val parentObjectName: ClassName,
    private val typeMapper: KgqlCustomTypeMapper
) {
    private val classifiedVariables = variables.map { VariableType.classify(it, typeMapper) }

    fun generateType(): TypeSpec {
        val classSpec = TypeSpec.classBuilder(VARIABLES_CLASS_NAME)
            .primaryConstructor(generateConstructor(classifiedVariables))
            .addProperties(generateProperties(classifiedVariables))
            .addFunctions(generateSetterFunctions(classifiedVariables))

        generateAsJsonObjectFunction(classifiedVariables, classSpec.propertySpecs.associateBy { it.name })

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
                    val isEnumSubclass = MemberName("com.codingfeline.kgql.core.internal", "isEnumSubClass")
                    beginControlFlow("%M", jsonFun)
                    variables.forEach { variable ->
                        val name = variable.definition.name
                        val property = properties[name]
                        if (variable is VariableType.Required) {
                            when (variable.type.copy(nullable = false)) {
                                String::class.asTypeName(),
                                Int::class.asTypeName(),
                                Float::class.asTypeName() -> {
                                    addStatement(
                                        "%S to %N",
                                        name,
                                        property
                                    )
                                }
                                else -> {
                                    beginControlFlow("val serializer = if (%M(%N))", isEnumSubclass, property)
                                    addStatement("%T", EnumSerializer::class.asTypeName())
                                    nextControlFlow("else")

                                    endControlFlow()

                                    addStatement(
                                        "%S to %T.plain.toJson(%T.serializer(), %N)",
                                        name,
                                        Json::class,
                                        variable.type,
                                        property
                                    )
                                }
                            }
                        } else {
                            variable.definition.type
                        }
                    }
                    endControlFlow()
                }
            )
            .build()
    }

    sealed class VariableType {
        abstract val definition: VariableDefinition
        abstract val type: TypeName

        open fun generateProperty(): PropertySpec {
            return PropertySpec.builder(definition.name, KgqlValue::class.asTypeName().plusParameter(type))
                .initializer("%T", KgqlValue.None)
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
                return PropertySpec.builder(definition.name, type)
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
