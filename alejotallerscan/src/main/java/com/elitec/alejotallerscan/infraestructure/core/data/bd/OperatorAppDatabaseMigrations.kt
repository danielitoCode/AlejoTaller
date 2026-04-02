package com.elitec.alejotallerscan.infraestructure.core.data.bd

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object OperatorAppDatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS operator_sale_record (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    saleId TEXT NOT NULL,
                    customerName TEXT,
                    userId TEXT NOT NULL,
                    amount REAL NOT NULL,
                    saleDate TEXT NOT NULL,
                    action TEXT NOT NULL,
                    stateAfter TEXT NOT NULL,
                    recordedAtEpochMillis INTEGER NOT NULL,
                    itemsSummary TEXT NOT NULL
                )
                """.trimIndent()
            )
        }
    }
}
