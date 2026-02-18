package com.elitec.alejotaller

import android.app.Application
import com.elitec.alejotaller.feature.auth.di.authFeatureDiModule
import com.elitec.alejotaller.feature.category.di.categoryFeatureModule
import com.elitec.alejotaller.feature.notifications.di.notificationsFeatureModule
import com.elitec.alejotaller.feature.product.di.productFeatureModule
import com.elitec.alejotaller.feature.sale.di.saleFeatureModule
import com.elitec.alejotaller.infraestructure.di.infrastructureModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TallerAlejoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TallerAlejoApp)
            modules(
                infrastructureModule,
                authFeatureDiModule,
                categoryFeatureModule,
                notificationsFeatureModule,
                productFeatureModule,
                saleFeatureModule
            )
        }
    }
}