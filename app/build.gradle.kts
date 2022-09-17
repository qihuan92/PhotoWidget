import java.io.FileInputStream
import java.util.*

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp").version("1.7.10-1.0.6")
}

val localProperties = Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
}
val keyStoreFile = file(localProperties.getProperty("storeFilePath"))
val keyStorePassword: String? = localProperties.getProperty("storePassword")
val keyAlias: String? = localProperties.getProperty("keyAlias")
val keyPassword: String? = localProperties.getProperty("keyPassword")
val appCenterSecretRelease: String? = localProperties.getProperty("appCenterSecretRelease")
val appCenterSecretDebug: String? = localProperties.getProperty("appCenterSecretDebug")

android {
    signingConfigs {
        getByName("debug") {
            storeFile = keyStoreFile
            storePassword = keyStorePassword
            keyAlias = keyAlias
            keyPassword = keyPassword
        }
        create("release") {
            storeFile = keyStoreFile
            storePassword = keyStorePassword
            keyAlias = keyAlias
            keyPassword = keyPassword
        }
    }

    compileSdk = libs.versions.compilesdk.get().toInt()
    buildToolsVersion = libs.versions.build.tools.version.get()

    defaultConfig {
        applicationId = "com.qihuan.photowidget"
        minSdk = libs.versions.minsdk.get().toInt()
        targetSdk = libs.versions.targetsdk.get().toInt()
        versionCode = 38
        versionName = "1.37"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")

            buildConfigField("String", "APP_CENTER_SECRET", "\"${appCenterSecretRelease}\"")
        }

        debug {
            signingConfig = signingConfigs.getByName("debug")

            buildConfigField("String", "APP_CENTER_SECRET", "\"${appCenterSecretDebug}\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    applicationVariants.all {
        outputs.all {
            if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
                outputFileName = "photowidget_${buildType.name}_${defaultConfig.versionName}.apk"
            }
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.expandProjection", "true")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.test.junit)
    androidTestImplementation(libs.test.androidx.junit)
    androidTestImplementation(libs.test.androidx.espresso)

    implementation(libs.google.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment)

    implementation(libs.androidx.paging)
    testImplementation(libs.androidx.paging.testing)

    implementation(libs.androidx.room)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    testImplementation(libs.androidx.room.testing)

    // WorkManager 执行时会触发 AppWidgetProvider.onUpdate() 回调，导致不可控的行为。
    // 在 AppWidgetProvider.onUpdate() 通过 WorkManager 执行刷新微件，会导致无限循环，所以暂时改用 JobScheduler 代替。
    // 具体可见：https://medium.com/intive-developers/toss-a-coin-to-your-widget-or-dont-part-1-of-3-188c39d50b66
    // def work_version = "2.7.1"
    // implementation("androidx.work:work-runtime-ktx:$work_version")

    implementation(libs.ucrop)
    implementation(libs.compressor)
    implementation(libs.colorpickerview)

    implementation(libs.glide)
    kapt(libs.glide.compiler)

    implementation(libs.bundles.appcenter)
}