package com.elitec.alejotaller.infraestructure.core.data.mappers

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class AutoMapperRename(
    val from: String,
    val to: String
)