plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.bibin.opnsense"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bibin.opnsense"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "2.0.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.activity.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi)
    implementation(libs.coroutines.android)
    implementation(libs.security.crypto)
    implementation(libs.biometric)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
}

// Copy release APK to /releases folder at project root
tasks.register<Copy>("buildRelease") {
    dependsOn("assembleRelease")
    from(layout.buildDirectory.dir("outputs/apk/release"))
    into(rootProject.layout.projectDirectory.dir("releases"))
    include("*.apk")
    rename { "OpnsenseManager-${android.defaultConfig.versionName}.apk" }
}
