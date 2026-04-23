package com.elitec.alejotallerscan

import android.app.Application
import com.elitec.alejotallerscan.infraestructure.di.operatorScanDiModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class OperatorScanApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@OperatorScanApplication)
            modules(operatorScanDiModule)
        }
    }
}
