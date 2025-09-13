plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10"
}

android {
    namespace = "com.sb.mycriptoanalisi"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sb.mycriptoanalisi"
        minSdk = 26
        targetSdk = 36
        buildFeatures{
            compose = true
        }


    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        getByName("release") {
            applicationIdSuffix = ""
            versionNameSuffix = "1.0beta"
        }
    }

    kotlin {
        jvmToolchain(17)
    }


}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.ui.text)
    implementation(libs.runtime.livedata)
    implementation(libs.foundation)
    implementation(libs.runtime)
    ksp(libs.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // DataStore
    implementation(libs.datastore.preferences)

    // Debug/Test
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
    androidTestImplementation(libs.compose.ui.test.junit4)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    configurations.all {
        exclude(group = "com.intellij", module = "annotations")
}
    testImplementation(kotlin("test"))
}