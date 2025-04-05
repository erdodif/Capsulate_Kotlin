plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

group = "com.erdodif.capsulate"
version = "1.0.0"

kotlin {
    jvmToolchain(21)
    jvm("desktop")
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kermit)
            implementation(compose.runtime)
            implementation(compose.material3)
            implementation(compose.foundation)
            implementation(compose.animation)
            implementation(compose.ui)
            implementation(libs.compose.dnd)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(project(":language"))
            implementation(project(":composeUtils"))
            implementation(project(":annotations"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
    androidLibrary {
        namespace = "com.erdodif.capsulate"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
