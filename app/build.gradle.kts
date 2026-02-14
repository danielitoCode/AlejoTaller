import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.androidx.room)
    // alias(libs.plugins.kotzilla)
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
    namespace = "com.elitec.alejotaller"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.elitec.alejotaller"
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
            isMinifyEnabled = false

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

dependencies {
    // implementation(libs.kotzilla.sdk)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    // Toast
    implementation(libs.multiplatform.toast)
    // HttpClient
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.serialization.kotlinx.json) { //Esto es para evitar conflictos con el R8
        exclude(group = "com.fasterxml.jackson.core")
        exclude(group = "com.fasterxml.jackson.dataformat", module = "jackson-dataformat-xml")
    }
    implementation(libs.ktor.client.logs)
    implementation(libs.ktor.client.websocket)
    implementation(libs.ktor.client.test)
    // Lotties
    implementation(libs.lotties.compose)
    // Icons
    implementation(libs.androidx.icons.extended)
    // Di
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.compose.navigation)
    implementation(libs.koin.test)
    // Google Identity
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    // Permission
    implementation(libs.compose.permission)
    // DataStore Preferences
    implementation(libs.androidx.datastore.preferences)
    // Shimmer effect
    implementation(libs.shimer.compose)
    //ConnectionStatus
    implementation(libs.konnection.status)
    // Nav3
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.material3.adaptive.navigation3)
    implementation(libs.kotlinx.serialization.core)
    // Real time
    // implementation(libs.okhttp)
    // implementation(libs.okio)
    implementation(libs.pusher.java.client)
    /*implementation(libs.courier.android) {
        exclude(group = "androidx.test.ext", module = "junit")
    }*/
    // Network Images
    implementation(libs.coil.compose)
    implementation(libs.coil.okhttp)
    implementation(libs.coil.compose.svg)
    // Blur effect
    implementation(libs.compose.blurEffect.core)
    implementation(libs.compose.blurEffect.materials)
    // Persistence
    implementation(libs.sdk.for1.android) {
        exclude(group = "androidx.browser", module = "browser")
    }
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    ksp(libs.room.compiler)
    // Datetime
    implementation(libs.kotlinx.datetime)
    // Browser
    implementation(libs.androidx.browser)
    ksp(project(":mapper-processor"))
}

room {
    schemaDirectory("$projectDir/schemas")
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
    prop("PUSHER_API_KEY")
    prop("PUSHER_CLUSTER")
    prop("PUSHER_NOTIFICATION_CHANNEL")
    prop("PUSHER_PROMO_CHANNEL")
    prop("PUSHER_SUPPORT_CHANNEL")
    prop("PUSHER_IA_CHANNEL")
    prop("PUSHER_SALE_CHANNEL")
}
