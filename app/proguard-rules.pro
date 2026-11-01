# R8 rules for release builds (isMinifyEnabled = true). R8 shrinks, optimises and renames code;
# anything that looks a class or field up BY NAME at runtime has to be kept here, or it breaks
# only in release builds. Libraries (Firebase, RevenueCat, OneSignal, PostHog, Sentry, Lottie,
# Room, Hilt, WorkManager, Compose) ship their own rules.

# Readable crash reports: keep line numbers; the Sentry Gradle plugin uploads the mapping file
# so stack traces are de-obfuscated (needs SENTRY_AUTH_TOKEN, set in Codemagic).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Generic signatures and annotations, needed by the reflection-based code below
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*

# Firestore maps documents to these classes by field name (toObject(Badge::class.java), and any
# object passed to set()/update()). Renamed fields would read as empty and write as "a", "b"...
-keep class com.alifba.alifba.data.models.** { *; }
-keep class com.alifba.alifba.presenation.login.ParentAccount { *; }
-keep class com.alifba.alifba.presenation.login.ChildProfile { *; }

# Gift-code API JSON: Moshi with KotlinJsonAdapterFactory reads constructor parameter and
# property names through Kotlin metadata at runtime (no Moshi codegen in this project)
-keep class com.alifba.alifba.data.gifts.** { *; }
-keep class kotlin.Metadata { *; }

# Story cache (kotlinx.serialization): keep the generated serializers and their lookups
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keepclassmembers @kotlinx.serialization.Serializable class com.alifba.alifba.** {
    static <fields>;
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.alifba.alifba.**$$serializer { *; }
