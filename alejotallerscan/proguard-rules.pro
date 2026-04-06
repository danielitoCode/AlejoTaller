# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

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
-keep class com.elitec.alejotallerscan.OperatorScanApplication { *; }
-keep class com.elitec.alejotallerscan.infraestructure.core.presentation.MainActivity { *; }
-keep class io.appwrite.views.CallbackActivity { *; }

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