package com.erdodif.capsulate.project

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import kotlinx.io.files.Path

@Composable
fun ProjectSelectorButton(
    onProjectSelected: (Project) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val picker = rememberDirectoryPickerLauncher("Open Project"){
        onProjectSelected(Project(Path(it?.path.toString())))
    }
    Button({picker.launch()}, modifier, content = content)
}