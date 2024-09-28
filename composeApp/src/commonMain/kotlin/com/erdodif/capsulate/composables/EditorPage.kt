package com.erdodif.capsulate.composables

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.erdodif.capsulate.project.ProjectSelectorButton
import com.erdodif.capsulate.structogram.composables.StatementPreview

@Composable
fun EditorPage(){
    ProjectSelectorButton({_ -> }){Text("Open Project")}
    StatementPreview()
}