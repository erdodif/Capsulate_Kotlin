package com.erdodif.capsulate.utility

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import com.erdodif.capsulate.toPngByteArray
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun Modifier.imageExportable(
    coroutineScope: CoroutineScope,
    graphicsLayer: GraphicsLayer,
    takeImage: Boolean = true,
    onImage: (ImageBitmap) -> Unit = {
        coroutineScope.launch {
            FileKit.saveFile(it.toPngByteArray(), "struk", "png")
        }
    },
): Modifier = this.drawWithContent{
    if (takeImage) {
        graphicsLayer.record {
            this@drawWithContent.drawContent()
            coroutineScope.launch {
                onImage(graphicsLayer.toImageBitmap())
            }
        }
    }
    drawContent()
}
