plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt") // Dagger dependency
    id("com.google.dagger.hilt.android") // Hilt dependency
}

android {
    namespace = "com.example.precastmanagementapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.precastmanagementapp"
        minSdk = 24
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.google.android.material:material:1.5.0") // Material 3
    implementation("com.google.dagger:dagger:2.57.1") // Dagger dependency
    kapt("com.google.dagger:dagger-compiler:2.57.1") // Dagger dependency
    implementation("com.google.dagger:hilt-android:2.56.2") // Hilt dependency
    kapt("com.google.dagger:hilt-android-compiler:2.56.2") // Hilt dependency
}