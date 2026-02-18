package com.elitec.alejotaller.infraestructure.di

import androidx.room.Room
import com.elitec.alejotaller.BuildConfig
import com.elitec.alejotaller.feature.auth.domain.caseuse.UpdateUserPhotoUrlCaseUse
import com.elitec.alejotaller.infraestructure.core.data.bd.AppBD
import com.elitec.alejotaller.infraestructure.core.data.repository.AppwriteSessionManager
import com.elitec.alejotaller.infraestructure.core.data.repository.GoogleAuthProviderImpl
import com.elitec.alejotaller.feature.auth.domain.ports.GoogleAuthProvider
import com.elitec.alejotaller.feature.auth.domain.ports.SessionManager
import com.elitec.alejotaller.infraestructure.core.data.realtime.PusherManager
import com.elitec.alejotaller.infraestructure.core.data.realtime.RealTimeManagerImpl
import com.elitec.alejotaller.infraestructure.core.presentation.viewmodel.ToasterViewModel
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val infrastructureModule = module {
    // AppWrite SDK
    single {
        Client(context = androidContext())
            .setEndpoint(BuildConfig.APPWRITE_PROJECT_ENDPOINT)
            .setProject(BuildConfig.APPWRITE_PROJECT_ID)
            // .setDevKey(getApiKey())
            .setSelfSigned(false)
    }
    single { Databases(get()) }
    single { Account(get()) }
    single { Storage(get()) }

    // Database
    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = AppBD::class.java,
            name = "app_database"
        )
            .build()
    }

    // Realtime
    single {
        Pusher(
            BuildConfig.PUSHER_API_KEY,
            PusherOptions()
                .setCluster(BuildConfig.PUSHER_CLUSTER))
    }
    single { PusherManager(get()) }
    single { RealTimeManagerImpl(get()) }

    // Networking
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    }
                )
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
        }
    }

    // GoogleSingInProvider
    single<GoogleAuthProvider> { GoogleAuthProviderImpl(androidContext()) }
    single<SessionManager> { AppwriteSessionManager(get()) }

    // ViewModels
    viewModel { ToasterViewModel() }
}