package com.elitec.alejotaller.infraestructure.core.data.bd

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.elitec.alejotaller.feature.category.data.dao.CategoryDao
import com.elitec.alejotaller.feature.category.data.dto.CategoryDto
import com.elitec.alejotaller.feature.category.domain.entity.Category
import com.elitec.alejotaller.feature.product.data.dto.ProductDao
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.sale.data.dto.SaleDto
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.infraestructure.core.data.converters.DateTimeConverter

@Database(
    entities = [
        CategoryDto::class,
        Product::class,
        Sale::class
    ],
    version = 2,
)
@TypeConverters(
    DateTimeConverter::class
)
abstract class AppBD: RoomDatabase() {
    abstract fun categoriesDao(): CategoryDao
    abstract fun productsDao(): ProductDao
    abstract fun saleDao(): SaleDto
}