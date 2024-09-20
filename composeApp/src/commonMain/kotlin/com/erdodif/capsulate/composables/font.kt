package com.erdodif.capsulate.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.erdodif.capsulate.resources.JetBrainsMono_Bold
import com.erdodif.capsulate.resources.JetBrainsMono_Italic
import com.erdodif.capsulate.resources.JetBrainsMono_Light
import com.erdodif.capsulate.resources.JetBrainsMono_LightItalic
import com.erdodif.capsulate.resources.JetBrainsMono_Medium
import com.erdodif.capsulate.resources.JetBrainsMono_MediumItalic
import com.erdodif.capsulate.resources.JetBrainsMono_Regular
import com.erdodif.capsulate.resources.Res
import org.jetbrains.compose.resources.Font

val fonts: FontFamily
    @Composable get() = FontFamily(
        Font(Res.font.JetBrainsMono_Regular, weight = FontWeight.Normal),
        Font(Res.font.JetBrainsMono_Italic, weight = FontWeight.Normal, style = FontStyle.Italic),
        Font(Res.font.JetBrainsMono_Medium, weight = FontWeight.Medium, style = FontStyle.Italic),
        Font(
            Res.font.JetBrainsMono_MediumItalic,
            weight = FontWeight.Medium,
            style = FontStyle.Italic
        ),
        Font(Res.font.JetBrainsMono_Bold, weight = FontWeight.Bold),
        Font(Res.font.JetBrainsMono_Light, weight = FontWeight.Light),
        Font(
            Res.font.JetBrainsMono_LightItalic,
            weight = FontWeight.Light,
            style = FontStyle.Italic
        ),
    )