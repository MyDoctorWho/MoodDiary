plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // 暂时注释掉KSP插件，直到找到兼容的版本
    // alias(libs.plugins.ksp)
    alias(libs.plugins.navigation.safeargs)
    id("kotlin-kapt") // 添加kotlin-kapt插件以支持注解处理
}

android {
    namespace = "com.example.mooddiary"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mooddiary"
        minSdk = 23
        targetSdk = 34
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
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    
    // Navigation
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler) // 将ksp改为annotationProcessor
    
    // Date/Time
    implementation(libs.kotlinx.datetime)
    
    // RecyclerView
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)
    
    // Charts
    implementation(libs.mpandroidchart)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}