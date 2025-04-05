plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

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
        val desktopMain by getting
        commonMain.dependencies {
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.animation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.material.kolor)
            implementation(libs.kermit)
            implementation(libs.circuit.foundation)
            implementation(libs.filekit.core)
            implementation(libs.filekit.compose)
            implementation(libs.kotlinx.io.core.v070)
            implementation(project(":language"))
            implementation(project(":annotations"))
            implementation(project(":struktogram"))
            implementation(project(":composeUtils"))
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.kotlin.test.junit)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
