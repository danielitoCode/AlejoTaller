import java.io.FileInputStream
import java.util.Properties
import kotlin.apply

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidx.room)
}

// Leer local.properties
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}

val versionMajor = 0
val versionMinor = 13
val versionPatch = 0

val appVersionName = "$versionMajor.$versionMinor.$versionPatch"
val appVersionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch

android {
    namespace = "com.elitec.alejotallerscan"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.elitec.alejotallerscan"
        minSdk = 26
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName
        // Mejor compatibilidad y menor riesgo en previews.
        vectorDrawables {
            useSupportLibrary = true
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Evita conflictos con Ktor, Koin y otras libs.
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        debug {
            injectLocalProperties()
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            injectLocalProperties()

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(project(":shared-core"))
    implementation(project(":shared-auth"))
    implementation(project(":shared-sale"))
    implementation(project(":shared-data"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.icons.extended)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.compose.navigation)
    implementation(libs.multiplatform.toast)
    implementation(libs.shimer.compose)
    implementation(libs.konnection.status)
    implementation(libs.compose.window.size)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Icons
    implementation(libs.androidx.icons.extended)
    // Di
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.compose.navigation)
    // Permission
    implementation(libs.compose.permission)
    //ConnectionStatus
    implementation(libs.konnection.status)
    // Nav3
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.material3.adaptive.navigation3)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    // Realtime
    implementation(libs.pusher.java.client)
    // Datetime
    implementation(libs.kotlinx.datetime)
    // Toast
    implementation(libs.multiplatform.toast)
    // Persistence
    implementation(libs.sdk.for1.android) {
        exclude(group = "androidx.browser", module = "browser")
    }

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    ksp(libs.room.compiler)
    implementation(libs.androidx.appcompat)
    implementation(libs.okhttp)
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.mlkit.barcode.scanning)
    implementation(libs.posthog.android)
}

fun com.android.build.api.dsl.BuildType.injectLocalProperties() {
    fun prop(name: String) =
        buildConfigField(
            "String",
            name,
            "\"${localProperties.getProperty(name, "")}\""
        )
    // Appwrite
    prop("APPWRITE_DATABASE_ID")
    prop("CATEGORY_TABLE_ID")
    prop("PRODUCT_TABLE_ID")
    prop("SALE_TABLE_ID")
    prop("APPWRITE_PROJECT_ID")
    prop("APPWRITE_PROJECT_ENDPOINT")
    prop("APPWRITE_TELEGRAM_FUNCTION_URL")
    prop("APPWRITE_BUCKECT_ID")
    // Google
    prop("GOOGLE_CLOUD_WEBCLIENT")
    prop("GOOGLE_CLOUD_ANDROID_DEBUG")
    prop("GOOGLE_CLOUD_ANDROID_RELEASE")
    // Telegram
    prop("TELEGRAM_BOT_KEY")
    prop("TELEGRAM_CHAT_ID")
    prop("TELEGRAM_GROUP_NAME")
    prop("TELEGRAM_GROUP_TYPE")
    prop("TELEGRAM_API_URL")
    // Pusher
    prop("PUSHER_APP_ID")
    prop("PUSHER_API_SECRETS")
    prop("PUSHER_API_KEY")
    prop("PUSHER_CLUSTER")
    prop("PUSHER_NOTIFICATION_CHANNEL")
    prop("PUSHER_PROMO_CHANNEL")
    prop("PUSHER_SUPPORT_CHANNEL")
    prop("PUSHER_IA_CHANNEL")
    prop("PUSHER_SALE_CHANNEL")
    // Soluciones Cuba Pay
    prop("SOLUCIONES_CUBA_PAY_API_URL")
    prop("SOLUCIONES_CUBA_API_KEY")
    prop("SOLUCIONES_CUBA_MERCHANT_ID")
    prop("SOLUCIONES_CUBA_SUCCESS_URL")
    prop("SOLUCIONES_CUBA_CANCEL_URL")
    prop("SOLUCIONES_CUBA_CALLBACK_URL")
    // posthog
    prop("POSTHOG_TOKEN")
    prop("POSTHOG_HOST")
    // Publisher service
    prop("PUBLISHER_BASE_URL")
    prop("PUBLISHER_API_KEY")
}
