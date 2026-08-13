package com.elitec.alejotaller.infraestructure.core.data.bd

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object AppBDMigrations {
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
        }
    }
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE SaleDto ADD COLUMN deliveryType TEXT DEFAULT NULL"
            )
        }
    }
    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE SaleDto ADD COLUMN deliveryAddress TEXT DEFAULT NULL"
            )
        }
    }
    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `SaleDto_new` (
                    `id` TEXT NOT NULL,
                    `date` TEXT NOT NULL,
                    `amount` REAL NOT NULL,
                    `verified` TEXT NOT NULL,
                    `products` TEXT NOT NULL,
                    `userId` TEXT NOT NULL,
                    `customerName` TEXT,
                    `deliveryType` TEXT,
                    `deliveryAddress` TEXT,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `SaleDto_new` (
                    `id`,
                    `date`,
                    `amount`,
                    `verified`,
                    `products`,
                    `userId`,
                    `customerName`,
                    `deliveryType`,
                    `deliveryAddress`
                )
                SELECT
                    `id`,
                    `date`,
                    `amount`,
                    `verified`,
                    `products`,
                    `userId`,
                    NULL,
                    `deliveryType`,
                    `deliveryAddress`
                FROM `SaleDto`
                """.trimIndent()
            )
            db.execSQL("DROP TABLE `SaleDto`")
            db.execSQL("ALTER TABLE `SaleDto_new` RENAME TO `SaleDto`")
        }
    }

    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
        }
    }

    val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE ProductDto ADD COLUMN existence INTEGER NOT NULL DEFAULT 0"
            )
        }
    }

    val MIGRATION_11_12 = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
        }
    }

    val MIGRATION_12_13 = object : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE promotions ADD COLUMN productId TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE promotions ADD COLUMN kind TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE promotions ADD COLUMN status TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE promotions ADD COLUMN source TEXT DEFAULT NULL")
        }
    }

    val ALL: Array<Migration> = arrayOf(
        MIGRATION_5_6,
        MIGRATION_6_7,
        MIGRATION_7_8,
        MIGRATION_8_9,
        MIGRATION_9_10,
        MIGRATION_10_11,
        MIGRATION_11_12,
        MIGRATION_12_13
    )
}
