package com.elitec.mapper_processor.mapper

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class AutoMapperProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return AutoMapperProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
    }
}