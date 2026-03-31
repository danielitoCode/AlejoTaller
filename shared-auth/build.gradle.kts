plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinSerialization)
}

android {
    namespace = "com.elitec.shared.auth"
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

    implementation(libs.sdk.for1.android)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.koin.android)
    implementation(libs.kotlinx.serialization.core)

    testImplementation(libs.junit)
    testImplementation(libs.mock.test)
    testImplementation(libs.kotlinx.coroutines.test)
}
