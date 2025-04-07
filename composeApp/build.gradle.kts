import com.android.build.gradle.internal.packaging.defaultExcludes
import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.kotlin.dsl.withType
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

kotlin {
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

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.kotlin.test.junit)
            implementation(libs.material)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.kotlin.test.junit)
        }
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
            // -- Modules --
            implementation(projects.platform)
            implementation(projects.components)
            implementation(projects.language)
            implementation(projects.struktogram)
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
        versionName = libs.versions.self.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            multiDexKeepProguard = file("multidex-config.pro")

            signingConfig = signingConfigs.getByName("debug") // Add release cert on PlayStore
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    dependencies {
        debugImplementation(compose.uiTooling)
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
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.erdodif.capsulate"
            packageVersion = libs.versions.self.get()
            val versionSteps = libs.versions.self.get().split('.', '-')
            // Because sem-ver isn't supported everywhere
            val friendlyVersion = if (
                versionSteps.last().toIntOrNull() == null || versionSteps.count() > 3
            ) {
                "${versionSteps[0]}.${versionSteps[1]}.${
                    (versionSteps[2].toIntOrNull()?.let { it + 1 }) ?: 1
                }"
            } else {
                versionSteps.joinToString(".")
            }
            macOS {
                dmgPackageVersion = if (versionSteps.first() == "0") "1.0.0" else friendlyVersion
                iconFile.set(project.file("$rootDir/img/logo/logo.icns"))
            }
            windows {
                msiPackageVersion = friendlyVersion
                iconFile.set(project.file("$rootDir/img/logo/logo.ico"))
            }
            linux {
                modules("jdk.security.auth")
                debPackageVersion = libs.versions.self.get()
                iconFile.set(project.file("$rootDir/img/logo/logo.png"))
            }
        }
        buildTypes.release.proguard {
            configurationFiles.from("$rootDir/proguard-desktop-rules.pro")
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
