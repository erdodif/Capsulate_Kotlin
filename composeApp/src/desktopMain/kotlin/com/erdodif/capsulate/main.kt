package com.erdodif.capsulate

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.erdodif.capsulate.resources.Res
import com.erdodif.capsulate.resources.app_name
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import io.github.aakira.napier.Napier
import kotlinx.coroutines.coroutineScope
import org.jetbrains.compose.resources.stringResource

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
        Napier.base(object :Antilog(){
            override fun performLog(
                priority: LogLevel,
                tag: String?,
                throwable: Throwable?,
                message: String?
            ) {
                if(throwable != null){
                    println("[$priority]:\n$message\n${throwable.stackTrace}")
                }
                else{
                    println("[$priority]:\n$message")
                }
            }
        })
        App()
    }
}