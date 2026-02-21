package com.elitec.alejotaller.infraestructure.core.data.bd

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.elitec.alejotaller.feature.category.data.dao.CategoryDao
import com.elitec.alejotaller.feature.category.data.dto.CategoryDto
import com.elitec.alejotaller.feature.notifications.data.dao.PromotionDao
import com.elitec.alejotaller.feature.notifications.data.dto.PromotionDto
import com.elitec.alejotaller.feature.product.data.dao.ProductDao
import com.elitec.alejotaller.feature.product.data.dto.ProductDto
import com.elitec.alejotaller.feature.sale.data.dao.SaleDao
import com.elitec.alejotaller.feature.sale.data.dto.SaleDto
import com.elitec.alejotaller.infraestructure.core.data.converters.DateTimeConverter
import com.elitec.alejotaller.infraestructure.core.data.converters.ListConverter

@Database(
    entities = [
        CategoryDto::class,
        ProductDto::class,
        SaleDto::class,
        PromotionDto::class
    ],
    version = 7,
)
@TypeConverters(
    DateTimeConverter::class,
    ListConverter::class,

)
abstract class AppBD: RoomDatabase() {
    abstract fun categoriesDao(): CategoryDao
    abstract fun productsDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun promotionDao(): PromotionDao
}
