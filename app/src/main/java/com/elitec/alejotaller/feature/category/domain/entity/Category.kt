package com.elitec.alejotaller.feature.category.domain.entity

import com.elitec.alejotaller.infraestructure.core.domain.CoreEntity

data class Category (
    override val id: String,
    val name: String,
    val photoUrl: String?
): CoreEntity