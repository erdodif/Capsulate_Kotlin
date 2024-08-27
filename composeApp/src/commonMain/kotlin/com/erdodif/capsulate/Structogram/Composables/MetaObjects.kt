package com.erdodif.capsulate.Structogram.Composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun defaultCommandModifier() =
    Modifier
        .background(MaterialTheme.colorScheme.primary)
        .border(2.dp, MaterialTheme.colorScheme.inversePrimary)
        .padding(5.dp, 3.dp)

@Composable
fun commandPlaceHolder() = Spacer(defaultCommandModifier())

@Composable
fun StatementText(text: String, centered: Boolean = true, modifier: Modifier = Modifier) =
    Text(
        text = text,
        modifier = defaultCommandModifier().then(modifier),
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

