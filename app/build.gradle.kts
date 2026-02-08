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

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "APPWRITE_DATABASE_ID", "\"${localProperties.getProperty("APPWRITE_DATABASE_ID")}\"")
            buildConfigField("String", "CATEGORY_TABLE_ID", "\"${localProperties.getProperty("CATEGORY_TABLE_ID")}\"")
            buildConfigField("String", "PRODUCT_TABLE_ID", "\"${localProperties.getProperty("PRODUCT_TABLE_ID")}\"")
            buildConfigField("String", "SALE_TABLE_ID", "\"${localProperties.getProperty("SALE_TABLE_ID")}\"")
        }
        release {
            isMinifyEnabled = false

            buildConfigField("String", "APPWRITE_DATABASE_ID", "\"${localProperties.getProperty("APPWRITE_DATABASE_ID")}\"")
            buildConfigField("String", "CATEGORY_TABLE_ID", "\"${localProperties.getProperty("CATEGORY_TABLE_ID")}\"")
            buildConfigField("String", "PRODUCT_TABLE_ID", "\"${localProperties.getProperty("PRODUCT_TABLE_ID")}\"")
            buildConfigField("String", "SALE_TABLE_ID", "\"${localProperties.getProperty("SALE_TABLE_ID")}\"")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
    // Network Images
    implementation(libs.coil.compose)
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
    // Generations
    implementation(libs.kotlinpoet)
    ksp(project(":mapper-processor"))
}

room {
    schemaDirectory("$projectDir/schemas")
}
