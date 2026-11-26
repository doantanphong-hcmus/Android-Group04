plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.ksp)
    id("org.jlleitschuh.gradle.ktlint")
}

val apiBaseUrl =
    providers
        .gradleProperty("TANG_KINH_CAC_API_BASE_URL")
        .orElse("http://10.0.2.2:8000/api/v1/")
        .get()
        .let { value -> if (value.endsWith('/')) value else "$value/" }

android {
    namespace = "com.tangkinhcac.app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.tangkinhcac.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        // Override with -PTANG_KINH_CAC_API_BASE_URL=https://host/api/v1/ for another environment.
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
    }
    lint {
        disable += "AndroidGradlePluginVersion"
        disable += "GradleDependency"
        // The generated adaptive launcher intentionally coexists with density-specific bitmap fallbacks.
        disable += "IconXmlAndPng"
        warningsAsErrors = true
    }
}

ktlint {
    android.set(true)
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.sqlite.framework)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.gson)
    ksp(libs.androidx.room.compiler)
}
