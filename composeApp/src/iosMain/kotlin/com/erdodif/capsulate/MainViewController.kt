package com.erdodif.capsulate

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.coroutineScope
import kotlin.system.exitProcess

fun MainViewController() = ComposeUIViewController {
    LaunchedEffect(Unit){
        coroutineScope {
            applicationExitJob.join()
            exitProcess(0)
        }
    }
    App()
}
