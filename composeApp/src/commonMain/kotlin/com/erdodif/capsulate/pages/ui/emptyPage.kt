package com.erdodif.capsulate.pages.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erdodif.capsulate.pages.screen.EmptyScreen
import com.erdodif.capsulate.presets.presets
import com.erdodif.capsulate.resources.Res
import com.erdodif.capsulate.resources.app_name
import com.erdodif.capsulate.resources.ic_logo_foreground_monochrome_paddingless
import com.erdodif.capsulate.resources.open_folder
import com.erdodif.capsulate.resources.open_preset
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
fun emptyPage(): Ui<EmptyScreen.State> = ui { state, modifier ->
    Column(
        modifier = modifier.background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painterResource(Res.drawable.ic_logo_foreground_monochrome_paddingless),
            stringResource(Res.string.app_name),
            Modifier.size(160.dp).padding(10.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            stringResource(Res.string.app_name),
            Modifier.padding(20.dp),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 24.sp
        )
        Column(
            Modifier.defaultMinSize(10.dp, 300.dp).fillMaxWidth().padding(40.dp, 10.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(10.dp))
                .padding(30.dp)
        ) {
            Button(
                { state.eventHandler(EmptyScreen.Event.ToProjectPage) },
                Modifier.align(Alignment.CenterHorizontally)
            ) { Text(stringResource(Res.string.open_folder)) }
            Button(
                { state.eventHandler(EmptyScreen.Event.OpenPresetModal) },
                Modifier.align(Alignment.CenterHorizontally)
            ) { Text(stringResource(Res.string.open_preset)) }
            if (state.presetVisible) {
                ModalBottomSheet(
                    onDismissRequest = { state.eventHandler(EmptyScreen.Event.ClosePresetModal) },
                    sheetState = state.bottomSheetState,
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        items(presets, { it.headerText }) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ){
                                Text(it.headerText)
                                Button(
                                    { state.eventHandler(EmptyScreen.Event.SelectPreset(it)) },
                                ) { Text("Open") }
                            }
                        }
                    }
                }
            }
        }
    }
}
