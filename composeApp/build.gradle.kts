import com.android.build.gradle.internal.packaging.defaultExcludes
import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    id("kotlin-parcelize")
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    kotlin("plugin.serialization") version "1.5.30"
}

dependencies{
    detektPlugins(libs.detekt.rules.compose)
}

kotlin {
    jvmToolchain(21)
    jvm("desktop")

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

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
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.animation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.kotlin.test)
            implementation(libs.kermit)
            implementation(libs.kmpfile)
            implementation(libs.kmpfile.filekit)
            implementation(libs.kmpfile.okio)
            implementation(libs.okio)
            implementation(libs.filekit.core)
            implementation(libs.filekit.compose)
            implementation(libs.circuit.foundation)
            implementation(libs.circuit.overlay)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.compose.dnd)
            implementation(libs.material.kolor)
            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.multiplatform.settings.serialization)
            implementation(libs.adaptive)
            implementation(libs.adaptive.layout)
            implementation(libs.adaptive.navigation)
            implementation(libs.kotlinx.datetime)
            implementation(libs.androidx.core.splashscreen)
            implementation(libs.kotlinx.io.core)
            implementation(libs.slf4j.api)
            implementation(libs.slf4j.simple)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.kotlin.test.junit)
        }
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.kotlin.test.junit)
            implementation(libs.material)
        }
        iosMain.dependencies {
        }
        commonTest.dependencies {
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
            implementation(kotlin("reflect"))
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
    namespace = "com.erdodif.capsulate"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/main/res", "src/commonMain/composeResources")
    sourceSets["main"].resources.srcDirs(
        "src/commonMain/resources", "src/commonMain/composeResources"
    )

    defaultConfig {
        applicationId = "com.erdodif.capsulate"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            multiDexKeepProguard = file("multidex-config.pro")

            signingConfig = signingConfigs.getByName("debug") // Add release cert on PlayStore
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.erdodif.capsulate.resources"
    defaultExcludes.plus("drawable-anydpi-v26/*")
    generateResClass = always
}

compose.desktop {
    application {
        mainClass = "com.erdodif.capsulate.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Pkg)
            packageName = "com.erdodif.capsulate"
            packageVersion = "1.0.0"
        }
        buildTypes.release.proguard {
            configurationFiles.from("proguard-desktop-rules.pro")
            joinOutputJars = true
            optimize = true
            obfuscate = false
            version.set("7.6.1")
        }
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$projectDir/detekt.yml")
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
}

// Kotlin DSL
tasks.withType<Detekt>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
    }
}
