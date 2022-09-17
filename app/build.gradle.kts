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

    compileSdk = 32
    buildToolsVersion = "32.0.0"

    defaultConfig {
        applicationId = "com.qihuan.photowidget"
        minSdk = 24
        targetSdk = 31
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
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.appcompat:appcompat:1.5.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    implementation("com.google.android.material:material:1.8.0-alpha01")
    implementation("androidx.activity:activity-ktx:1.5.1")
    implementation("androidx.fragment:fragment-ktx:1.5.2")

    val paging_version = "3.1.1"
    implementation("androidx.paging:paging-runtime-ktx:$paging_version")
    testImplementation("androidx.paging:paging-common-ktx:$paging_version")

    val room_version = "2.4.3"
    implementation("androidx.room:room-runtime:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    implementation("androidx.room:room-paging:$room_version")
    testImplementation("androidx.room:room-testing:$room_version")

    // WorkManager 执行时会触发 AppWidgetProvider.onUpdate() 回调，导致不可控的行为。
    // 在 AppWidgetProvider.onUpdate() 通过 WorkManager 执行刷新微件，会导致无限循环，所以暂时改用 JobScheduler 代替。
    // 具体可见：https://medium.com/intive-developers/toss-a-coin-to-your-widget-or-dont-part-1-of-3-188c39d50b66
    // def work_version = "2.7.1"
    // implementation("androidx.work:work-runtime-ktx:$work_version")

    implementation("com.github.yalantis:ucrop:2.2.7")
    implementation("id.zelory:compressor:3.0.1")
    implementation("com.github.skydoves:colorpickerview:2.2.4")

    val glide_version = "4.13.1"
    implementation("com.github.bumptech.glide:glide:${glide_version}")
    kapt("com.github.bumptech.glide:compiler:${glide_version}")

    val appCenterSdkVersion = "4.3.1"
    implementation("com.microsoft.appcenter:appcenter-analytics:${appCenterSdkVersion}")
    implementation("com.microsoft.appcenter:appcenter-crashes:${appCenterSdkVersion}")
}