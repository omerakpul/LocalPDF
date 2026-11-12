import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

// Version comes from the release tag (APP_VERSION=1.2.3, set by the release workflow).
// Local builds fall back to 1.0.0. versionCode = major * 10000 + minor * 100 + patch.
val appVersion: String = System.getenv("APP_VERSION")?.takeIf { it.isNotBlank() } ?: "1.0.0"
val appVersionCode: Int = appVersion.split(".")
    .map { part -> part.takeWhile { it.isDigit() }.toIntOrNull() ?: 0 }
    .let { parts -> (parts.getOrElse(0) { 0 } * 10000) + (parts.getOrElse(1) { 0 } * 100) + parts.getOrElse(2) { 0 } }
    .coerceAtLeast(1)

android {
    namespace = "com.omerakpul.localpdf"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.omerakpul.localpdf"
        minSdk = 26
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

// Hilt's annotation processor ships an older kotlin-metadata reader than the Kotlin version used
// to compile this app. Force the matching reader so it can read the generated metadata.
configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin" && requested.name == "kotlin-metadata-jvm") {
            useVersion(libs.versions.kotlin.get())
        }
    }
}

// Room exports its schema so future migrations can be written and tested against it
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Compose + Material3
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.window.size)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.androidx.compose.material.icons.extended)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    // Hilt + Navigation
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // UI components
    implementation(libs.shadcn.compose)

    // Image loading
    implementation(libs.coil.compose)

    // PDF / Office
    implementation(libs.pdfbox.android)
    implementation(libs.apache.poi)
    implementation(libs.apache.poi.ooxml)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}