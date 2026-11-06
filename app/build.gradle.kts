import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// Chave da LocationIQ lida do local.properties (não versionado):
// LOCATIONIQ_KEY=pk.xxxxxxxx
val localProps = Properties().apply {
    val arquivo = rootProject.file("local.properties")
    if (arquivo.exists()) arquivo.inputStream().use { load(it) }
}

android {
    namespace = "unoeste.fipp.appaondeestou"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "unoeste.fipp.appaondeestou"
        minSdk = 31
        targetSdk = 37
        versionCode = 3
        versionName = "3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "LOCATIONIQ_KEY",
            "\"${localProps.getProperty("LOCATIONIQ_KEY", "")}\""
        )
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.viewpager2)
    implementation(libs.osmdroid)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
