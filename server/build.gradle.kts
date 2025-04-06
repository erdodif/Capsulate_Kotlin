@file:OptIn(ExperimentalComposeLibrary::class)

import com.android.build.gradle.internal.cxx.io.removeDuplicateFiles
import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    application
}

dependencies{
    // -- From Common --
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.animation)
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(compose.uiTest)
    implementation(compose.components.resources)
    implementation(compose.components.uiToolingPreview)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime.compose)
    // -- Desktop --
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.kotlin.test.junit)
    // -- Modules --
    implementation(projects.platform)
    implementation(projects.components)
    implementation(projects.language)
    implementation(projects.struktogram)
}

group = "com.erdodif.capsulate"
version = "1.0.0"
application {
    mainClass.set("com.erdodif.capsulate.ApplicationKt")
    applicationDefaultJvmArgs = listOf()
}
tasks.withType<Tar>{
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Zip>{
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
