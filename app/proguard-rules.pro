# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Reglas de R8/ProGuard para release.
# Objetivo: permitir minificación/obfuscación sin romper reflexión,
# serialización ni componentes declarados por framework.

# Mantener metadata consultada en runtime por Kotlin/serialización/reflexión.
-keepattributes *Annotation*,InnerClasses,EnclosingMethod,Signature

# Preservar nombres de archivo y líneas para retrace de crashes.
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# ---------- KOTLINX SERIALIZATION ----------
-keepclassmembers class **$$serializer { *; }
-keepclassmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class * {
    *** Companion;
}
-keep @kotlinx.serialization.Serializable class *

# ---------- ROOM ----------
-keep class androidx.room.RoomDatabaseConstructor { *; }
-keep class * extends androidx.room.RoomDatabase
-keep class * implements androidx.room.RoomDatabase$Callback
-keep @androidx.room.Database class *
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# ---------- APP COMPONENTS (ANDROID FRAMEWORK) ----------
# Instanciados por nombre desde AndroidManifest o framework.
-keep class com.elitec.alejotaller.TallerAlejoApp { *; }
-keep class com.elitec.alejotaller.infraestructure.core.presentation.MainActivity { *; }
-keep class com.elitec.alejotaller.infraestructure.core.presentation.services.OrderNotificationService { *; }
-keep class io.appwrite.views.CallbackActivity { *; }

# ---------- AUTENTICACIÓN (GOOGLE + APPWRITE) ----------
# Reglas acotadas para evitar errores release tipo:
# "Function 'create' (...) not resolved" durante login Google/Appwrite.
-keep class com.elitec.alejotaller.infraestructure.core.data.repository.GoogleAuthProviderImpl { *; }
-keep class com.elitec.alejotaller.infraestructure.core.data.repository.AppwriteSessionManager { *; }
-keep class com.elitec.alejotaller.feature.auth.data.repository.AccountRepositoryImpl { *; }
-keep class com.elitec.alejotaller.feature.auth.data.repository.FileUploadRepoImpl { *; }
-keep interface com.elitec.alejotaller.feature.auth.domain.repositories.AccountRepository { *; }
-keep interface com.elitec.alejotaller.feature.auth.domain.repositories.FileRepository { *; }
-keep interface com.elitec.alejotaller.feature.auth.domain.ports.GoogleAuthProvider { *; }
-keep interface com.elitec.alejotaller.feature.auth.domain.ports.SessionManager { *; }

# ---------- APPWRITE ----------
-keep class io.appwrite.** { *; }
-keep interface io.appwrite.** { *; }
-dontwarn io.appwrite.**

# ---------- KTOR ----------
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# ---------- PUSHER ----------
-keep class com.pusher.client.** { *; }
-keep class com.pusher.java_websocket.** { *; }
-dontwarn com.pusher.client.**

# ---------- KOIN / VIEWMODELS ----------
-keep class com.elitec.alejotaller.**.*ViewModel { *; }
-keep class kotlin.Metadata { *; }

# ---------- WARNINGS COMUNES ----------
-dontwarn javax.naming.**
-dontwarn org.slf4j.**
-dontwarn java.lang.invoke.StringConcatFactory