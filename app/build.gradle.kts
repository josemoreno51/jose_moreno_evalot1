plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.pps"
    compileSdk = 36  // CAMBIA ESTO de 34 a 36

    defaultConfig {
        applicationId = "com.example.pps"
        minSdk = 23
        targetSdk = 36  // También actualiza esto a 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Firebase BOM (Bill of Materials)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Firebase sin versión (usa la del BOM)
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx") // ← ESTA FALTABA

    // Firebase Auth sin ktx (para compatibilidad)
    implementation("com.google.firebase:firebase-auth")

    // Google Play Services Auth
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // AndroidX - ACTUALIZA ESTAS VERSIONES
    implementation("androidx.core:core-ktx:1.12.0") // Versión compatible con compileSdk 36
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.activity:activity-ktx:1.8.2") // Versión compatible
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}