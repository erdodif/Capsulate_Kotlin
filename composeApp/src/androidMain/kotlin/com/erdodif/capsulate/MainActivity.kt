package com.erdodif.capsulate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.coroutineScope
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LaunchedEffect(Unit) {
                coroutineScope {
                    applicationExitJob.join()
                    finishAfterTransition()
                    exitProcess(0)
                }
            }
            BackHandler(enabled = false) {}
            App()
        }
    }
}
