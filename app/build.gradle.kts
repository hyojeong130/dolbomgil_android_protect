plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.dive_pre"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.dive_pre"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }

        buildConfigField(
            "String",
            "KAKAO_NATIVE_APP_KEY",                               // ← 필드 이름
            "\"${project.findProperty("KAKAO_NATIVE_APP_KEY")}\"" // ← local.properties의 키 이름
        )
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
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation("androidx.navigation:navigation-compose:2.9.3")

//    implementation("com.kakaomobility.knsdk:knsdk:1.9.4")
    implementation("com.kakaomobility.knsdk:knsdk_ui:1.9.4")
//    implementation("com.kakaomobility.knsdk:knsdk_guidance:1.9.4")
//    implementation("com.kakaomobility.knsdk:knsdk_poi:1.9.4")

    // Compose <-> ViewBinding bridge
    implementation("androidx.compose.ui:ui-viewbinding:1.9.0")

    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    implementation("androidx.appcompat:appcompat:1.7.1")

    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
//    androidTestImplementation(platform(libs.androidx.compose.bom))
//    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}