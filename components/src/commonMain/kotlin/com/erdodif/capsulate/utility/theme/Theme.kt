package com.erdodif.capsulate.utility.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

object Theme {
    var borderColor: Color = Color.Unspecified
    var borderWidth: Dp = 4.dp
    val commandModifier
        @Composable get() = Modifier.background(MaterialTheme.colorScheme.primary)
            .defaultMinSize(100.dp, 25.dp)
    val commandPadding = PaddingValues(10.dp, 6.dp)
    val casePadding = PaddingValues(28.dp, 6.dp, 10.dp, 6.dp)
    val elsePadding = PaddingValues(10.dp, 6.dp, 28.dp, 6.dp)
    val ifPadding = PaddingValues(28.dp, 6.dp)

    @Composable
    fun initialize() {
        borderColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.5f)
            .compositeOver(MaterialTheme.colorScheme.scrim)
    }
}


@Composable
fun Modifier.caseIndicator(color: Color = Theme.borderColor): Modifier = this.drawBehind {
    val borderWidth = Theme.borderWidth
    drawLine(
        color = color,
        start = Offset((borderWidth + 8.dp).toPx(), (-borderWidth / 2).toPx()),
        end = Offset(
            (borderWidth + 18.dp).toPx(),
            drawContext.size.height + (borderWidth / 2).toPx()
        ),
        strokeWidth = borderWidth.toPx(),
        cap = StrokeCap.Butt,
    )
}

@Composable
fun Modifier.elseIndicator(color: Color = Theme.borderColor): Modifier = this.drawBehind {
    val borderWidth = Theme.borderWidth
    val width = drawContext.size.width
    drawLine(
        color = color,
        start = Offset(width - (borderWidth + 8.dp).toPx(), (-borderWidth / 2).toPx()),
        end = Offset(
            width - (borderWidth + 18.dp).toPx(),
            drawContext.size.height + (borderWidth / 2).toPx()
        ),
        strokeWidth = borderWidth.toPx(),
        cap = StrokeCap.Butt
    )
}

@Composable
fun Modifier.awaitIndicator(): Modifier = this.drawBehind {
    val path = Path()
    val height = min(drawContext.size.height, 32.dp.toPx())
    val width = drawContext.size.width
    val borderWidth = Theme.borderWidth.toPx()
    path.moveTo(-borderWidth / 2f, height)
    path.quadraticTo(0f, borderWidth / 4f, width / 2f, borderWidth / 4f)
    path.quadraticTo(width, borderWidth / 4f, width + borderWidth / 2f, height)
    drawPath(
        path,
        Theme.borderColor,
        style = Stroke(width = borderWidth)
    )
}

@Composable
fun commandPlaceHolder(modifier: Modifier = Modifier) = StatementText("", modifier = modifier)

val defaultModifier = Modifier.padding(Theme.commandPadding)

@Composable
fun StatementText(
    text: String,
    centered: Boolean = true,
    modifier: Modifier = defaultModifier
) = Text(
    text = text.replace("\n", " "),
    modifier = Theme.commandModifier.then(modifier),
    color = MaterialTheme.colorScheme.onPrimary,
    fontSize = TextUnit.Unspecified,
    fontStyle = null,
    fontWeight = FontWeight(700),
    fontFamily = FontFamily.Monospace,
    letterSpacing = TextUnit.Unspecified,
    textDecoration = null,
    textAlign = if (centered) TextAlign.Center else TextAlign.Start,
    lineHeight = 20.sp,
    overflow = TextOverflow.Clip,
    softWrap = true,
    style = LocalTextStyle.current
)

@Composable
fun HorizontalBorder(modifier: Modifier = Modifier) = Spacer(
    modifier.fillMaxWidth().height(Theme.borderWidth).background(Theme.borderColor)
)

@Composable
fun VerticalBorder(modifier: Modifier = Modifier) = Spacer(
    modifier.fillMaxHeight().width(Theme.borderWidth).background(Theme.borderColor)
)

