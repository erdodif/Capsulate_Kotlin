package com.erdodif.capsulate.project

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.core.PlatformDirectory

@Composable
fun FolderSelectorButton(
    onFolderSelected: (PlatformDirectory?) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val picker = rememberDirectoryPickerLauncher("Open Project"){
        onFolderSelected(it)
    }
    Button({picker.launch()}, modifier, content = content)
}