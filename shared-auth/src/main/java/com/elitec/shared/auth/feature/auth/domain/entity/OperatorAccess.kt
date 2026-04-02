package com.elitec.shared.auth.feature.auth.domain.entity

private val OPERATOR_ALLOWED_ROLES = setOf("operator", "admin", "administrator", "owner")

fun String?.normalizeBusinessRole(): String? = this?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }

fun String?.hasOperatorAccess(): Boolean = normalizeBusinessRole() in OPERATOR_ALLOWED_ROLES

