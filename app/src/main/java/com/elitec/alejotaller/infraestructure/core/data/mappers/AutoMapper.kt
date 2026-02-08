package com.elitec.alejotaller.infraestructure.core.data.mappers

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class AutoMapper(
    val domain: String,
    val renames: Array<AutoMapperRename> = []
)