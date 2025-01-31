import java.io.ByteArrayOutputStream

plugins {
    id("com.android.application")
    id("kotlin-android")
    kotlin("plugin.serialization")
}

android {
    namespace = "com.micewine.emu"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.micewine.emu"
        minSdk = 28
        //noinspection ExpiredTargetSdkVersion
        targetSdk = 28
        versionCode = 5
        versionName = "v0.1.4"
        signingConfig = signingConfigs.getByName("debug")
        proguardFiles()
    }

    ndkVersion = "26.1.10909125"

    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                file("proguard-rules.pro"))
            ndk {
                abiFilters += listOf("arm64-v8a", "x86_64")
            }
            buildConfigField("String", "GIT_SHORT_SHA", "\"${getGitShortSHA()}\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    sourceSets {
        getByName("main") {
            aidl.srcDirs("src/main/aidl")
        }
    }
    
    buildFeatures {
        aidl = true
        buildConfig = true
        viewBinding = true
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildToolsVersion = "34.0.0"
}

dependencies {
    implementation("net.lingala.zip4j:zip4j:2.11.5")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("org.apache.commons:commons-compress:1.26.1")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.3")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.github.vatbub:mslinks:1.0.6.2")
    implementation("com.h6ah4i.android.widget.advrecyclerview:advrecyclerview:1.0.0")
    implementation("com.getkeepsafe.taptargetview:taptargetview:1.15.0")
    implementation(project(":app:stub"))
}

fun getGitShortSHA(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine = listOf("git", "rev-parse", "--short", "HEAD")
        standardOutput = stdout
    }
    return stdout.toString().trim()
}
