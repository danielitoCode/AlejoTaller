package com.elitec.alejotaller.core

import android.content.Context
import androidx.room.Room
import com.elitec.alejotaller.infraestructure.core.data.bd.AppBD

fun createTestDatabase(context: Context): AppBD {

    return Room.inMemoryDatabaseBuilder(
        context,
        AppBD::class.java
    )
        .allowMainThreadQueries()
        .build()
}