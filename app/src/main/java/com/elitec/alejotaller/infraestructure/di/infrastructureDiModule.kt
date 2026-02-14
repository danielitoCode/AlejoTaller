package com.elitec.alejotaller.infraestructure.di

import androidx.room.Room
import com.elitec.alejotaller.BuildConfig
import com.elitec.alejotaller.infraestructure.core.data.bd.AppBD
import com.elitec.alejotaller.infraestructure.core.data.repository.AppwriteSessionManager
import com.elitec.alejotaller.infraestructure.core.data.repository.GoogleAuthProviderImpl
import com.elitec.alejotaller.feature.auth.domain.ports.GoogleAuthProvider
import com.elitec.alejotaller.feature.auth.domain.ports.SessionManager
import com.elitec.alejotaller.infraestructure.core.presentation.viewmodel.ToasterViewModel
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Databases
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

    // GoogleSingInProvider
    single<GoogleAuthProvider> { GoogleAuthProviderImpl(androidContext()) }
    single<SessionManager> { AppwriteSessionManager(get()) }

    // ViewModels
    viewModel { ToasterViewModel() }
}