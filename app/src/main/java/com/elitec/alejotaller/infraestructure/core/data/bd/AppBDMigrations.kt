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
    val ALL: Array<Migration> = arrayOf(
        MIGRATION_5_6,
        MIGRATION_6_7//, MIGRATION_7_8, etc.
    )
}