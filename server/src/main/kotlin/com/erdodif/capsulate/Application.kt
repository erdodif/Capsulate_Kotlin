package com.erdodif.capsulate

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import com.erdodif.capsulate.lang.util.valueOrNull
import com.erdodif.capsulate.structogram.Structogram
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.system.exitProcess
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

private val colors: Colors
    @Composable
    get() = MaterialTheme.colors.copy(
        primary = Color.White,
        secondary = Color.Yellow,
        onPrimary = Color.Blue,
        onSecondary = Color(239, 167, 130)
    )

private const val MAX_HEIGHT = 100000
private val availableFormats = arrayOf(
    EncodedImageFormat.PNG,
    EncodedImageFormat.JPEG,
    EncodedImageFormat.WEBP
)

@OptIn(ExperimentalTestApi::class, ExperimentalUuidApi::class, ExperimentalResourceApi::class)
suspend fun getImage(width: Int, height: Int, input: String): ImageBitmap {
    val receiver = Channel<Pair<ImageBitmap, Int>>(1, BufferOverflow.SUSPEND)
    var actualHeight = 0
    coroutineScope {
        val struk = Structogram.fromString(input).valueOrNull
        runDesktopComposeUiTest(width = width, height = if (height <= 0) MAX_HEIGHT else height) {
            setContent {
                MaterialTheme(colors = colors) {
                    struk?.Content(
                        Modifier.fillMaxWidth().onGloballyPositioned {
                            actualHeight = if (height <= 0) it.size.height else height
                        })
                }
            }
            runBlocking { receiver.send(this@runDesktopComposeUiTest.captureToImage() to actualHeight) }
        }
    }
    return receiver.receive().let { (image: ImageBitmap, height: Int) ->
        image.toAwtImage().getSubimage(0, 0, width, height).toComposeImageBitmap()
    }
}


fun getNamedParamValue(input: Array<String>, paramName: String): String? = input.firstOrNull {
    it.startsWith("--$paramName")
}?.dropWhile { it != '=' }?.drop(1)

fun getParam(input: Array<String>): String? = input.firstOrNull { !it.startsWith('-') }

fun getInfo(message: String): String? = println(message).let { readln() }

@Suppress("ComplexCondition")
@OptIn(ExperimentalUuidApi::class, ExperimentalComposeUiApi::class, ExperimentalStdlibApi::class)
fun main(args: Array<String>) {
    var width = getNamedParamValue(args, "width")?.toIntOrNull()
    var height = getNamedParamValue(args, "height")?.toIntOrNull()
    var input = getNamedParamValue(args, "input")
    var location = getParam(args)
    if (width == null) println("missing --width=")
    if (height == null) println("missing --height=")
    if (input == null) println("missing --input=\"\"")
    if (location == null) println("missing <location>")
    if (args.isEmpty()) {
        while (width == null) width = getInfo("Please specify the width:")?.toIntOrNull()
        while (height == null) height = getInfo("Please specify the height:")?.toIntOrNull()
        while (input == null) {
            input = getInfo("Please enter the code in \" \":")
        }
        while (location == null) {
            location = getInfo("Please enter the output file location (including the filename):")
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
    if (width < 0) {
        println("Auto width-sizing is not supported, please give a positive width value!")
        exitProcess(-1)
    }
    println("Creating the ${width}x${if (height <= 0) "UNLIMITED" else height} image")
    val format = try {
        val extension = location.split('.').lastOrNull()?.uppercase()!!
        availableFormats.first { it.name == extension }
    } catch (_: Exception) {
        println("Filetype unsupported!\nSupported image types: ${availableFormats.joinToString()}")
        exitProcess(-1)
    }
    val image: ImageBitmap
    runBlocking {
        if (height <= 0) {
            println("Auto height measure triggered, maximum height supported: $MAX_HEIGHT")
        }
        image = getImage(width, height, input.replace("\\n", "\n"))
        File(location).writeBytes(
            Image.makeFromBitmap(image.asSkiaBitmap())
                .encodeToData(format, 100)!!.bytes
        )
    }
    println("Image created with size ${image.width}x${image.height} and can be found at $location")
    exitProcess(0)
}