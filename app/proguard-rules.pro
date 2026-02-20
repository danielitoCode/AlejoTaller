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

# ---------- KOIN ----------
# Koin funciona con referencias de tipo; mantenemos metadata Kotlin.
-keep class kotlin.Metadata { *; }

# ---------- WARNINGS COMUNES DE LIBRERÍAS ----------
# Evita ruido por clases opcionales de JDK/Android no presentes en runtime Android.
-dontwarn javax.naming.**
-dontwarn org.slf4j.**
-dontwarn java.lang.invoke.StringConcatFactory