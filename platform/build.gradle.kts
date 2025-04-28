import io.gitlab.arturbosch.detekt.Detekt
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
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.material.kolor)
            implementation(libs.kermit)
            implementation(libs.bignum)
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
    namespace = "com.erdodif.capsulate.platform"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$rootDir/detekt.yml")
    source.setFrom(
        "src/commonMain/kotlin",
        "src/nativeMain/kotlin",
        "src/commonTest/kotlin",
        "src/androidMain/kotlin",
        "src/desktopMain/kotlin",
        "src/iosMain/kotlin",
    )
    basePath = projectDir.absolutePath
    toolVersion = "1.23.8"
    // Android specifics
    ignoredBuildTypes = listOf("release", "debug")
    ignoredFlavors = listOf("production")
    ignoredVariants = listOf("productionRelease")
}

tasks.withType<Detekt>().configureEach {
    dependencies {
        detektPlugins(libs.detekt.rules.compose)
    }
    reports {
        xml.required.set(true)
        html.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
    }
}
