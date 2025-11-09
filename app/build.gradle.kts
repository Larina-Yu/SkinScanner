plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.skinscanner"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.skinscanner"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui:1.5.3")
    implementation("androidx.compose.material:material:1.5.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.3")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.camera.view)
    implementation("androidx.compose.material:material-icons-extended:1.7.5")
    implementation("androidx.compose.ui:ui:1.6.0-alpha03")
    implementation("androidx.compose.material:material:1.6.0-alpha03")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.0-alpha03")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.1")

    // CameraX
    implementation("androidx.camera:camera-camera2:1.2.3")
    implementation("androidx.camera:camera-lifecycle:1.2.3")
    implementation("androidx.camera:camera-view:1.2.3")
    implementation("androidx.camera:camera-extensions:1.2.3")
}
