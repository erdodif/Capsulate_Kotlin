plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

group = "com.erdodif.capsulate"
version = "1.0.0"

kotlin {

    jvm("desktop")
    iosX64()
    iosArm64()
    iosSimulatorArm64()


    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(libs.kotlinx.io.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kermit)
            implementation(libs.kotlinx.collections.immutable)
            implementation(project(":annotations"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        desktopMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.kotlin.test.junit)
        }
        androidMain.dependencies {
            implementation(libs.kotlin.test.junit)
        }
    }
    androidLibrary {
        namespace = "com.erdodif.capsulate"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}