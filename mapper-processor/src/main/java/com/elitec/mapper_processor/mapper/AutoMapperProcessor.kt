package com.elitec.mapper_processor.mapper

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

class AutoMapperProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotationName = "com.elitec.alejotaller.infraestructure.mapper.AutoMapper"
        val symbols = resolver.getSymbolsWithAnnotation(annotationName)
        val invalid = mutableListOf<KSAnnotated>()

        symbols.filterIsInstance<KSClassDeclaration>().forEach { dtoClass ->
            if (!dtoClass.validate()) {
                invalid.add(dtoClass)
                return@forEach
            }

            val annotation = dtoClass.annotations.firstOrNull {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationName
            }

            val domainQualifiedName = annotation
                ?.arguments
                ?.firstOrNull { it.name?.asString() == "domain" }
                ?.value as? String

            if (domainQualifiedName.isNullOrBlank()) {
                logger.error("AutoMapper requires a domain qualified name.", dtoClass)
                return@forEach
            }

            val domainClass = resolver.getClassDeclarationByName(
                resolver.getKSNameFromString(domainQualifiedName)
            )

            if (domainClass == null) {
                logger.error("Domain class not found: $domainQualifiedName", dtoClass)
                return@forEach
            }

            val dtoProperties = dtoClass.getAllProperties().associateBy { it.simpleName.asString() }
            val domainProperties = domainClass.getAllProperties().associateBy { it.simpleName.asString() }

            if (dtoProperties.keys != domainProperties.keys) {
                logger.error(
                    "AutoMapper requires matching property names for DTO and domain.",
                    dtoClass
                )
                return@forEach
            }

            val dtoClassName = dtoClass.toClassName()
            val domainClassName = domainClass.toClassName()
            val mapperFile = FileSpec.builder(
                dtoClass.packageName.asString(),
                "${dtoClass.simpleName.asString()}AutoMapper"
            )
                .addFunction(buildToDomain(dtoProperties, domainProperties, dtoClassName, domainClassName))
                .addFunction(buildToDto(dtoProperties, domainProperties, dtoClassName, domainClassName))
                .build()

            mapperFile.writeTo(
                codeGenerator,
                Dependencies(aggregating = false, dtoClass.containingFile!!)
            )
        }

        return invalid
    }

    private fun buildToDomain(
        dtoProperties: Map<String, KSPropertyDeclaration>,
        domainProperties: Map<String, KSPropertyDeclaration>,
        dtoClassName: com.squareup.kotlinpoet.ClassName,
        domainClassName: com.squareup.kotlinpoet.ClassName
    ): FunSpec {
        val args = domainProperties.keys.joinToString(", ") { key -> "$key = $key" }

        return FunSpec.builder("toDomain")
            .receiver(dtoClassName)
            .returns(domainClassName)
            .addStatement("return %T($args)", domainClassName)
            .build()
    }

    private fun buildToDto(
        dtoProperties: Map<String, KSPropertyDeclaration>,
        domainProperties: Map<String, KSPropertyDeclaration>,
        dtoClassName: com.squareup.kotlinpoet.ClassName,
        domainClassName: com.squareup.kotlinpoet.ClassName
    ): FunSpec {
        val args = dtoProperties.keys.joinToString(", ") { key -> "$key = $key" }

        return FunSpec.builder("toDto")
            .receiver(domainClassName)
            .returns(dtoClassName)
            .addStatement("return %T($args)", dtoClassName)
            .build()
    }

}