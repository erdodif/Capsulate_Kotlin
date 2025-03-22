package com.erdodif.capsulate

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.erdodif.capsulate.resources.Res
import com.erdodif.capsulate.resources.app_name
import kotlinx.coroutines.coroutineScope
import org.jetbrains.compose.resources.stringResource
import java.awt.Dimension

fun main() = application {
    val appName = stringResource(Res.string.app_name)
    LaunchedEffect(Unit){
        coroutineScope {
            applicationExitJob.join()
            exitApplication()
        }
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = appName,
    ) {
        window.minimumSize = Dimension(450,400)
        App()
    }
}