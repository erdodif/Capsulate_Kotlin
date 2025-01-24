package com.erdodif.capsulate.pages.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.erdodif.capsulate.lang.util.Fail
import com.erdodif.capsulate.lang.util.Left
import com.erdodif.capsulate.lang.util.Right
import com.erdodif.capsulate.pages.screen.PresetScreen
import com.erdodif.capsulate.pages.screen.PresetScreen.Event
import com.erdodif.capsulate.pages.screen.PresetScreen.State
import com.erdodif.capsulate.presets.Preset
import com.erdodif.capsulate.resources.Res
import com.erdodif.capsulate.resources.close
import com.erdodif.capsulate.structogram.Structogram
import com.erdodif.capsulate.utility.CodeEditor
import com.erdodif.capsulate.utility.screenUiFactory
import com.slack.circuit.runtime.ui.Ui
import org.jetbrains.compose.resources.stringResource

class PresetPage(private val preset: Preset) : Ui<State> {
    companion object Factory : Ui.Factory by screenUiFactory<PresetScreen>(
        { screen -> PresetPage(screen.preset) }
    )

    @Composable
    override fun Content(state: State, modifier: Modifier) {
        Scaffold(
            modifier = Modifier.padding(15.dp),
            floatingActionButton = {
                Button({ state.eventHandler(Event.Close) }) {
                    Row {
                        Text(stringResource(Res.string.close))
                    }
                }
            }) {
            LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                item {
                    Text(preset.headerText)
                }
                items(preset.demos, { it.description }) {
                    Surface(Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(Modifier.fillMaxWidth()) {
                            Text(it.description)
                            CodeEditor(
                                TextFieldValue(it.code),
                                Modifier.heightIn(max = 350.dp).fillMaxWidth()
                            )
                            val result =
                                remember { derivedStateOf { Structogram.fromString(it.code) } }
                            when (result.value) {
                                is Left<Structogram> -> (result.value as Left<Structogram>).value.content()
                                is Right<Fail> -> Text(
                                    (result.value as Right<Fail>).value.reason,
                                    color = Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}