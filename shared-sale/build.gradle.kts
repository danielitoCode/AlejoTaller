plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinSerialization)
}

android {
    namespace = "com.elitec.shared.sale"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":shared-core"))
    implementation(project(":shared-auth"))

    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.pusher.java.client)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logs)
    implementation(libs.sdk.for1.android)
    implementation(libs.koin.android)

    testImplementation(libs.junit)
    testImplementation(libs.mock.test)
    testImplementation(libs.turbine.test)
    testImplementation(libs.kotlinx.coroutines.test)
}
