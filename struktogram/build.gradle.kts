import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    id("kotlin-parcelize")
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    kotlin("plugin.serialization") version  libs.versions.kotlinx.serialization
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.animation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.compose.dnd)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.material.kolor)
            implementation(libs.kermit)
            implementation(libs.kotlinx.io.core)
            implementation(libs.kotlinx.collections.immutable)
            // -- Modules --
            implementation(projects.platform)
            implementation(projects.components)
            implementation(projects.language)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidTarget {
            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            compilerOptions.freeCompilerArgs.addAll(
                "-P",
                "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=com.erdodif.capsulate.KParcelize"
            )
        }
    }
}

android {
    namespace = "com.erdodif.capsulate.struktogram"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
