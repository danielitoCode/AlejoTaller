package com.elitec.alejotaller

import android.app.Application
import com.elitec.alejotaller.feature.category.di.categoryFeatureModule
import com.elitec.alejotaller.feature.product.di.productFeatureModule
import com.elitec.alejotaller.infraestructure.di.infraestructureModule
import io.kotzilla.sdk.analytics.koin.analytics
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TallerAlejoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TallerAlejoApp)
            analytics()
            infraestructureModule
            categoryFeatureModule
            productFeatureModule
        }
    }
}