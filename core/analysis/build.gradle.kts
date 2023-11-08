import java.io.FileInputStream
import java.util.*

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

val localProperties = Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
}

android {
    namespace = "com.qihuan.photowidget.core.analysis"
    compileSdk = libs.versions.compilesdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minsdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            val appCenterSecretRelease = localProperties.getProperty("appCenterSecretRelease")
            buildConfigField("String", "APP_CENTER_SECRET", "\"${appCenterSecretRelease}\"")
        }
        debug {
            val appCenterSecretDebug = localProperties.getProperty("appCenterSecretDebug")
            buildConfigField("String", "APP_CENTER_SECRET", "\"${appCenterSecretDebug}\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    testImplementation(libs.test.junit)
    androidTestImplementation(libs.test.androidx.junit)
    androidTestImplementation(libs.test.androidx.espresso)

    implementation(libs.androidx.startup)
    implementation(libs.bundles.appcenter)
}