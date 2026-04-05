package com.elitec.alejotallerscan

import android.app.Application
import com.elitec.alejotallerscan.infraestructure.di.operatorScanDiModule
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class OperatorScanApplication : Application() {
    companion object {
        const val POSTHOG_TOKEN = "phc_mqoyxYtN6gchS8snn6b6krrd6cVooX4vGkbiyscZ8gaV"
        const val POSTHOG_HOST = "https://us.i.posthog.com"
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@OperatorScanApplication)
            modules(operatorScanDiModule)
        }

        val config = PostHogAndroidConfig(
            apiKey = POSTHOG_TOKEN,
            host = POSTHOG_HOST
        )

        /*
            Enable session recording. Requires enabling in your project settings as well.
            Default is false.
         */
        config.sessionReplay = true

        /*
            Whether text and text input fields are masked. Default is true.
            Password inputs are always masked regardless
        */
        config.sessionReplayConfig.maskAllTextInputs = true

        // Whether images are masked. Default is true.
        config.sessionReplayConfig.maskAllImages = true

        /* Capture logs automatically. Default is true.

            Support for remote configuration
            in the [session replay settings](https://app.posthog.com/settings/project-replay#replay-log-capture)
            requires SDK version 3.32.0 or higher.

        */
        config.sessionReplayConfig.captureLogcat = true

        /*
            Whether replays are created using high quality screenshots. Default is false.
            If disabled, replays are created using wireframes instead.
            The screenshot may contain sensitive information, so use with caution

        */
        config.sessionReplayConfig.screenshot = true

        // Throttle delay used to reduce the number of snapshots captured. Default is 1000ms
        config.sessionReplayConfig.debouncerDelayMs = 1000

        PostHogAndroid.setup(this, config)
    }
}
