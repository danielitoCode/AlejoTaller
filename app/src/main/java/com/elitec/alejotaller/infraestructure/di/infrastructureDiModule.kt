package com.elitec.alejotaller.infraestructure.di

import android.content.Context
import androidx.room.Room
import com.elitec.alejotaller.infraestructure.core.data.bd.AppBD
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val infraestructureModule = module {
    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = AppBD::class.java,
            name = "app_database"
        )
            .build()
    }
}