package com.elitec.alejotaller

import android.app.Application
import org.koin.core.context.startKoin

class TallerAlejoApp: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {

        }
    }
}