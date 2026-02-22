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

# Reglas de R8/ProGuard para release.
# Objetivo: permitir minificación/obfuscación sin romper reflexión,
# serialización ni componentes declarados por framework.

# Mantener metadata de anotaciones para librerías que la consultan en runtime.
-keepattributes *Annotation*,InnerClasses,EnclosingMethod,Signature

# Preservar nombres de archivos/líneas para mapear crashes con retrace.
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# ---------- KOTLINX SERIALIZATION ----------
# Evita que R8 elimine serializers generados usados en runtime.
-keepclassmembers class **$$serializer { *; }
-keepclassmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class * {
    *** Companion;
}
-keep @kotlinx.serialization.Serializable class *

# ---------- ROOM ----------
# Room utiliza clases/constructores/métodos generados que deben preservarse.
-keep class androidx.room.RoomDatabaseConstructor{ *; }
-keep class * extends androidx.room.RoomDatabase
-keep class * implements androidx.room.RoomDatabase$Callback
-keep @androidx.room.Database class *
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# ---------- APP COMPONENTS / REFLEXIÓN ----------
# Application/Activity/Service del proyecto (instanciados por Android mediante nombre).
-keep class com.elitec.alejotaller.TallerAlejoApp { *; }
-keep class com.elitec.alejotaller.infraestructure.core.presentation.MainActivity { *; }
-keep class com.elitec.alejotaller.infraestructure.core.presentation.services.OrderNotificationService { *; }

# Activity del SDK de Appwrite declarada en AndroidManifest.
-keep class io.appwrite.views.CallbackActivity { *; }

# ---------- APPWRITE (SDK & AUTH) ----------
# Evita errores de "create method not resolved" en servicios de Appwrite
-keep class io.appwrite.** { *; }
-keep interface io.appwrite.** { *; }
-dontwarn io.appwrite.**

# ---------- PUSHER (REAL-TIME) ----------
# Pusher utiliza reflexión para manejar eventos y conexiones
-keep class com.pusher.client.** { *; }
-keep class com.pusher.java_websocket.** { *; }
-dontwarn com.pusher.client.**

# ---------- KTOR (NETWORKING) ----------
# Necesario para el motor CIO y coroutines internas
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# ---------- VIEWMODELS & KOIN ----------
# Mantiene los constructores de ViewModels para que Koin pueda inyectarlos
-keep class com.elitec.alejotaller.**.*ViewModel { *; }
-keep class kotlin.Metadata { *; }

# ---------- DTOs & ENTITIES ----------
# Asegura que las clases de datos no pierdan campos críticos para JSON/DB
-keep @kotlinx.serialization.Serializable class com.elitec.alejotaller.** { *; }
-keep @androidx.room.Entity class com.elitec.alejotaller.** { *; }

# ---------- WARNINGS COMUNES ----------
-dontwarn javax.naming.**
-dontwarn org.slf4j.**
-dontwarn java.lang.invoke.StringConcatFactory

# Prevención de errores en el SDK de Appwrite (Reflexión)
-keep class io.appwrite.** { *; }
-keep interface io.appwrite.** { *; }
-dontwarn io.appwrite.**
# Si utilizas Ktor (que Appwrite usa internamente), también es recomendable:
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**