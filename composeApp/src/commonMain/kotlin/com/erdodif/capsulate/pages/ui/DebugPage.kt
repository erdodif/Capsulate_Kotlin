package com.erdodif.capsulate.pages.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.erdodif.capsulate.pages.screen.DebugScreen
import com.erdodif.capsulate.utility.screenUiFactory
import com.slack.circuit.runtime.ui.Ui

class DebugPage: Ui<DebugScreen.State> {

    companion object Factory: Ui.Factory by screenUiFactory<DebugScreen>(::DebugPage)

    @Composable
    override fun Content(
        state: DebugScreen.State,
        modifier: Modifier
    ) {
        TODO("Highligted structogram and/or code needed. + The debug controls")
    }
}