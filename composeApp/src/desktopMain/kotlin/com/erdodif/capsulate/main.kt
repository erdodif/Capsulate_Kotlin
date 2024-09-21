package com.erdodif.capsulate

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.LogLevel
import io.github.aakira.napier.Napier

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "KotlinProject",
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