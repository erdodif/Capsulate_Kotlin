package com.erdodif.capsulate

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.erdodif.capsulate.resources.jet_brains_mono_bold
import com.erdodif.capsulate.resources.jet_brains_mono_italic
import com.erdodif.capsulate.resources.jet_brains_mono_light
import com.erdodif.capsulate.resources.jet_brains_mono_light_italic
import com.erdodif.capsulate.resources.jet_brains_mono_medium
import com.erdodif.capsulate.resources.jet_brains_mono_medium_italic
import com.erdodif.capsulate.resources.jet_brains_mono_regular
import com.erdodif.capsulate.resources.Res
import org.jetbrains.compose.resources.Font

val fonts: FontFamily
    @Composable get() = FontFamily(
        Font(Res.font.jet_brains_mono_regular, weight = FontWeight.Normal),
        Font(Res.font.jet_brains_mono_italic, weight = FontWeight.Normal, style = FontStyle.Italic),
        Font(Res.font.jet_brains_mono_medium, weight = FontWeight.Medium, style = FontStyle.Italic),
        Font(
            Res.font.jet_brains_mono_medium_italic,
            weight = FontWeight.Medium,
            style = FontStyle.Italic
        ),
        Font(Res.font.jet_brains_mono_bold, weight = FontWeight.Bold),
        Font(Res.font.jet_brains_mono_light, weight = FontWeight.Light),
        Font(
            Res.font.jet_brains_mono_light_italic,
            weight = FontWeight.Light,
            style = FontStyle.Italic
        ),
    )
