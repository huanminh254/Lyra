import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

val supabaseProperties = Properties().apply {
    val propertiesFile = rootProject.file("supabase.properties")
    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use { load(it) }
    }
}

fun String.toBuildConfigString(): String =
    "\"${replace("\\", "\\\\").replace("\"", "\\\"")}\""

android {
    namespace = "com.devpro.sound"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.devpro.sound"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "SUPABASE_URL",
            supabaseProperties.getProperty(
                "supabase.url",
                "https://uetfxxexepywuyqbbtwn.supabase.co"
            ).toBuildConfigString()
        )
        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            supabaseProperties.getProperty("supabase.anonKey", "").toBuildConfigString()
        )
        buildConfigField(
            "String",
            "SUPABASE_AUDIO_BUCKET",
            supabaseProperties.getProperty("supabase.audioBucket", "covers").toBuildConfigString()
        )
        buildConfigField(
            "String",
            "SUPABASE_COVER_BUCKET",
            supabaseProperties.getProperty("supabase.coverBucket", "covers").toBuildConfigString()
        )
        buildConfigField(
            "String",
            "SUPABASE_COVER_PREFIX",
            supabaseProperties.getProperty("supabase.coverPrefix", "vpop").toBuildConfigString()
        )
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
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.appcompat)
    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)
    implementation(libs.firebase.firestore)
    implementation(libs.hilt.android)
    implementation(libs.firebase.auth)
    ksp(libs.hilt.compiler)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.swiperefreshlayout)

    testImplementation(libs.junit)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
