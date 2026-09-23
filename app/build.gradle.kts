@file:Suppress("MISSING_DEPENDENCY_CLASS_IN_EXPRESSION_TYPE")

import org.gradle.api.JavaVersion
import java.util.Properties

// Local dev reads keystore.properties (gitignored); CI (Codemagic) sets these as environment
// variables instead, since it injects the keystore file itself rather than checking one in.
// Missing entirely just means an unsigned release build — fine for a local debug-only checkout.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}
fun signingProp(propKey: String, envKey: String): String? =
    keystoreProperties.getProperty(propKey) ?: System.getenv(envKey)

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.serialization")

    id("io.sentry.android.gradle") version "6.22.0"
}

android {
    namespace = "com.alifba.alifba"
    compileSdk = 36

    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion("1.9.25")
            }
        }
    }

    defaultConfig {
        applicationId = "com.alifba.alifba"
        minSdk = 25
        //noinspection EditedTargetSdkVersion
        targetSdk = 36
        versionCode = 15
        versionName = "2.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }


    }
    sourceSets {
        //getByName("main").java.srcDirs("build/generated/source/kapt/main")
    }

    signingConfigs {
        create("release") {
            val storeFilePath = signingProp("storeFile", "CM_KEYSTORE_PATH")
            if (storeFilePath != null) {
                storeFile = file(storeFilePath)
                storePassword = signingProp("storePassword", "CM_KEYSTORE_PASSWORD")
                keyAlias = signingProp("keyAlias", "CM_KEY_ALIAS")
                keyPassword = signingProp("keyPassword", "CM_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    kapt {
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
//    packaging {
//        resources {
//            excludes += "/META-INF/{AL2.0,LGPL2.1}"
//        }
//    }
}
kapt {
    correctErrorTypes = true
    useBuildCache = true
    arguments {
        arg("jvmTarget", "17")
    }
    // Add compiler options for Kapt here
    javacOptions {
        option("-XaddExports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    // Core Android dependencies
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Use Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))

    // Compose dependencies without version numbers
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.runtime:runtime-livedata")

    // Material Components
    implementation("com.google.android.material:material:1.11.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Other dependencies
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.firebase:firebase-auth-ktx:23.0.0")
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.0")
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation ("com.google.firebase:firebase-messaging-ktx:24.1.0")
    implementation("com.posthog:posthog-android:3.+")

    // Lottie for animations
    implementation("com.airbnb.android:lottie-compose:4.0.0")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.2.2")
    implementation("io.coil-kt:coil-svg:2.2.2")
    implementation("io.coil-kt:coil-gif:2.2.2")
    
    // Shimmer effect
    implementation("com.valentinilk.shimmer:compose-shimmer:1.2.0")
    
    // Media notification support
    implementation("androidx.media:media:1.7.0")
    
    // Palette for color extraction
    implementation("androidx.palette:palette-ktx:1.0.0")
    
    // AndroidSVG for SVG parsing and rendering
    implementation("com.caverock:androidsvg-aar:1.4")

    // Splash screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-auth")

    // Dagger Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    implementation("androidx.test:runner:1.5.2")
    implementation("androidx.hilt:hilt-common:1.2.0")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Play Services Auth
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // LiveData and Coroutines
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // ExoPlayer
    implementation("com.google.android.exoplayer:exoplayer:2.19.1")

    implementation ("androidx.compose.compiler:compiler:1.5.3")
    implementation ("androidx.room:room-runtime:2.6.1")
    implementation ("androidx.room:room-ktx:2.6.1")

    // RevenueCat - core SDK
    implementation("com.revenuecat.purchases:purchases:9.15.1")

    // In-app review (Play Store)
    implementation("com.google.android.play:review:2.0.1")

    // Networking - Retrofit + Moshi
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("io.insert-koin:koin-android:3.4.2")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation ("com.onesignal:OneSignal:[5.0.0, 5.1.99]")
    kapt ("androidx.room:room-compiler:2.6.1")
}


sentry {
    org.set("alifba-ltd")
    projectName.set("alifba-android")

    // this will upload your source code to Sentry to show it as part of the stack traces
    // disable if you don't want to expose your sources
    includeSourceContext.set(true)
}
