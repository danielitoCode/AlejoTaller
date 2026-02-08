package com.elitec.alejotaller.feature.product.data.repository

import com.elitec.alejotaller.feature.category.data.mapper.toDomain
import com.elitec.alejotaller.feature.product.data.dao.ProductDao
import com.elitec.alejotaller.feature.product.data.dto.ProductDto
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductOfflineFirstRepository(
    private val net: ProductNetRepositoryImpl,
    private val bd: ProductDao
): ProductRepository {
    override suspend fun getAll(): List<Product> {
        TODO("Not yet implemented")
    }

    /* bd.getAllFlow().map { categoriesDtoList ->
         categoriesDtoList.map { categoryDto ->
             categoryDto.toDomain()
         }
     }*/


    override suspend fun getById(itemId: String): Product {
        TODO("Not yet implemented")
    }

}