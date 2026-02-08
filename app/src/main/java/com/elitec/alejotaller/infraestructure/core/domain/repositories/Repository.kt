package com.elitec.alejotaller.infraestructure.core.domain.repositories

import com.elitec.alejotaller.infraestructure.core.domain.entity.CoreEntity

interface Repository<T: CoreEntity> {
    suspend fun getAll(): List<T>
    suspend fun getById(itemId: String): T
    suspend fun save(item: T, onSave: () -> Unit = {})
    suspend fun delete(item: T, onDelete: () -> Unit = {})
    suspend fun modify(itemId: String, item: T, onModify: () -> Unit = {})
}