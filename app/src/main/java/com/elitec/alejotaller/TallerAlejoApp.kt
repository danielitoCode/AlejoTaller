package com.elitec.alejotaller

import android.app.Application
import com.elitec.alejotaller.feature.agent.di.agentFeatureModule
import com.elitec.alejotaller.feature.auth.di.authFeatureDiModule
import com.elitec.alejotaller.feature.category.di.categoryFeatureModule
import com.elitec.alejotaller.feature.exchange.di.exchangeFeatureModule
import com.elitec.alejotaller.feature.notifications.di.notificationsFeatureModule
import com.elitec.alejotaller.feature.product.di.productFeatureModule
import com.elitec.alejotaller.feature.sale.di.saleFeatureModule
import com.elitec.alejotaller.feature.settigns.di.settingsFeatureModule
import com.elitec.alejotaller.infraestructure.di.infrastructureModule
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TallerAlejoApp : Application() {
    companion object {
        const val POSTHOG_TOKEN = "phc_mqoyxYtN6gchS8snn6b6krrd6cVooX4vGkbiyscZ8gaV"
        const val POSTHOG_HOST = "https://us.i.posthog.com"
    }

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
                settingsFeatureModule,
                saleFeatureModule,
                exchangeFeatureModule,
                agentFeatureModule,
            )
        }
        val config = PostHogAndroidConfig(
            apiKey = POSTHOG_TOKEN,
            host = POSTHOG_HOST
        )

        config.sessionReplay = true
        config.sessionReplayConfig.maskAllTextInputs = true
        config.sessionReplayConfig.maskAllImages = true
        config.sessionReplayConfig.captureLogcat = true
        config.sessionReplayConfig.screenshot = true
        config.sessionReplayConfig.debouncerDelayMs = 1000

        PostHogAndroid.setup(this, config)
    }
}
