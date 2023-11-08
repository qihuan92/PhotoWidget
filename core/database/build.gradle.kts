plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp").version("1.8.10-1.0.9")
}

android {
    namespace = "com.qihuan.photowidget.core.database"
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
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.expandProjection", "true")
}

dependencies {
    implementation(project(":core:model"))

    implementation(libs.androidx.core.ktx)
    testImplementation(libs.test.junit)
    androidTestImplementation(libs.test.androidx.junit)
    androidTestImplementation(libs.test.androidx.espresso)

    implementation(libs.androidx.paging)
    testImplementation(libs.androidx.paging.testing)

    api(libs.androidx.room)
    ksp(libs.androidx.room.compiler)
    api(libs.androidx.room.ktx)
    api(libs.androidx.room.paging)
    testImplementation(libs.androidx.room.testing)

    implementation(libs.koin.android)
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit)
}