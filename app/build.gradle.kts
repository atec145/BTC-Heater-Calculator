import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val localProps = Properties()
val localPropsFile = rootProject.file("local.properties")
if (localPropsFile.exists()) localProps.load(localPropsFile.inputStream())

android {
    namespace = "com.example.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = localProps.getProperty("KEYSTORE_PATH")?.let { rootProject.file(it) }
            storePassword = localProps.getProperty("KEYSTORE_PASSWORD")
            keyAlias = localProps.getProperty("KEY_ALIAS")
            keyPassword = localProps.getProperty("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null) signingConfig = releaseSigning
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
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Retrofit + OkHttp + Gson
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.retrofit.scalars)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Coil
    implementation(libs.coil.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Testing
    testImplementation(libs.junit)
}
