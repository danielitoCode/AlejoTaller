package com.elitec.alejotaller.infraestructure.core.data.bd

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object AppBDMigrations {
    /**
     * Ejemplo de migración de versión 5 → 6.
     * Reemplaza este bloque con los cambios reales del esquema.
     * Consulta app/schemas/ para ver el diff entre versiones.
     */
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Ejemplo: columna agregada en SaleDto entre v5 y v6
            // db.execSQL("ALTER TABLE sale ADD COLUMN new_column TEXT NOT NULL DEFAULT ''")
            // Si no hubo cambios de esquema estructurales entre estas versiones,
            // se puede dejar el cuerpo vacío (solo sube el número de versión).
        }
    }
    // ← NUEVA migración para el campo deliveryType
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
    val ALL: Array<Migration> = arrayOf(
        MIGRATION_5_6,
        MIGRATION_6_7,
        MIGRATION_7_8,
        MIGRATION_8_9
    )
}
