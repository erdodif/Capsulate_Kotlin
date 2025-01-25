package com.erdodif.capsulate

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.ComposeUIViewController
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.coroutineScope
import kotlin.system.exitProcess

fun MainViewController() = ComposeUIViewController {
    Napier.base(DebugAntilog())
    LaunchedEffect(Unit){
        coroutineScope {
            applicationExitJob.join()
            exitProcess(0)
        }
    }
    App()
}