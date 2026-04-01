plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.hazard.koe"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.hazard.koe"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
        freeCompilerArgs.add("-java-parameters")
    }
}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.androidx.room.testing)

    //emoji
    implementation("com.vanniktech:emoji-ios:0.23.0")

    // Location
    implementation(libs.play.services.location)

    // Google Maps Compose
    implementation(libs.maps.compose)

    // ML Kit Text Recognition
    implementation("com.google.mlkit:text-recognition:16.0.1")

    // Coroutines Play Services (for .await() on ML Kit Tasks)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")

    // adds all APIs (recommended for bootstrapping new projects)
    implementation("com.composables:composeunstyled:1.49.6")

    // adds theming APIs
    implementation("com.composables:composeunstyled-theming:1.49.6")

    // adds component primitives for building components
    implementation("com.composables:composeunstyled-primitives:1.49.6")

    // adds themes for native look and feel
    implementation("com.composables:composeunstyled-platformtheme:1.49.6")

    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.11.0"))

    // Firebase AI Logic — no necesitas especificar versión con el BoM
    implementation("com.google.firebase:firebase-ai")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
