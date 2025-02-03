package com.erdodif.capsulate.utility

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erdodif.capsulate.structogram.composables.Theme

private val theme = ColorScheme(
    primary = Color(84, 168, 245),
    onPrimary = Color(32, 32, 32),
    primaryContainer = Color(29, 30, 33),
    onPrimaryContainer = Color(84, 168, 245),
    inversePrimary = Color(84, 168, 245),
    secondary = Color(178, 173, 95),
    onSecondary = Color(32, 32, 32),
    secondaryContainer = Color(178, 173, 95),
    onSecondaryContainer = Color(32, 32, 32),
    tertiary = Color(206, 142, 108),
    onTertiary = Color(32, 32, 32),
    tertiaryContainer = Color(243, 180, 148),
    onTertiaryContainer = Color(32, 32, 32),
    background = Color(42, 45, 48),
    onBackground = Color(196, 197, 204),
    surface = Color(29, 30, 33),
    onSurface = Color(196, 197, 204),
    surfaceVariant = Color(77, 78, 81),
    onSurfaceVariant = Color(0, 0, 0),
    surfaceTint = Color(0, 0, 0),
    inverseSurface = Color(196, 197, 204),
    inverseOnSurface = Color(29, 30, 33),
    error = Color(208, 46, 46),
    onError = Color(43, 10, 10),
    errorContainer = Color(112, 61, 61),
    onErrorContainer = Color(45, 10, 10),
    outline = Color(0, 0, 0),
    outlineVariant = Color(32, 32, 32),
    scrim = Color(0, 0, 0),
    surfaceBright = Color(255, 255, 255),
    surfaceDim = Color(0, 0, 0),
    surfaceContainer = Color(29, 29, 29),
    surfaceContainerHigh = Color(49, 40, 43),
    surfaceContainerHighest = Color(69, 60, 63),
    surfaceContainerLow = Color(19, 20, 23),
    surfaceContainerLowest = Color(9, 10, 13),
)

@Composable
fun PreviewColumn(width: Dp = 250.dp, content: LazyGridScope.() -> Unit) = MaterialTheme(
    colorScheme = theme
) {
    Theme.initialize()
    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(width),
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(10.dp)
                .heightIn(max = maxHeight).fillMaxSize().align(Alignment.Center),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            content()
        }
    }
}

@Composable
fun Header(text: String) = Text(
    text, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp,
    modifier = Modifier.padding(bottom = 5.dp)
)

fun LazyGridScope.labeled(text: String, content: @Composable (ColumnScope.() -> Unit)) = item {
    Column(Modifier.fillMaxSize().padding(10.dp)) {
        Header(text)
        content()
    }
}

