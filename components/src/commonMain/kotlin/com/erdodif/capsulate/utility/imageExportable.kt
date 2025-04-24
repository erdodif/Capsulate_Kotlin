package com.erdodif.capsulate.utility

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import com.erdodif.capsulate.toPngByteArray
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun Modifier.imageExportable(
    coroutineScope: CoroutineScope,
    graphicsLayer: GraphicsLayer,
    takeImage: Boolean = true,
    onImage: suspend (ImageBitmap) -> Unit = {
        FileKit.saveFile(it.toPngByteArray(), "struk", "png")
    },
): Modifier = this.drawWithContent {
    var job: Job? = null
    if (takeImage && job == null) {
        graphicsLayer.record {
            this@drawWithContent.drawContent()
            job = coroutineScope.launch {
                onImage(graphicsLayer.toImageBitmap())
            }
        }
    }
    drawContent()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.imageExportable(
    onImage: suspend (ImageBitmap) -> Unit = {
        FileKit.saveFile(it.toPngByteArray(), "struk", "png")
    },
): Modifier {
    var makeImage by remember { mutableStateOf(false) }
    var canMakeImage by remember { mutableStateOf(true) }
    SideEffect { makeImage = false }
    return this.combinedClickable(
        indication = null,
        interactionSource = null,
        enabled = true,
        onClick = {},
        onLongClick = { if (canMakeImage) makeImage = true }
    ).imageExportable(
        rememberCoroutineScope(),
        rememberGraphicsLayer(),
        canMakeImage && makeImage,
        {
            canMakeImage = false
            makeImage = false
            onImage(it)
            canMakeImage = true
        }
    )
}
