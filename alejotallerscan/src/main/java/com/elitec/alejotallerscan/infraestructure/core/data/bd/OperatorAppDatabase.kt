package com.elitec.alejotallerscan.infraestructure.core.data.bd

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.elitec.shared.data.feature.sale.data.dao.SaleDao
import com.elitec.shared.data.feature.sale.data.dto.SaleDto
import com.elitec.shared.data.infraestructure.core.data.converters.DateTimeConverter
import com.elitec.shared.data.infraestructure.core.data.converters.ListConverter

@Database(
    entities = [SaleDto::class],
    version = 1
)
@TypeConverters(
    DateTimeConverter::class,
    ListConverter::class
)
abstract class OperatorAppDatabase : RoomDatabase() {
    abstract fun saleDao(): SaleDao
}
