import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.jetbrains.kotlinx.kover)
}

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
        vectorDrawables {
            useSupportLibrary = true
        }
        testInstrumentationRunner = "com.elitec.alejotaller.KoinTestRunner"
    }

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

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(project(":shared-core"))
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
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    testImplementation(libs.mock.test)
    testImplementation(libs.turbine.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit4)
    testImplementation(libs.ktor.client.test)
    androidTestImplementation(libs.koin.test)
    androidTestImplementation(libs.koin.test.junit4)
    androidTestImplementation(libs.koin.test.junit4)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.multiplatform.toast)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.serialization.kotlinx.json) {
        exclude(group = "com.fasterxml.jackson.core")
        exclude(group = "com.fasterxml.jackson.dataformat", module = "jackson-dataformat-xml")
    }
    implementation(libs.ktor.client.logs)
    implementation(libs.ktor.client.websocket)
    implementation(libs.ktor.client.test)
    implementation(libs.lotties.compose)
    implementation(libs.androidx.icons.extended)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.compose.navigation)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.compose.permission)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.shimer.compose)
    implementation(libs.konnection.status)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.material3.adaptive.navigation3)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.pusher.java.client)
    implementation(libs.coil.compose)
    implementation(libs.coil.okhttp)
    implementation(libs.coil.compose.svg)
    implementation(libs.compose.blurEffect.core)
    implementation(libs.compose.blurEffect.materials)
    implementation(libs.sdk.for1.android) {
        exclude(group = "androidx.browser", module = "browser")
    }
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    ksp(libs.room.compiler)
    implementation(libs.kotlinx.datetime)
    implementation(libs.compose.qr)
    implementation(libs.compose.window.size)
    implementation(libs.androidx.browser)
    ksp(project(":mapper-processor"))

    implementation(libs.posthog.android)
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*.R",
                    "*.R$*",
                    "*.BuildConfig",
                    "*.Manifest*",
                    "*.*Test*",
                    "*.ComposableSingletons*",
                    "*.Preview*",
                    "*.*_Factory*",
                    "*.*_Provide*Factory*",
                    "*.*_MembersInjector*",
                    "*.*Dao_Impl*",
                    "*.*MapperImpl*",
                    "*.Companion*"
                )
            }
        }

        verify {
            rule {
                minBound(45)
            }
        }
    }
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
    // Mistral Agent (Fase 1) — keys only in local.properties, never commit
    prop("MISTRAL_API_KEY")
    prop("MISTRAL_AGENT_ID")
    prop("MISTRAL_MODEL_ID")
}
