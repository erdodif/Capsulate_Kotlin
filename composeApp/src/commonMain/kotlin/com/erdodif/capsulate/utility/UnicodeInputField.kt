package com.erdodif.capsulate.utility

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erdodif.capsulate.onMobile
import com.erdodif.capsulate.resources.Res
import com.erdodif.capsulate.resources.jet_brains_mono_bold
import com.erdodif.capsulate.resources.jet_brains_mono_italic
import com.erdodif.capsulate.utility.chars.escapes
import com.erdodif.capsulate.utility.chars.getFromLatexPrefix
import com.erdodif.capsulate.utility.chars.getFromPrefix
import com.erdodif.capsulate.utility.chars.latexEscapes
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayNavigator
import org.jetbrains.compose.resources.Font

enum class MatchStatus {
    HasMatch,
    NoMatchButPrefix,
    Nothing
}

class UnicodeOverlay(private val useImePadding: Boolean = false) : Overlay<Char> {

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content(navigator: OverlayNavigator<Char>) {
        val modifier = if (useImePadding) Modifier.imePadding() else Modifier
        val focusRequester = remember { FocusRequester() }
        var value by remember { mutableStateOf(TextFieldValue("\\ ", TextRange(1))) }
        var index by remember { mutableStateOf(0) }
        val word = value.text.substring(1, value.text.length - 1)
        val match = escapes[word] ?: latexEscapes[word]
        var extraMatches by remember { mutableStateOf(CharArray(0)) }
        var matchStatus by remember { mutableStateOf(MatchStatus.Nothing) }
        val selectedChar = if (match != null && index < match.length) {
            match[index]
        } else {
            val newIndex = index - (match?.length ?: 0)
            if (newIndex < extraMatches.size) {
                extraMatches[newIndex]
            } else {
                ' '
            }
        }
        var prefixes by remember { mutableStateOf(emptyList<String>()) }
        val borderColor = when (matchStatus) {
            MatchStatus.HasMatch -> MaterialTheme.colorScheme.primary
            MatchStatus.NoMatchButPrefix -> MaterialTheme.colorScheme.tertiary
            MatchStatus.Nothing -> MaterialTheme.colorScheme.error
        }
        LaunchedEffect(value) {
            prefixes = getFromPrefix(word) + getFromLatexPrefix(word)
            matchStatus = when {
                match != null -> MatchStatus.HasMatch
                prefixes.isNotEmpty() -> MatchStatus.NoMatchButPrefix
                else -> MatchStatus.Nothing
            }
            extraMatches =
                prefixes.mapNotNull { escapes[it] ?: latexEscapes[it] }.flatMap { it.toList() }
                    .filter { it !in (match ?: "") }.toCharArray()
        }
        Column(
            modifier.background(
                MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f),
                RoundedCornerShape(5.dp)
            ),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                Modifier.fillMaxWidth()
                    .weight(if (match != null || extraMatches.isNotEmpty()) 1f else 3f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Top
            ) {
                IconButton({ navigator.finish(0.toChar()) }) {
                    Icon(Icons.Filled.Close, "Close")
                }
            }
            if (match != null || extraMatches.isNotEmpty()) {
                FlowRow(
                    Modifier.weight(2f).clip(RectangleShape)
                        .verticalScroll(rememberScrollState(0), true),
                    horizontalArrangement = Arrangement.Start
                ) {
                    match?.forEachIndexed { i, char ->
                        Text(
                            text = char.toString(),
                            fontSize = 24.sp,
                            modifier = Modifier.padding(2.dp)
                                .clickable { navigator.finish(char) }
                                .size(40.dp)
                                .padding(1.dp)
                                .background(
                                    if (i == index) MaterialTheme.colorScheme.tertiaryContainer else
                                        MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(5.dp)
                                ),
                            color = if (i == index) MaterialTheme.colorScheme.onTertiaryContainer else
                                MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center,
                            lineHeight = 40.sp
                        )
                    }
                    extraMatches.forEachIndexed { i, char ->
                        Text(
                            char.toString(),
                            fontSize = 24.sp,
                            modifier = Modifier.padding(2.dp)
                                .clickable { navigator.finish(char) }
                                .size(40.dp)
                                .padding(1.dp)
                                .background(
                                    if (i + (match?.length
                                            ?: 0) == index
                                    ) MaterialTheme.colorScheme.tertiaryContainer else
                                        MaterialTheme.colorScheme.surfaceContainerLow,
                                    RoundedCornerShape(5.dp)
                                ),
                            color = if (i + (match?.length
                                    ?: 0) == index
                            ) MaterialTheme.colorScheme.onTertiaryContainer else
                                MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center)
                    }
                }
            }
            Column(
                Modifier.weight(1f)
            ) {
                LazyRow(
                    Modifier.fillMaxWidth().padding(10.dp)
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    items(prefixes) {
                        Text(
                            "\\$it ",
                            fontFamily = FontFamily(Font(Res.font.jet_brains_mono_italic)),
                            modifier = Modifier.padding(5.dp, 2.dp).clickable {
                                index = 0
                                value = value.copy("\\$it ", selection = TextRange(it.length + 1))
                            },
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            style = TextStyle(fontFeatureSettings = "liga 0")
                        )
                    }
                }
                Row(
                    Modifier.fillMaxWidth().defaultMinSize(Dp.Unspecified, 60.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Spacer(Modifier.height(5.dp))
                    BasicTextField(
                        value,
                        {
                            if (it.text == " ") {
                                navigator.finish(0.toChar())
                                return@BasicTextField
                            }
                            if (it.text == value.text) {
                                if (!onMobile &&
                                    it.selection.collapsed &&
                                    it.selection.start != value.selection.start &&
                                    !match.isNullOrEmpty()
                                ) {
                                    val maxIndex = match.length + extraMatches.size
                                    index =
                                        (index + it.selection.start - value.selection.start + maxIndex) % maxIndex
                                }
                            } else {
                                index = 0
                            }
                            value = it.copy(it.text, selection = TextRange(it.text.length - 1))
                        },
                        Modifier.weight(4f).defaultMinSize(150.dp, 30.dp)
                            .padding(10.dp)
                            .border(2.dp, borderColor)
                            .focusRequester(focusRequester)
                            .background(Color.Transparent),
                        singleLine = true,
                        enabled = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            autoCorrect = false,
                            keyboardType = KeyboardType.Ascii,
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (match != null) navigator.finish(selectedChar) else navigator.finish(
                                    0.toChar()
                                )
                            }
                        ),
                        textStyle = TextStyle(
                            color = if (match != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                            fontSize = 30.sp,
                            fontFeatureSettings = "liga 0"
                        ),
                        cursorBrush = SolidColor(Color.Transparent)
                    )
                    if (match != null || extraMatches.isNotEmpty()) {
                        Text(
                            selectedChar.toString(),
                            fontSize = 40.sp,
                            modifier = Modifier
                                .clickable { navigator.finish(selectedChar) }
                                .weight(1f).height(50.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontFamily = FontFamily(Font(Res.font.jet_brains_mono_bold)),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Spacer(
                            Modifier.weight(1f).background(MaterialTheme.colorScheme.error)
                                .height(50.dp)
                        )
                    }
                }
            }
        }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
}
