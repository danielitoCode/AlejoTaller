package com.elitec.mapper_processor.mapper

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
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
            val renameMap = extractRenames(annotation)

            val mappedDtoKeys = dtoProperties.keys.map { renameMap[it] ?: it }.toSet()

            if (mappedDtoKeys != domainProperties.keys) {
                logger.error(
                    "AutoMapper requires matching property names for DTO and domain (after renames).",
                    dtoClass
                )
                return@forEach
            }

            if (!typesAreCompatible(dtoProperties, domainProperties, renameMap)) {
                logger.error(
                    "AutoMapper requires matching property types for DTO and domain (after renames).",
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
                .addFunction(
                    buildToDomain(dtoProperties, domainProperties, renameMap, dtoClassName, domainClassName)
                )
                .addFunction(
                    buildToDto(dtoProperties, domainProperties, renameMap, dtoClassName, domainClassName)
                )
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
        renameMap: Map<String, String>,
        dtoClassName: com.squareup.kotlinpoet.ClassName,
        domainClassName: com.squareup.kotlinpoet.ClassName
    ): FunSpec {
        val args = domainProperties.keys.joinToString(", ") { key ->
            val dtoKey = renameMap.entries.firstOrNull { it.value == key }?.key ?: key
            "$key = $dtoKey"
        }

        return FunSpec.builder("toDomain")
            .receiver(dtoClassName)
            .returns(domainClassName)
            .addStatement("return %T($args)", domainClassName)
            .build()
    }

    private fun buildToDto(
        dtoProperties: Map<String, KSPropertyDeclaration>,
        domainProperties: Map<String, KSPropertyDeclaration>,
        renameMap: Map<String, String>,
        dtoClassName: com.squareup.kotlinpoet.ClassName,
        domainClassName: com.squareup.kotlinpoet.ClassName
    ): FunSpec {
        val args = dtoProperties.keys.joinToString(", ") { key ->
            val domainKey = renameMap[key] ?: key
            "$key = $domainKey"
        }

        return FunSpec.builder("toDto")
            .receiver(domainClassName)
            .returns(dtoClassName)
            .addStatement("return %T($args)", dtoClassName)
            .build()
    }

    private fun extractRenames(annotation: com.google.devtools.ksp.symbol.KSAnnotation?): Map<String, String> {
        val renamesArg = annotation
            ?.arguments
            ?.firstOrNull { it.name?.asString() == "renames" }
            ?.value as? List<*>
            ?: emptyList<Any>()

        return renamesArg
            .mapNotNull { it as? com.google.devtools.ksp.symbol.KSAnnotation }
            .mapNotNull { rename ->
                val from = rename.arguments.firstOrNull { it.name?.asString() == "from" }?.value as? String
                val to = rename.arguments.firstOrNull { it.name?.asString() == "to" }?.value as? String
                if (from != null && to != null) from to to else null
            }
            .toMap()
    }

    private fun typesAreCompatible(
        dtoProperties: Map<String, KSPropertyDeclaration>,
        domainProperties: Map<String, KSPropertyDeclaration>,
        renameMap: Map<String, String>
    ): Boolean {
        return dtoProperties.all { (dtoKey, dtoProp) ->
            val domainKey = renameMap[dtoKey] ?: dtoKey
            val domainProp = domainProperties[domainKey] ?: return@all false
            val dtoType = dtoProp.type.resolve()
            val domainType = domainProp.type.resolve()
            areSameType(dtoType, domainType)
        }
    }

    private fun areSameType(dtoType: KSType, domainType: KSType): Boolean {
        if (dtoType.isMarkedNullable != domainType.isMarkedNullable) {
            return false
        }
        return dtoType.declaration.qualifiedName?.asString() ==
                domainType.declaration.qualifiedName?.asString()
    }
}