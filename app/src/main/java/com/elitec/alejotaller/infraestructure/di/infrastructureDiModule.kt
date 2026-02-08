package com.elitec.alejotaller.infraestructure.di

import androidx.room.Room
import com.elitec.alejotaller.infraestructure.core.data.bd.AppBD
import com.elitec.alejotaller.infraestructure.core.data.repository.AppwriteSessionManager
import com.elitec.alejotaller.infraestructure.core.data.repository.GoogleAuthProviderImpl
import com.elitec.alejotaller.feature.auth.domain.ports.GoogleAuthProvider
import com.elitec.alejotaller.feature.auth.domain.ports.SessionManager
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Databases
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val infrastructureModule = module {
    // AppWrite SDK
    single {
        Client(context = androidContext())
            .setEndpoint("https://nyc.cloud.appwrite.io/v1")
            .setProject("68f990d4002b6960ecf9") // Change in other versions
            // .setDevKey(getApiKey())
            .setSelfSigned(false)
    }
    single { Databases(get()) }
    single { Account(get()) }

    // Database
    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = AppBD::class.java,
            name = "app_database"
        )
            .build()
    }

    // GoogleSingInProvider
    single<GoogleAuthProvider> { GoogleAuthProviderImpl(androidContext()) }
    single<SessionManager> { AppwriteSessionManager(get()) }

}