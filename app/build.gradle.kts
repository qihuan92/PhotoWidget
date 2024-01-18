import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
}

val localProperties = Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
}

android {
    signingConfigs {
        getByName("debug") {
            storeFile = file(localProperties.getProperty("storeFilePath"))
            storePassword = localProperties.getProperty("storePassword")
            keyAlias = localProperties.getProperty("keyAlias")
            keyPassword = localProperties.getProperty("keyPassword")
        }
        create("release") {
            storeFile = file(localProperties.getProperty("storeFilePath"))
            storePassword = localProperties.getProperty("storePassword")
            keyAlias = localProperties.getProperty("keyAlias")
            keyPassword = localProperties.getProperty("keyPassword")
        }
    }

    compileSdk = libs.versions.compilesdk.get().toInt()
    buildToolsVersion = libs.versions.build.tools.version.get()

    defaultConfig {
        applicationId = "com.qihuan.photowidget"
        minSdk = libs.versions.minsdk.get().toInt()
        targetSdk = libs.versions.targetsdk.get().toInt()
        versionCode = project.property("app.versionCode").toString().toInt()
        versionName = project.property("app.versionName").toString()

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
        }

        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
    namespace = "com.qihuan.photowidget"

    applicationVariants.all {
        outputs.all {
            if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
                outputFileName = "photowidget_${buildType.name}_${defaultConfig.versionName}.apk"
            }
        }
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:database"))
    implementation(project(":core:analysis"))
    implementation(project(":core:common"))

    implementation(project(":feature:about"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:link"))

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
}