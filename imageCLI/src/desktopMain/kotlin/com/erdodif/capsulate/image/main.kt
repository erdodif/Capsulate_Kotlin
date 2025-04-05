package com.erdodif.capsulate.image

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import com.erdodif.capsulate.lang.util.valueOrNull
import com.erdodif.capsulate.structogram.Structogram
import com.erdodif.capsulate.toPngByteArray
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.system.exitProcess
import kotlin.uuid.ExperimentalUuidApi
import com.erdodif.capsulate.utility.imageExportable

@OptIn(ExperimentalTestApi::class, ExperimentalUuidApi::class)
suspend fun getImage(width: Int, height: Int, input: String): ByteArray {
    val receiver = Channel<ImageBitmap>()
    runDesktopComposeUiTest(width = width, height = height) {
        setContent {
            val coroutineScope = rememberCoroutineScope()
            var struk: Structogram? by remember { mutableStateOf(null) }
            MaterialTheme {
                val graphicsLayer = rememberGraphicsLayer()
                LaunchedEffect(Unit) {
                    struk = Structogram.fromString(input).valueOrNull
                }
                struk?.Content(
                    Modifier.fillMaxSize().imageExportable(coroutineScope, graphicsLayer, onImage = {
                            coroutineScope.launch {
                                receiver.send(it)
                            }
                        })
                )
            }
        }
    }
    return receiver.receive().toPngByteArray()
}

fun getNamedParamValue(input: Array<String>, paramName: String): String? = input.firstOrNull {
    it.startsWith("--$paramName")
}?.split("=")?.get(1)

fun getParam(input: Array<String>): String? = input.firstOrNull { !it.startsWith('-') }

fun getInfo(message: String): String? = println(message).let { readln() }

@Suppress("ComplexCondition")
@OptIn(ExperimentalUuidApi::class, ExperimentalComposeUiApi::class, ExperimentalStdlibApi::class)
fun main(args: Array<String>) {
    var width = getNamedParamValue(args, "width")?.toIntOrNull()
    var height = getNamedParamValue(args, "height")?.toIntOrNull()
    var input = getNamedParamValue(args, "input")
    var location = getParam(args)
    if (input != null && (!input.startsWith('"') || !input.endsWith('"'))) input = null
    if (args.isEmpty()) {
        while (width == null) width = getInfo("Please specify the width:")?.toIntOrNull()
        while (height == null) height = getInfo("Please specify the width:")?.toIntOrNull()
        while (input == null) {
            input = getInfo("Please enter the code in \" \":")
            if (input != null && (!input.startsWith('"') || !input.endsWith('"'))) input = null
        }
        while (location == null) {
            location = getInfo("Please enter the output file location (including the filename):" )
        }
    } else if (
        args.size > 4 ||
        width == null ||
        height == null ||
        input == null ||
        location == null
    ) {
        println("usage: --width=<width> --height=<height> --input=\"<code>\" <location>")
        exitProcess(-1)
    }
    runBlocking {
        val image = getImage(width, height, input)
        val file = File(location.drop(1).dropLast(1))
        file.createNewFile()
        with(file.bufferedWriter()) {
            image.forEach { write(it.toInt()) }
            close()
        }
    }
    exitProcess(0)
}